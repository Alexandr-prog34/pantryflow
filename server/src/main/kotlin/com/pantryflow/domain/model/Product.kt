package com.pantryflow.domain.model

import com.pantryflow.domain.enums.UnitOfMeasure
import com.pantryflow.domain.valueobject.ProductName
import com.pantryflow.domain.valueobject.Quantity
import java.util.UUID

data class Product(
    val id: UUID,
    val householdId: UUID,
    val name: ProductName,
    val category: String?,
    val unit: UnitOfMeasure,
    val minimumQuantity: Quantity,
    val currentQuantity: Quantity = Quantity.ZERO,
)
