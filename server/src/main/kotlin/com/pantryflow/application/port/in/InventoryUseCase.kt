package com.pantryflow.application.port.`in`

import com.pantryflow.application.dto.AddBatchCommand
import com.pantryflow.application.dto.BatchData
import com.pantryflow.application.dto.CreateProductCommand
import com.pantryflow.application.dto.ProductData
import java.math.BigDecimal
import java.util.UUID

interface InventoryUseCase {
    fun createProduct(command: CreateProductCommand): ProductData
    fun listProducts(householdId: UUID): List<ProductData>
    fun lowStock(householdId: UUID): List<ProductData>
    fun addBatch(command: AddBatchCommand): BatchData
    fun listBatches(householdId: UUID, productId: UUID): List<BatchData>
    fun adjustBatch(householdId: UUID, batchId: UUID, delta: BigDecimal): BatchData
}

interface InventoryStock {
    fun requireProduct(householdId: UUID, productId: UUID)
    fun addPurchasedBatch(command: AddBatchCommand): BatchData
}
