package com.pantryflow.application.port.out

import com.pantryflow.domain.model.Product
import com.pantryflow.domain.model.ProductBatch
import java.math.BigDecimal
import java.util.UUID

interface InventoryRepository {
    fun saveProduct(product: Product)
    fun product(householdId: UUID, productId: UUID): Product?
    fun listProducts(householdId: UUID): List<Product>
    fun saveBatch(batch: ProductBatch)
    fun batch(householdId: UUID, batchId: UUID): ProductBatch?
    fun listBatches(householdId: UUID, productId: UUID): List<ProductBatch>
    fun adjustBatch(householdId: UUID, batchId: UUID, delta: BigDecimal): ProductBatch?
}
