package com.pantryflow.application.port.`in`

import com.pantryflow.application.dto.AddShoppingItemCommand
import com.pantryflow.application.dto.PurchaseCommand
import com.pantryflow.application.dto.PurchaseData
import com.pantryflow.application.dto.ShoppingItemData
import java.util.UUID

interface ShoppingUseCase {
    fun add(command: AddShoppingItemCommand): ShoppingItemData
    fun list(householdId: UUID): List<ShoppingItemData>
    fun purchase(command: PurchaseCommand): PurchaseData
    fun delete(householdId: UUID, itemId: UUID)
}
