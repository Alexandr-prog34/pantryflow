package com.pantryflow.domain.valueobject

import com.pantryflow.domain.exception.requireInput

@JvmInline
value class HouseholdName private constructor(val value: String) {
    companion object {
        fun of(raw: String): HouseholdName {
            val normalized = raw.trim()
            requireInput(normalized.length in 2..100, "Household name must contain 2 to 100 characters")
            return HouseholdName(normalized)
        }
    }
}
