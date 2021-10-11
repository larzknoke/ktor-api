package com.larsknoke

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.larsknoke.routes.registerAuthenticationRoutes
import com.larsknoke.routes.registerNotesRoutes
import com.larsknoke.routes.registerCustomRoutes
import com.typesafe.config.ConfigFactory
import freemarker.cache.ClassTemplateLoader
import freemarker.core.HTMLOutputFormat
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.config.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kotlinx.html.*



fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {

    val config = HoconApplicationConfig(ConfigFactory.load())

    install(CallLogging)

    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
        outputFormat = HTMLOutputFormat.INSTANCE
    }
    install(ContentNegotiation){
        json()
    }

    val secret = config.property("jwt.secret").getString()
    val issuer = config.property("jwt.issuer").getString()
    val audience = config.property("jwt.audience").getString()
    val myRealm = config.property("jwt.realm").getString()
    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(
                JWT
                .require(Algorithm.HMAC256(secret))
                .withAudience(audience)
                .withIssuer(issuer)
                .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }


    registerNotesRoutes()
    registerCustomRoutes()
    registerAuthenticationRoutes()

    routing {
        static("/static") {
            resources("files")
        }
        get("/") {
            call.respond(FreeMarkerContent("index.ftl", mapOf("entries" to blogEntries), ""))
        }
        post("/submit") {
            val params = call.receiveParameters()
            val headline = params["headline"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val body = params["body"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val newEntry = BlogEntry(headline, body)
            blogEntries.add(0, newEntry)
            call.respondHtml {
                body {
                    h1 {
                        +"Thanks for submitting your entry!"
                    }
                    p {
                        +"We've submitted your new entry titled "
                        b {
                            +newEntry.headline
                        }
                    }
                    p {
                        +"You have submitted a total of ${blogEntries.count()} articles!"
                    }
                    a("/") {
                        +"Go back"
                    }
                }
            }
        }
    }
}
