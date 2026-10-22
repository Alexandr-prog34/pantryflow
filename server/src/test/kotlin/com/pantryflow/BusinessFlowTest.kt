package com.pantryflow

import com.pantryflow.application.dto.*
import com.pantryflow.application.port.out.*
import com.pantryflow.application.service.*
import com.pantryflow.domain.exception.Conflict
import com.pantryflow.domain.exception.InvalidInput
import com.pantryflow.domain.exception.NotFound
import com.pantryflow.domain.enums.ShoppingStatus
import com.pantryflow.domain.enums.Storage
import com.pantryflow.domain.enums.UnitOfMeasure
import com.pantryflow.domain.model.*
import com.pantryflow.domain.valueobject.Email
import com.pantryflow.domain.valueobject.InviteCode
import com.pantryflow.domain.valueobject.Quantity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class BusinessFlowTest {
    private val tx = object : TransactionRunner { override fun <T> transaction(block: () -> T) = block() }
    private val users = TestUsers()
    private val userService = UserService(users, tx)
    private val householdRepository = TestHouseholds()
    private val households = HouseholdService(householdRepository, users, tx)
    private val inventoryRepository = TestInventory()
    private val inventory = InventoryService(inventoryRepository, householdRepository, tx)
    private val shopping = ShoppingService(TestShopping(), inventory, householdRepository, tx)

    @Test
    fun `two members share stock and purchase completes once`() {
        val owner = userService.create(CreateUserCommand("owner@example.com", "Owner"))
        val member = userService.create(CreateUserCommand("member@example.com", "Member"))
        val home = households.create(CreateHouseholdCommand(owner.id, "Home"))
        households.join(JoinHouseholdCommand(member.id, home.inviteCode))
        assertEquals(listOf(home), households.listForUser(member.id))

        val milk = inventory.createProduct(CreateProductCommand(home.id, "Milk", "Dairy", UnitOfMeasure.MILLILITER, bd("1000")))
        assertEquals(1, inventory.lowStock(home.id).size)
        val item = shopping.add(AddShoppingItemCommand(home.id, milk.id, bd("1500")))
        val purchase = shopping.purchase(PurchaseCommand(home.id, item.id, Storage.FRIDGE, null, null))

        assertEquals(ShoppingStatus.PURCHASED, purchase.item.status)
        assertEquals(bd("1500"), inventory.listProducts(home.id).single().currentQuantity)
        assertTrue(inventory.lowStock(home.id).isEmpty())
        assertThrows(Conflict::class.java) {
            shopping.purchase(PurchaseCommand(home.id, item.id, Storage.FRIDGE, null, null))
        }
        assertEquals(1, inventory.listBatches(home.id, milk.id).size)
    }

    @Test
    fun `invalid references do not create data`() {
        val owner = userService.create(CreateUserCommand("owner@example.com", "Owner"))
        val home = households.create(CreateHouseholdCommand(owner.id, "Home"))
        assertThrows(NotFound::class.java) {
            inventory.createProduct(CreateProductCommand(UUID.randomUUID(), "Milk", null, UnitOfMeasure.PIECE, bd("1")))
        }
        assertThrows(NotFound::class.java) {
            shopping.add(AddShoppingItemCommand(home.id, UUID.randomUUID(), bd("1")))
        }
        assertTrue(inventory.listProducts(home.id).isEmpty())
    }

    @Test
    fun `quantity cannot become negative and expiry cannot precede purchase`() {
        val owner = userService.create(CreateUserCommand("owner@example.com", "Owner"))
        val home = households.create(CreateHouseholdCommand(owner.id, "Home"))
        val eggs = inventory.createProduct(CreateProductCommand(home.id, "Eggs", null, UnitOfMeasure.PIECE, bd("6")))
        val batch = inventory.addBatch(AddBatchCommand(home.id, eggs.id, bd("4"), Storage.FRIDGE, null, null))
        assertThrows(Conflict::class.java) { inventory.adjustBatch(home.id, batch.id, bd("-5")) }
        assertEquals(bd("4"), inventory.listBatches(home.id, eggs.id).single().quantity)
        assertThrows(InvalidInput::class.java) {
            inventory.addBatch(AddBatchCommand(home.id, eggs.id, bd("1"), Storage.FRIDGE,
                LocalDate.parse("2026-10-10"), LocalDate.parse("2026-10-09")))
        }
    }

    private fun bd(value: String) = BigDecimal(value)
}

