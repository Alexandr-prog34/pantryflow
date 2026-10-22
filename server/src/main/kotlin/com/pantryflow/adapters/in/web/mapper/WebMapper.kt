package com.pantryflow.adapters.`in`.web.mapper

import com.pantryflow.adapters.`in`.web.dto.*
import com.pantryflow.application.dto.*
import com.pantryflow.domain.exception.InvalidInput
import com.pantryflow.domain.enums.Storage
import com.pantryflow.domain.enums.UnitOfMeasure
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

fun CreateUserRequest.toCommand() = CreateUserCommand(email, displayName)
fun CreateHouseholdRequest.toCommand() = CreateHouseholdCommand(ownerId.asUuid(), name)
fun JoinHouseholdRequest.toCommand() = JoinHouseholdCommand(userId.asUuid(), inviteCode)
fun CreateProductRequest.toCommand(householdId: UUID) = CreateProductCommand(
    householdId, name, category, unit.asUnit(), minimumQuantity.asQuantity(),
)
fun CreateBatchRequest.toCommand(householdId: UUID, productId: UUID) = AddBatchCommand(
    householdId, productId, quantity.asQuantity(), storage.asStorage(), purchasedOn.asDate(), expiresOn.asDate(),
)
fun CreateShoppingItemRequest.toCommand(householdId: UUID) = AddShoppingItemCommand(
    householdId, productId.asUuid(), quantity.asQuantity(),
)
fun PurchaseRequest.toCommand(householdId: UUID, itemId: UUID) = PurchaseCommand(
    householdId, itemId, storage.asStorage(), purchasedOn.asDate(), expiresOn.asDate(),
)

fun UserData.toResponse() = UserResponse(id.toString(), email, displayName)
fun HouseholdData.toResponse() = HouseholdResponse(id.toString(), name, inviteCode)
fun ProductData.toResponse() = ProductResponse(
    id.toString(), householdId.toString(), name, category, unit.name,
    minimumQuantity.toPlainString(), currentQuantity.toPlainString(),
)
fun BatchData.toResponse() = BatchResponse(
    id.toString(), householdId.toString(), productId.toString(), quantity.toPlainString(),
    storage.name, purchasedOn?.toString(), expiresOn?.toString(),
)
fun ShoppingItemData.toResponse() = ShoppingItemResponse(
    id.toString(), householdId.toString(), productId.toString(), quantity.toPlainString(), status.name,
)
fun PurchaseData.toResponse() = PurchaseResponse(item.toResponse(), batch.toResponse())

fun String.asUuid(): UUID = runCatching { UUID.fromString(this) }
    .getOrElse { throw InvalidInput("Invalid UUID") }
fun String.asQuantity(): BigDecimal = runCatching { BigDecimal(this) }
    .getOrElse { throw InvalidInput("Invalid quantity") }
private fun String?.asDate(): LocalDate? = this?.let {
    runCatching { LocalDate.parse(it) }.getOrElse { throw InvalidInput("Invalid date, expected YYYY-MM-DD") }
}
private fun String.asUnit(): UnitOfMeasure = enumValues<UnitOfMeasure>()
    .firstOrNull { it.name == uppercase() } ?: throw InvalidInput("Invalid unit")
private fun String.asStorage(): Storage = enumValues<Storage>()
    .firstOrNull { it.name == uppercase() } ?: throw InvalidInput("Invalid storage")
