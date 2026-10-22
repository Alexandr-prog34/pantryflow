package com.pantryflow.adapters.out.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

private val EMPTY_ID = UUID(0, 0)

@Entity
@Table(name = "users")
class UserJpaEntity(
    @Id var id: UUID = EMPTY_ID,
    @Column(nullable = false) var email: String = "",
    @Column(name = "display_name", nullable = false) var displayName: String = "",
)

@Entity
@Table(name = "households")
class HouseholdJpaEntity(
    @Id var id: UUID = EMPTY_ID,
    @Column(nullable = false) var name: String = "",
    @Column(name = "invite_code", nullable = false) var inviteCode: String = "",
)

@Embeddable
data class MembershipJpaId(
    @Column(name = "household_id") var householdId: UUID = EMPTY_ID,
    @Column(name = "user_id") var userId: UUID = EMPTY_ID,
) : Serializable

@Entity
@Table(name = "household_members")
class MembershipJpaEntity(
    @EmbeddedId var id: MembershipJpaId = MembershipJpaId(),
    @Column(nullable = false) var role: String = "",
)

@Entity
@Table(name = "products")
class ProductJpaEntity(
    @Id var id: UUID = EMPTY_ID,
    @Column(name = "household_id", nullable = false) var householdId: UUID = EMPTY_ID,
    @Column(nullable = false) var name: String = "",
    var category: String? = null,
    @Column(nullable = false) var unit: String = "",
    @Column(name = "minimum_quantity", nullable = false, precision = 12, scale = 3)
    var minimumQuantity: BigDecimal = BigDecimal.ZERO,
)

@Entity
@Table(name = "product_batches")
class ProductBatchJpaEntity(
    @Id var id: UUID = EMPTY_ID,
    @Column(name = "household_id", nullable = false) var householdId: UUID = EMPTY_ID,
    @Column(name = "product_id", nullable = false) var productId: UUID = EMPTY_ID,
    @Column(nullable = false, precision = 12, scale = 3) var quantity: BigDecimal = BigDecimal.ZERO,
    @Column(nullable = false) var storage: String = "",
    @Column(name = "purchased_on") var purchasedOn: LocalDate? = null,
    @Column(name = "expires_on") var expiresOn: LocalDate? = null,
)

@Entity
@Table(name = "shopping_items")
class ShoppingItemJpaEntity(
    @Id var id: UUID = EMPTY_ID,
    @Column(name = "household_id", nullable = false) var householdId: UUID = EMPTY_ID,
    @Column(name = "product_id", nullable = false) var productId: UUID = EMPTY_ID,
    @Column(nullable = false, precision = 12, scale = 3) var quantity: BigDecimal = BigDecimal.ZERO,
    @Column(nullable = false) var status: String = "",
)
