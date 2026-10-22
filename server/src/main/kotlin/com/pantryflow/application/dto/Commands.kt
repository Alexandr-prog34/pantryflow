package com.pantryflow.application.dto

import com.pantryflow.domain.enums.Storage
import com.pantryflow.domain.enums.UnitOfMeasure
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class CreateUserCommand(val email: String, val displayName: String)
data class CreateHouseholdCommand(val ownerId: UUID, val name: String)
data class JoinHouseholdCommand(val userId: UUID, val inviteCode: String)
data class CreateProductCommand(
    val householdId: UUID,
    val name: String,
    val category: String?,
    val unit: UnitOfMeasure,
    val minimumQuantity: BigDecimal,
)
data class AddBatchCommand(
    val householdId: UUID,
    val productId: UUID,
    val quantity: BigDecimal,
    val storage: Storage,
    val purchasedOn: LocalDate?,
    val expiresOn: LocalDate?,
)
data class AddShoppingItemCommand(val householdId: UUID, val productId: UUID, val quantity: BigDecimal)
data class PurchaseCommand(
    val householdId: UUID,
    val itemId: UUID,
    val storage: Storage,
    val purchasedOn: LocalDate?,
    val expiresOn: LocalDate?,
)
