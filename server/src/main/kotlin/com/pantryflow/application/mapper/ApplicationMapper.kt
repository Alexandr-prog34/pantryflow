package com.pantryflow.application.mapper

import com.pantryflow.application.dto.*
import com.pantryflow.domain.model.*

fun User.toData() = UserData(id, email.value, displayName.value)
fun Household.toData() = HouseholdData(id, name.value, inviteCode.value)
fun Product.toData() = ProductData(id, householdId, name.value, category, unit, minimumQuantity.value, currentQuantity.value)
fun ProductBatch.toData() = BatchData(id, householdId, productId, quantity.value, storage, dates.purchasedOn, dates.expiresOn)
fun ShoppingItem.toData() = ShoppingItemData(id, householdId, productId, quantity.value, status)
