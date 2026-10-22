package com.pantryflow.application.port.out

import com.pantryflow.domain.model.ShoppingItem
import java.util.UUID

interface ShoppingRepository {
    fun save(item: ShoppingItem)
    fun list(householdId: UUID): List<ShoppingItem>
    fun item(householdId: UUID, itemId: UUID): ShoppingItem?
    fun markPurchased(householdId: UUID, itemId: UUID): ShoppingItem?
    fun deleteOpen(householdId: UUID, itemId: UUID): Boolean
}
