package com.pantryflow.application.dto

import com.pantryflow.domain.enums.ShoppingStatus
import com.pantryflow.domain.enums.Storage
import com.pantryflow.domain.enums.UnitOfMeasure
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class UserData(val id: UUID, val email: String, val displayName: String)
data class HouseholdData(val id: UUID, val name: String, val inviteCode: String)
data class ProductData(
    val id: UUID,
    val householdId: UUID,
    val name: String,
    val category: String?,
    val unit: UnitOfMeasure,
    val minimumQuantity: BigDecimal,
    val currentQuantity: BigDecimal,
)
data class BatchData(
    val id: UUID,
    val householdId: UUID,
    val productId: UUID,
    val quantity: BigDecimal,
    val storage: Storage,
    val purchasedOn: LocalDate?,
    val expiresOn: LocalDate?,
)
data class ShoppingItemData(
    val id: UUID,
    val householdId: UUID,
    val productId: UUID,
    val quantity: BigDecimal,
    val status: ShoppingStatus,
)
data class PurchaseData(val item: ShoppingItemData, val batch: BatchData)
