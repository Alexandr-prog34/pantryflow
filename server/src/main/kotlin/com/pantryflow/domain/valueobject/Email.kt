package com.pantryflow.domain.valueobject

import com.pantryflow.domain.exception.requireInput

@JvmInline
value class Email private constructor(val value: String) {
    companion object {
        private val FORMAT = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        fun of(raw: String): Email {
            val normalized = raw.trim().lowercase()
            requireInput(normalized.length <= 320 && FORMAT.matches(normalized), "Valid email is required")
            return Email(normalized)
        }
    }
}
