package com.pantryflow.domain.model

import com.pantryflow.domain.enums.Storage
import com.pantryflow.domain.valueobject.BatchDates
import com.pantryflow.domain.valueobject.Quantity
import java.util.UUID

data class ProductBatch(
    val id: UUID,
    val householdId: UUID,
    val productId: UUID,
    val quantity: Quantity,
    val storage: Storage,
    val dates: BatchDates,
)
