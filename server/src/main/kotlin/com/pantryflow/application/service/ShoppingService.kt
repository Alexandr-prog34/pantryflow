package com.pantryflow.application.service

import com.pantryflow.application.dto.*
import com.pantryflow.application.mapper.toData
import com.pantryflow.application.port.`in`.InventoryStock
import com.pantryflow.application.port.`in`.ShoppingUseCase
import com.pantryflow.application.port.out.HouseholdRepository
import com.pantryflow.application.port.out.ShoppingRepository
import com.pantryflow.application.port.out.TransactionRunner
import com.pantryflow.domain.exception.Conflict
import com.pantryflow.domain.exception.NotFound
import com.pantryflow.domain.model.ShoppingItem
import com.pantryflow.domain.enums.ShoppingStatus
import com.pantryflow.domain.valueobject.BatchDates
import com.pantryflow.domain.valueobject.Quantity
import java.util.UUID

class ShoppingService(
    private val shopping: ShoppingRepository,
    private val inventory: InventoryStock,
    private val households: HouseholdRepository,
    private val transactions: TransactionRunner,
) : ShoppingUseCase {
    override fun add(command: AddShoppingItemCommand): ShoppingItemData = transactions.transaction {
        requireHousehold(command.householdId)
        inventory.requireProduct(command.householdId, command.productId)
        val quantity = Quantity.positive(command.quantity)
        val item = ShoppingItem(UUID.randomUUID(), command.householdId, command.productId,
            quantity, ShoppingStatus.OPEN)
        shopping.save(item)
        item.toData()
    }

    override fun list(householdId: UUID): List<ShoppingItemData> = transactions.transaction {
        requireHousehold(householdId)
        shopping.list(householdId).map { it.toData() }
    }

    override fun purchase(command: PurchaseCommand): PurchaseData = transactions.transaction {
        requireHousehold(command.householdId)
        BatchDates(command.purchasedOn, command.expiresOn)
        val item = shopping.markPurchased(command.householdId, command.itemId)
            ?: if (shopping.item(command.householdId, command.itemId) == null) {
                throw NotFound("Shopping item not found")
            } else {
                throw Conflict("Shopping item is already purchased")
            }
        val batch = inventory.addPurchasedBatch(AddBatchCommand(command.householdId, item.productId,
            item.quantity.value, command.storage, command.purchasedOn, command.expiresOn))
        PurchaseData(item.toData(), batch)
    }

    override fun delete(householdId: UUID, itemId: UUID) = transactions.transaction {
        requireHousehold(householdId)
        if (!shopping.deleteOpen(householdId, itemId)) {
            if (shopping.item(householdId, itemId) == null) throw NotFound("Shopping item not found")
            throw Conflict("Purchased item cannot be deleted")
        }
    }

    private fun requireHousehold(householdId: UUID) {
        if (households.findById(householdId) == null) throw NotFound("Household not found")
    }
}
