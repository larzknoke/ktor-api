package com.larsknoke.routes

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.html.*

fun Route.customRoutes() {
    get("/foo") {
        call.respondHtml {
            body {
                h1 { + "Foo" }
            }
        }
    }
    get("/bar") {
        call.respondHtml {
            body {
                h1 { + "Bar" }
            }
        }
    }
}

fun Application.registerCustomRoutes() {
    routing {
        customRoutes()
    }
}
