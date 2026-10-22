package com.pantryflow.adapters.out.jpa.mapper

import com.pantryflow.adapters.out.jpa.entity.*
import com.pantryflow.domain.enums.ShoppingStatus
import com.pantryflow.domain.enums.Storage
import com.pantryflow.domain.enums.UnitOfMeasure
import com.pantryflow.domain.model.*
import com.pantryflow.domain.valueobject.*
import java.math.BigDecimal

fun UserJpaEntity.toDomain() = User(id, Email.of(email), DisplayName.of(displayName))
fun User.toJpa() = UserJpaEntity(id, email.value, displayName.value)

fun HouseholdJpaEntity.toDomain() = Household(id, HouseholdName.of(name), InviteCode.of(inviteCode))
fun Household.toJpa() = HouseholdJpaEntity(id, name.value, inviteCode.value)

fun Membership.toJpa() = MembershipJpaEntity(MembershipJpaId(householdId, userId), role.name)

fun ProductJpaEntity.toDomain(currentQuantity: BigDecimal) = Product(
    id, householdId, ProductName.of(name), category, UnitOfMeasure.valueOf(unit),
    Quantity.of(minimumQuantity), Quantity.total(currentQuantity),
)
fun Product.toJpa() = ProductJpaEntity(id, householdId, name.value, category, unit.name, minimumQuantity.value)

fun ProductBatchJpaEntity.toDomain() = ProductBatch(
    id, householdId, productId, Quantity.of(quantity), Storage.valueOf(storage), BatchDates(purchasedOn, expiresOn),
)
fun ProductBatch.toJpa() = ProductBatchJpaEntity(
    id, householdId, productId, quantity.value, storage.name, dates.purchasedOn, dates.expiresOn,
)

fun ShoppingItemJpaEntity.toDomain() = ShoppingItem(
    id, householdId, productId, Quantity.positive(quantity), ShoppingStatus.valueOf(status),
)
fun ShoppingItem.toJpa() = ShoppingItemJpaEntity(id, householdId, productId, quantity.value, status.name)
