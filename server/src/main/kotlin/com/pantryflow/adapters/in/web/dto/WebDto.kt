package com.pantryflow.adapters.`in`.web.dto

import kotlinx.serialization.Serializable

@Serializable data class ErrorResponse(val code: String, val message: String)

@Serializable data class CreateUserRequest(val email: String, val displayName: String)
@Serializable data class UserResponse(val id: String, val email: String, val displayName: String)

@Serializable data class CreateHouseholdRequest(val ownerId: String, val name: String)
@Serializable data class JoinHouseholdRequest(val userId: String, val inviteCode: String)
@Serializable data class HouseholdResponse(val id: String, val name: String, val inviteCode: String)

@Serializable data class CreateProductRequest(
    val name: String,
    val category: String? = null,
    val unit: String,
    val minimumQuantity: String = "0",
)
@Serializable data class ProductResponse(
    val id: String,
    val householdId: String,
    val name: String,
    val category: String?,
    val unit: String,
    val minimumQuantity: String,
    val currentQuantity: String,
)
@Serializable data class CreateBatchRequest(
    val quantity: String,
    val storage: String,
    val purchasedOn: String? = null,
    val expiresOn: String? = null,
)
@Serializable data class AdjustBatchRequest(val delta: String)
@Serializable data class BatchResponse(
    val id: String,
    val householdId: String,
    val productId: String,
    val quantity: String,
    val storage: String,
    val purchasedOn: String?,
    val expiresOn: String?,
)

@Serializable data class CreateShoppingItemRequest(val productId: String, val quantity: String)
@Serializable data class PurchaseRequest(
    val storage: String,
    val purchasedOn: String? = null,
    val expiresOn: String? = null,
)
@Serializable data class ShoppingItemResponse(
    val id: String,
    val householdId: String,
    val productId: String,
    val quantity: String,
    val status: String,
)
@Serializable data class PurchaseResponse(val item: ShoppingItemResponse, val batch: BatchResponse)
