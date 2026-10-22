package com.pantryflow.adapters.`in`.web

import com.pantryflow.adapters.`in`.web.dto.ErrorResponse
import com.pantryflow.adapters.`in`.web.mapper.asUuid
import com.pantryflow.domain.exception.*
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.util.UUID

fun Application.configureHttp() {
    val appLog = log
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = false; encodeDefaults = true })
    }
    install(CallLogging)
    install(StatusPages) {
        exception<AppException> { call, cause ->
            val (status, code) = when (cause) {
                is InvalidInput -> HttpStatusCode.BadRequest to "INVALID_INPUT"
                is NotFound -> HttpStatusCode.NotFound to "NOT_FOUND"
                is Conflict -> HttpStatusCode.Conflict to "CONFLICT"
            }
            call.respond(status, ErrorResponse(code, cause.message ?: code))
        }
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_REQUEST", "Malformed request body"))
        }
        exception<SerializationException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_REQUEST", "Malformed request body"))
        }
        exception<Throwable> { call, cause ->
            appLog.error("Request failed", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("INTERNAL_ERROR", "Internal server error"))
        }
    }
}

fun ApplicationCall.uuid(name: String): UUID = parameters[name]?.asUuid()
    ?: throw InvalidInput("Missing $name")

suspend fun <T> databaseCall(block: () -> T): T = withContext(Dispatchers.IO) { block() }
