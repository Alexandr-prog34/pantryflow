package com.pantryflow

import com.pantryflow.adapters.`in`.web.configureHttp
import com.pantryflow.adapters.`in`.web.controller.UseCases
import com.pantryflow.adapters.`in`.web.controller.configureRoutes
import com.pantryflow.application.port.out.TransactionRunner
import com.pantryflow.application.service.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class HttpRoutesTest {
    @Test
    fun `REST creates data and validates input`() = testApplication {
        val tx = object : TransactionRunner { override fun <T> transaction(block: () -> T) = block() }
        val users = TestUsers()
        val households = TestHouseholds()
        val inventory = InventoryService(TestInventory(), households, tx)
        application {
            configureHttp()
            configureRoutes(UseCases(
                UserService(users, tx), HouseholdService(households, users, tx), inventory,
                ShoppingService(TestShopping(), inventory, households, tx),
            ))
        }

        val openApi = client.get("/openapi.yaml")
        assertEquals(HttpStatusCode.OK, openApi.status)
        assertTrue(openApi.bodyAsText().contains("title: PantryFlow API"))
        val swagger = client.get("/swagger")
        assertEquals(HttpStatusCode.OK, swagger.status)
        assertTrue(swagger.bodyAsText().contains("Swagger UI"))
        assertTrue(swagger.bodyAsText().contains("/swagger-assets@5.20.1/swagger-ui.css"))
        assertTrue(!swagger.bodyAsText().contains("unpkg.com"))
        assertEquals(HttpStatusCode.OK, client.get("/swagger/pantryflow.yaml").status)
        assertEquals(HttpStatusCode.OK, client.get("/swagger-assets@5.20.1/swagger-ui.css").status)
        assertEquals(HttpStatusCode.OK, client.get("/swagger-assets@5.20.1/swagger-ui-bundle.js").status)
        assertEquals(HttpStatusCode.OK, client.get("/swagger-assets@5.20.1/swagger-ui-standalone-preset.js").status)
        assertEquals(HttpStatusCode.OK, client.get("/swagger-assets@5.20.1/favicon-32x32.png").status)

        val user = client.post("/api/v1/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"alex@example.com","displayName":"Alex"}""")
        }
        assertEquals(HttpStatusCode.Created, user.status)
        val userId = Json.parseToJsonElement(user.bodyAsText()).jsonObject.getValue("id").jsonPrimitive.content

        val home = client.post("/api/v1/households") {
            contentType(ContentType.Application.Json)
            setBody("""{"ownerId":"$userId","name":"Home"}""")
        }
        assertEquals(HttpStatusCode.Created, home.status)
        val householdId = Json.parseToJsonElement(home.bodyAsText()).jsonObject.getValue("id").jsonPrimitive.content

        val invalidProduct = client.post("/api/v1/households/$householdId/products") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"Milk","unit":"MILLILITER","minimumQuantity":"-1"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, invalidProduct.status)
        assertEquals("INVALID_INPUT", Json.parseToJsonElement(invalidProduct.bodyAsText())
            .jsonObject.getValue("code").jsonPrimitive.content)

        val missingHome = client.get("/api/v1/households/${UUID.randomUUID()}/products")
        assertEquals(HttpStatusCode.NotFound, missingHome.status)
    }
}
