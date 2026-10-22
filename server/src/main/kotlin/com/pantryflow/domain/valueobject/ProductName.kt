package com.pantryflow.domain.valueobject

import com.pantryflow.domain.exception.requireInput

@JvmInline
value class ProductName private constructor(val value: String) {
    companion object {
        fun of(raw: String): ProductName {
            val normalized = raw.trim()
            requireInput(normalized.length in 1..120, "Product name must contain 1 to 120 characters")
            return ProductName(normalized)
        }
    }
}
