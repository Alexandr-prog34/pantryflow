package com.pantryflow.adapters.`in`.web.controller

import com.pantryflow.adapters.`in`.web.databaseCall
import com.pantryflow.adapters.`in`.web.dto.CreateShoppingItemRequest
import com.pantryflow.adapters.`in`.web.dto.PurchaseRequest
import com.pantryflow.adapters.`in`.web.mapper.toCommand
import com.pantryflow.adapters.`in`.web.mapper.toResponse
import com.pantryflow.adapters.`in`.web.uuid
import com.pantryflow.application.port.`in`.ShoppingUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.shoppingRoutes(shopping: ShoppingUseCase) {
    route("/households/{householdId}/shopping-items") {
        get {
            val result = databaseCall { shopping.list(call.uuid("householdId")) }
            call.respond(result.map { it.toResponse() })
        }
        post {
            val command = call.receive<CreateShoppingItemRequest>().toCommand(call.uuid("householdId"))
            call.respond(HttpStatusCode.Created, databaseCall { shopping.add(command) }.toResponse())
        }
        post("/{itemId}/purchase") {
            val command = call.receive<PurchaseRequest>()
                .toCommand(call.uuid("householdId"), call.uuid("itemId"))
            call.respond(databaseCall { shopping.purchase(command) }.toResponse())
        }
        delete("/{itemId}") {
            databaseCall { shopping.delete(call.uuid("householdId"), call.uuid("itemId")) }
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
