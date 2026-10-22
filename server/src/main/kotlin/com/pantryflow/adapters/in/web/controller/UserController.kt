package com.pantryflow.adapters.`in`.web.controller

import com.pantryflow.adapters.`in`.web.databaseCall
import com.pantryflow.adapters.`in`.web.dto.CreateUserRequest
import com.pantryflow.adapters.`in`.web.mapper.toCommand
import com.pantryflow.adapters.`in`.web.mapper.toResponse
import com.pantryflow.adapters.`in`.web.uuid
import com.pantryflow.application.port.`in`.UserUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.userRoutes(users: UserUseCase) {
    route("/users") {
        post {
            val command = call.receive<CreateUserRequest>().toCommand()
            call.respond(HttpStatusCode.Created, databaseCall { users.create(command) }.toResponse())
        }
        get("/{userId}") {
            call.respond(databaseCall { users.get(call.uuid("userId")) }.toResponse())
        }
    }
}
