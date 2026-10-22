package com.pantryflow.adapters.out.jpa.repository

import com.pantryflow.adapters.out.jpa.JpaDatabase
import com.pantryflow.adapters.out.jpa.entity.ProductBatchJpaEntity
import com.pantryflow.adapters.out.jpa.entity.ProductJpaEntity
import com.pantryflow.adapters.out.jpa.mapper.toDomain
import com.pantryflow.adapters.out.jpa.mapper.toJpa
import com.pantryflow.application.port.out.InventoryRepository
import com.pantryflow.domain.model.Product
import com.pantryflow.domain.model.ProductBatch
import java.math.BigDecimal
import java.util.UUID

class JpaInventoryRepository(private val db: JpaDatabase) : InventoryRepository {
    override fun saveProduct(product: Product) {
        db.entityManager().persist(product.toJpa())
    }

    override fun product(householdId: UUID, productId: UUID): Product? {
        val entity = db.entityManager().find(ProductJpaEntity::class.java, productId)
            ?.takeIf { it.householdId == householdId } ?: return null
        return entity.toDomain(totalQuantity(productId))
    }

    override fun listProducts(householdId: UUID): List<Product> = db.entityManager()
        .createQuery(
            "select p from ProductJpaEntity p where p.householdId = :householdId order by p.name, p.id",
            ProductJpaEntity::class.java,
        )
        .setParameter("householdId", householdId)
        .resultList.map { it.toDomain(totalQuantity(it.id)) }

    override fun saveBatch(batch: ProductBatch) {
        db.entityManager().persist(batch.toJpa())
    }

    override fun batch(householdId: UUID, batchId: UUID): ProductBatch? = db.entityManager()
        .find(ProductBatchJpaEntity::class.java, batchId)
        ?.takeIf { it.householdId == householdId }?.toDomain()

    override fun listBatches(householdId: UUID, productId: UUID): List<ProductBatch> = db.entityManager()
        .createQuery(
            "select b from ProductBatchJpaEntity b where b.householdId = :householdId " +
                "and b.productId = :productId order by b.expiresOn nulls last, b.id",
            ProductBatchJpaEntity::class.java,
        )
        .setParameter("householdId", householdId)
        .setParameter("productId", productId)
        .resultList.map { it.toDomain() }

    override fun adjustBatch(householdId: UUID, batchId: UUID, delta: BigDecimal): ProductBatch? {
        val manager = db.entityManager()
        val changed = manager.createNativeQuery(
            "UPDATE product_batches SET quantity = quantity + :delta " +
                "WHERE household_id = :householdId AND id = :batchId " +
                "AND quantity + :delta BETWEEN 0 AND 999999999.999",
        )
            .setParameter("delta", delta)
            .setParameter("householdId", householdId)
            .setParameter("batchId", batchId)
            .executeUpdate()
        if (changed == 0) return null
        manager.clear() // A bulk update bypasses entities already held by the EntityManager.
        return batch(householdId, batchId)
    }

    private fun totalQuantity(productId: UUID): BigDecimal = db.entityManager()
        .createNativeQuery("SELECT COALESCE(SUM(quantity), 0) FROM product_batches WHERE product_id = :productId")
        .setParameter("productId", productId)
        .singleResult as BigDecimal
}