internal class TestUsers : UserRepository {
    private val users = mutableMapOf<UUID, User>()
    override fun findByEmail(email: Email) = users.values.firstOrNull { it.email == email }
    override fun findById(id: UUID) = users[id]
    override fun save(user: User) { users[user.id] = user }
}

internal class TestHouseholds : HouseholdRepository {
    private val homes = mutableMapOf<UUID, Household>()
    private val members = mutableMapOf<Pair<UUID, UUID>, Membership>()
    override fun save(household: Household) { homes[household.id] = household }
    override fun findById(id: UUID) = homes[id]
    override fun findByInviteCode(code: InviteCode) = homes.values.firstOrNull { it.inviteCode == code }
    override fun listForUser(userId: UUID) = homes.values.filter { members.containsKey(it.id to userId) }
    override fun addMember(membership: Membership) = members.putIfAbsent(membership.householdId to membership.userId, membership) == null
}

internal class TestInventory : InventoryRepository {
    private val products = mutableMapOf<UUID, Product>()
    private val batches = mutableMapOf<UUID, ProductBatch>()
    override fun saveProduct(product: Product) { products[product.id] = product }
    override fun product(householdId: UUID, productId: UUID) = products[productId]
        ?.takeIf { it.householdId == householdId }?.withQuantity()
    override fun listProducts(householdId: UUID) = products.values
        .filter { it.householdId == householdId }.map { it.withQuantity() }
    override fun saveBatch(batch: ProductBatch) { batches[batch.id] = batch }
    override fun batch(householdId: UUID, batchId: UUID) = batches[batchId]
        ?.takeIf { it.householdId == householdId }
    override fun listBatches(householdId: UUID, productId: UUID) = batches.values
        .filter { it.householdId == householdId && it.productId == productId }
    override fun adjustBatch(householdId: UUID, batchId: UUID, delta: BigDecimal): ProductBatch? {
        val old = batch(householdId, batchId) ?: return null
        val newQuantity = old.quantity.value + delta
        if (newQuantity < BigDecimal.ZERO || newQuantity > BigDecimal("999999999.999")) return null
        return old.copy(quantity = Quantity.of(newQuantity)).also { batches[batchId] = it }
    }
    private fun Product.withQuantity() = copy(currentQuantity = Quantity.total(batches.values.filter { it.productId == id }
        .fold(BigDecimal.ZERO) { total, batch -> total + batch.quantity.value }))
}

internal class TestShopping : ShoppingRepository {
    private val items = mutableMapOf<UUID, ShoppingItem>()
    override fun save(item: ShoppingItem) { items[item.id] = item }
    override fun list(householdId: UUID) = items.values.filter { it.householdId == householdId }
    override fun item(householdId: UUID, itemId: UUID) = items[itemId]?.takeIf { it.householdId == householdId }
    override fun markPurchased(householdId: UUID, itemId: UUID): ShoppingItem? {
        val old = item(householdId, itemId)?.takeIf { it.status == ShoppingStatus.OPEN } ?: return null
        return old.copy(status = ShoppingStatus.PURCHASED).also { items[itemId] = it }
    }
    override fun deleteOpen(householdId: UUID, itemId: UUID) = item(householdId, itemId)
        ?.takeIf { it.status == ShoppingStatus.OPEN }?.let { items.remove(itemId); true } ?: false
}
