package com.pantryflow.domain.valueobject

import com.pantryflow.domain.exception.requireInput

@JvmInline
value class DisplayName private constructor(val value: String) {
    companion object {
        fun of(raw: String): DisplayName {
            val normalized = raw.trim()
            requireInput(normalized.length in 2..100, "Display name must contain 2 to 100 characters")
            return DisplayName(normalized)
        }
    }
}