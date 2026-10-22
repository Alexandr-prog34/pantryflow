package com.pantryflow.adapters.`in`.web.controller

import com.pantryflow.adapters.`in`.web.databaseCall
import com.pantryflow.adapters.`in`.web.dto.AdjustBatchRequest
import com.pantryflow.adapters.`in`.web.dto.CreateBatchRequest
import com.pantryflow.adapters.`in`.web.dto.CreateProductRequest
import com.pantryflow.adapters.`in`.web.mapper.asQuantity
import com.pantryflow.adapters.`in`.web.mapper.toCommand
import com.pantryflow.adapters.`in`.web.mapper.toResponse
import com.pantryflow.adapters.`in`.web.uuid
import com.pantryflow.application.port.`in`.InventoryUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.inventoryRoutes(inventory: InventoryUseCase) {
    route("/households/{householdId}") {
        get("/products") {
            val result = databaseCall { inventory.listProducts(call.uuid("householdId")) }
            call.respond(result.map { it.toResponse() })
        }
        get("/products/low-stock") {
            val result = databaseCall { inventory.lowStock(call.uuid("householdId")) }
            call.respond(result.map { it.toResponse() })
        }
        post("/products") {
            val command = call.receive<CreateProductRequest>().toCommand(call.uuid("householdId"))
            call.respond(HttpStatusCode.Created, databaseCall { inventory.createProduct(command) }.toResponse())
        }
        get("/products/{productId}/batches") {
            val result = databaseCall { inventory.listBatches(call.uuid("householdId"), call.uuid("productId")) }
            call.respond(result.map { it.toResponse() })
        }
        post("/products/{productId}/batches") {
            val command = call.receive<CreateBatchRequest>()
                .toCommand(call.uuid("householdId"), call.uuid("productId"))
            call.respond(HttpStatusCode.Created, databaseCall { inventory.addBatch(command) }.toResponse())
        }
        patch("/batches/{batchId}/quantity") {
            val delta = call.receive<AdjustBatchRequest>().delta.asQuantity()
            val result = databaseCall {
                inventory.adjustBatch(call.uuid("householdId"), call.uuid("batchId"), delta)
            }
            call.respond(result.toResponse())
        }
    }
}
