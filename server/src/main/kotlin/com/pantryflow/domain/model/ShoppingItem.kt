package com.pantryflow.domain.model

import com.pantryflow.domain.enums.ShoppingStatus
import com.pantryflow.domain.valueobject.Quantity
import java.util.UUID

data class ShoppingItem(
    val id: UUID,
    val householdId: UUID,
    val productId: UUID,
    val quantity: Quantity,
    val status: ShoppingStatus,
)
