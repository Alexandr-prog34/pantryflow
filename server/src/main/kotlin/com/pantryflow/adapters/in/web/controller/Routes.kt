package com.pantryflow.adapters.`in`.web.controller

import com.pantryflow.application.port.`in`.*
import io.ktor.server.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import io.ktor.server.response.respondResource
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

data class UseCases(
    val users: UserUseCase,
    val households: HouseholdUseCase,
    val inventory: InventoryUseCase,
    val shopping: ShoppingUseCase,
)

fun Application.configureRoutes(useCases: UseCases) {
    routing {
        get("/health") { call.respondText("ok") }
        val swaggerAssets = setOf(
            "swagger-ui.css", "swagger-ui-bundle.js", "swagger-ui-standalone-preset.js", "favicon-32x32.png",
        )
        get("/swagger-assets@5.20.1/{asset}") {
            val asset = call.parameters["asset"]
            if (asset == null || asset !in swaggerAssets) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            call.respondResource("META-INF/resources/webjars/swagger-ui/5.20.1/$asset")
        }
        swaggerUI(path = "swagger", swaggerFile = "openapi/pantryflow.yaml") {
            version = "5.20.1"
            packageLocation = "/swagger-assets"
            faviconLocation = "/swagger-assets@5.20.1/favicon-32x32.png"
        }
        get("/openapi.yaml") {
            val specification = requireNotNull(
                javaClass.classLoader.getResource("openapi/pantryflow.yaml"),
            ) { "OpenAPI specification is missing" }
            call.respondText(specification.readText(), ContentType.parse("application/yaml"))
        }
        route("/api/v1") {
            userRoutes(useCases.users)
            householdRoutes(useCases.households)
            inventoryRoutes(useCases.inventory)
            shoppingRoutes(useCases.shopping)
        }
    }
}
