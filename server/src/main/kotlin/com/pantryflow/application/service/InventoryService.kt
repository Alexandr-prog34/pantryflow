package com.pantryflow.application.service

import com.pantryflow.application.dto.*
import com.pantryflow.application.mapper.toData
import com.pantryflow.application.port.`in`.InventoryStock
import com.pantryflow.application.port.`in`.InventoryUseCase
import com.pantryflow.application.port.out.HouseholdRepository
import com.pantryflow.application.port.out.InventoryRepository
import com.pantryflow.application.port.out.TransactionRunner
import com.pantryflow.domain.exception.Conflict
import com.pantryflow.domain.exception.NotFound
import com.pantryflow.domain.exception.requireInput
import com.pantryflow.domain.model.Product
import com.pantryflow.domain.model.ProductBatch
import com.pantryflow.domain.valueobject.BatchDates
import com.pantryflow.domain.valueobject.ProductName
import com.pantryflow.domain.valueobject.Quantity
import java.math.BigDecimal
import java.util.UUID

class InventoryService(
    private val inventory: InventoryRepository,
    private val households: HouseholdRepository,
    private val transactions: TransactionRunner,
) : InventoryUseCase, InventoryStock {
    override fun createProduct(command: CreateProductCommand): ProductData = transactions.transaction {
        requireHousehold(command.householdId)
        val name = ProductName.of(command.name)
        val category = command.category?.trim()?.takeIf { it.isNotEmpty() }
        requireInput(category == null || category.length <= 80, "Category is too long")
        val minimumQuantity = Quantity.of(command.minimumQuantity)
        val product = Product(UUID.randomUUID(), command.householdId, name, category, command.unit, minimumQuantity)
        inventory.saveProduct(product)
        product.toData()
    }

    override fun listProducts(householdId: UUID): List<ProductData> = transactions.transaction {
        requireHousehold(householdId)
        inventory.listProducts(householdId).map { it.toData() }
    }

    override fun lowStock(householdId: UUID): List<ProductData> = listProducts(householdId)
        .filter { it.currentQuantity < it.minimumQuantity }

    override fun addBatch(command: AddBatchCommand): BatchData = addPurchasedBatch(command)

    override fun requireProduct(householdId: UUID, productId: UUID) {
        requireHousehold(householdId)
        if (inventory.product(householdId, productId) == null) throw NotFound("Product not found")
    }

    override fun addPurchasedBatch(command: AddBatchCommand): BatchData = transactions.transaction {
        requireProduct(command.householdId, command.productId)
        val quantity = Quantity.positive(command.quantity)
        val dates = BatchDates(command.purchasedOn, command.expiresOn)
        val batch = ProductBatch(UUID.randomUUID(), command.householdId, command.productId,
            quantity, command.storage, dates)
        inventory.saveBatch(batch)
        batch.toData()
    }

    override fun listBatches(householdId: UUID, productId: UUID): List<BatchData> = transactions.transaction {
        requireProduct(householdId, productId)
        inventory.listBatches(householdId, productId).map { it.toData() }
    }

    override fun adjustBatch(householdId: UUID, batchId: UUID, delta: BigDecimal): BatchData = transactions.transaction {
        requireHousehold(householdId)
        Quantity.positive(delta.abs())
        val changed = inventory.adjustBatch(householdId, batchId, delta)
            ?: if (inventory.batch(householdId, batchId) == null) throw NotFound("Batch not found")
            else throw Conflict("Quantity must stay within the allowed range")
        changed.toData()
    }

    private fun requireHousehold(householdId: UUID) {
        if (households.findById(householdId) == null) throw NotFound("Household not found")
    }
}
