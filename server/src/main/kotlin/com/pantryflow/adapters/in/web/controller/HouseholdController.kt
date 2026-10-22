package com.pantryflow.adapters.`in`.web.controller

import com.pantryflow.adapters.`in`.web.databaseCall
import com.pantryflow.adapters.`in`.web.dto.CreateHouseholdRequest
import com.pantryflow.adapters.`in`.web.dto.JoinHouseholdRequest
import com.pantryflow.adapters.`in`.web.mapper.toCommand
import com.pantryflow.adapters.`in`.web.mapper.toResponse
import com.pantryflow.adapters.`in`.web.uuid
import com.pantryflow.application.port.`in`.HouseholdUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.householdRoutes(households: HouseholdUseCase) {
    route("/households") {
        post {
            val command = call.receive<CreateHouseholdRequest>().toCommand()
            call.respond(HttpStatusCode.Created, databaseCall { households.create(command) }.toResponse())
        }
        post("/join") {
            val command = call.receive<JoinHouseholdRequest>().toCommand()
            call.respond(databaseCall { households.join(command) }.toResponse())
        }
    }
    get("/users/{userId}/households") {
        val result = databaseCall { households.listForUser(call.uuid("userId")) }
        call.respond(result.map { it.toResponse() })
    }
}
