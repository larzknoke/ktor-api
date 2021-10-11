package com.larsknoke.routes

import com.larsknoke.db.DatabaseConnection
import com.larsknoke.entities.NotesEntity
import com.larsknoke.models.Note
import com.larsknoke.models.NoteRequest
import com.larsknoke.models.NoteResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.ktorm.dsl.*

fun Application.registerNotesRoutes() {

    val db = DatabaseConnection.database

    routing {
        authenticate("auth-jwt") {
            get("/notes"){
                val notes = db.from(NotesEntity).select()
                    .map {
                        println("${it}")
                        val id = it[NotesEntity.id]
                        val note = it[NotesEntity.note]
                        Note(id ?: -1, note ?: "")
                    }
                call.respond(notes)
            }
        }

        post("/notes"){
            val request = call.receive<NoteRequest>()
            val result = db.insert(NotesEntity){
                set(it.note, request.note)
            }

            if (result == 1) {
                call.respond(HttpStatusCode.OK, NoteResponse(
                    success = true,
                    data = "Value has been successfully inserted"
                ))
            } else {
                call.respond(HttpStatusCode.BadRequest, NoteResponse(
                    success = false,
                    data = "Failed to insert values"
                ))
            }
        }

        get("/notes/{id}"){
            val id = call.parameters["id"]?.toInt() ?: -1

            val note = db.from(NotesEntity)
                .select()
                .where{NotesEntity.id eq id}
                .map{
                    val id = it[NotesEntity.id]!!
                    val note = it[NotesEntity.note]!!
                    Note(id, note)
                }.firstOrNull()

            if(note == null){
                call.respond(HttpStatusCode.NotFound,
                    NoteResponse(
                     success = false,
                        data = "could not found note with id ${id}"
                ))
            } else {
                call.respond(HttpStatusCode.OK,
                    NoteResponse(
                        success = true,
                        data = note
                    )
                )
            }
        }

        put("/notes/{id}"){
            val id =call.parameters["id"]?.toInt() ?: -1
            val updatedNote =call.receive<NoteRequest>()

            val rowsEffected = db.update(NotesEntity){
                set(it.note, updatedNote.note)
                where{
                    it.id eq id
                }
            }

            if(rowsEffected == 1) {
                call.respond(
                    HttpStatusCode.OK,
                    NoteResponse(
                        success = true,
                        data = "Note has been updated"
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.BadRequest,
                    NoteResponse(
                        success = false,
                        data = "Note failed to update"
                    )
                )
            }
        }

        delete("/notes/{id}"){
            val id =call.parameters["id"]?.toInt() ?: -1
            val rowsEffected = db.delete(NotesEntity){
                it.id eq id
            }

            if(rowsEffected == 1) {
                call.respond(
                    HttpStatusCode.OK,
                    NoteResponse(
                        success = true,
                        data = "Note has been delete"
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.BadRequest,
                    NoteResponse(
                        success = false,
                        data = "Note failed to delete"
                    )
                )
            }
        }
    }
}
