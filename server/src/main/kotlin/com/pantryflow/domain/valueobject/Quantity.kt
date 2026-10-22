package com.pantryflow.domain.valueobject

import com.pantryflow.domain.exception.requireInput
import java.math.BigDecimal

@JvmInline
value class Quantity private constructor(val value: BigDecimal) : Comparable<Quantity> {
    override fun compareTo(other: Quantity): Int = value.compareTo(other.value)

    companion object {
        private val MAX = BigDecimal("999999999.999")
        val ZERO = Quantity(BigDecimal.ZERO)

        fun of(value: BigDecimal): Quantity = checked(value, allowZero = true)

        fun positive(value: BigDecimal): Quantity = checked(value, allowZero = false)

        fun total(value: BigDecimal): Quantity {
            requireInput(value.signum() >= 0 && value.scale() <= 3, "Quantity must be non-negative with at most 3 decimals")
            return Quantity(value)
        }

        private fun checked(value: BigDecimal, allowZero: Boolean): Quantity {
            requireInput(
                value <= MAX && value.scale() <= 3 &&
                    (value.signum() > 0 || allowZero && value.signum() == 0),
                "Quantity must be ${if (allowZero) "non-negative" else "positive"} with at most 3 decimals",
            )
            return Quantity(value)
        }
    }
}
