package com.pantryflow.domain.valueobject

import com.pantryflow.domain.exception.requireInput
import java.time.LocalDate

data class BatchDates(val purchasedOn: LocalDate?, val expiresOn: LocalDate?) {
    init {
        requireInput(
            purchasedOn == null || expiresOn == null || !expiresOn.isBefore(purchasedOn),
            "Expiry date cannot precede purchase date",
        )
    }
}
