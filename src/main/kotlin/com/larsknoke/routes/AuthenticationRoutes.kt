package com.larsknoke.routes

import com.larsknoke.db.DatabaseConnection
import com.larsknoke.entities.UserEntity
import com.larsknoke.models.NoteResponse
import com.larsknoke.models.User
import com.larsknoke.models.UserCredentials
import com.larsknoke.utils.TokenManager
import com.typesafe.config.ConfigFactory
import io.ktor.application.*
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.ktorm.dsl.*
import org.mindrot.jbcrypt.BCrypt

fun Application.registerAuthenticationRoutes(){
    val db = DatabaseConnection.database
    val tokenManager = TokenManager(HoconApplicationConfig(ConfigFactory.load()))

    routing {
        post("/register"){
            val userCredentials = call.receive<UserCredentials>()


            if (!userCredentials.isValidCredentials()){
                call.respond(
                    HttpStatusCode.BadRequest,
                    NoteResponse(success = false, data = "Username or Password must be longer!")
                )
                return@post
            }

            val username = userCredentials.username.lowercase()
            val password = userCredentials.hashedPassword()

            // check if user already in use
            val user = db.from(UserEntity)
                .select()
                .where{UserEntity.username eq username}
                .map { it[UserEntity.username] }
                .firstOrNull()

            if (user != null){
                call.respond(
                    HttpStatusCode.BadRequest,
                    NoteResponse(success = false, data = "User already in use! Try another one.")
                )
                return@post
            }

            db.insert(UserEntity){
                set(it.username, username)
                set(it.password, password)
            }

            call.respond(
                HttpStatusCode.Created,
                NoteResponse(success = true, data = "User created!")
            )
        }

        post("/login"){
            val userCredentials = call.receive<UserCredentials>()

            if (!userCredentials.isValidCredentials()){
                call.respond(
                    HttpStatusCode.BadRequest,
                    NoteResponse(success = false, data = "Username or Password must be longer!")
                )
                return@post
            }

            val username = userCredentials.username.lowercase()
            val password = userCredentials.password


            //check if user exists
            val user = db.from(UserEntity)
                .select()
                .where{UserEntity.username eq username}
                .map {
                    val id = it[UserEntity.id]!!
                    val username = it[UserEntity.username]!!
                    val password = it[UserEntity.password]!!
                    User(id, username, password)
                }.firstOrNull()

            if (user == null){
                call.respond(
                    HttpStatusCode.BadRequest,
                    NoteResponse(success = false, data = "Invalid username or password.")
                )
                return@post
            }

            val passwordCorrect = BCrypt.checkpw(password, user?.password)
            if (!passwordCorrect) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    NoteResponse(success = false, data = "Invaled username or password")
                )
                return@post
            }

            val token = tokenManager.generateJWTToken(user)
            call.respond(
                HttpStatusCode.OK,
                NoteResponse(success = true, data = token)
            )


        }
    }

}
