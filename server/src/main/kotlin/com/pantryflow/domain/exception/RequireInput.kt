package com.pantryflow.domain.exception

fun requireInput(condition: Boolean, message: String) {
    if (!condition) throw InvalidInput(message)
}
