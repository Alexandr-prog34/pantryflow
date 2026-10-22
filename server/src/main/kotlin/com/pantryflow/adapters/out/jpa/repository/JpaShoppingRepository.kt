package com.pantryflow.adapters.out.jpa.repository

import com.pantryflow.adapters.out.jpa.JpaDatabase
import com.pantryflow.adapters.out.jpa.entity.ShoppingItemJpaEntity
import com.pantryflow.adapters.out.jpa.mapper.toDomain
import com.pantryflow.adapters.out.jpa.mapper.toJpa
import com.pantryflow.application.port.out.ShoppingRepository
import com.pantryflow.domain.model.ShoppingItem
import java.util.UUID

class JpaShoppingRepository(private val db: JpaDatabase) : ShoppingRepository {
    override fun save(item: ShoppingItem) {
        db.entityManager().persist(item.toJpa())
    }

    override fun list(householdId: UUID): List<ShoppingItem> = db.entityManager()
        .createQuery(
            "select i from ShoppingItemJpaEntity i where i.householdId = :householdId order by i.id",
            ShoppingItemJpaEntity::class.java,
        )
        .setParameter("householdId", householdId)
        .resultList.map { it.toDomain() }

    override fun item(householdId: UUID, itemId: UUID): ShoppingItem? = db.entityManager()
        .find(ShoppingItemJpaEntity::class.java, itemId)
        ?.takeIf { it.householdId == householdId }?.toDomain()

    override fun markPurchased(householdId: UUID, itemId: UUID): ShoppingItem? {
        val manager = db.entityManager()
        val changed = manager.createNativeQuery(
            "UPDATE shopping_items SET status = 'PURCHASED', purchased_at = now() " +
                "WHERE household_id = :householdId AND id = :itemId AND status = 'OPEN'",
        )
            .setParameter("householdId", householdId)
            .setParameter("itemId", itemId)
            .executeUpdate()
        if (changed == 0) return null
        manager.clear()
        return item(householdId, itemId)
    }

    override fun deleteOpen(householdId: UUID, itemId: UUID): Boolean {
        val manager = db.entityManager()
        val deleted = manager.createNativeQuery(
            "DELETE FROM shopping_items WHERE household_id = :householdId " +
                "AND id = :itemId AND status = 'OPEN'",
        )
            .setParameter("householdId", householdId)
            .setParameter("itemId", itemId)
            .executeUpdate() == 1
        if (deleted) manager.clear()
        return deleted
    }
}
