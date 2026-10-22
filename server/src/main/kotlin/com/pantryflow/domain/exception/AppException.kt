package com.pantryflow.domain.exception

sealed class AppException(message: String) : RuntimeException(message)
