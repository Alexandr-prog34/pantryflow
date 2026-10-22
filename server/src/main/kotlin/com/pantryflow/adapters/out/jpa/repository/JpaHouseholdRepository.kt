package com.pantryflow.adapters.out.jpa.repository

import com.pantryflow.adapters.out.jpa.JpaDatabase
import com.pantryflow.adapters.out.jpa.entity.HouseholdJpaEntity
import com.pantryflow.adapters.out.jpa.entity.MembershipJpaEntity
import com.pantryflow.adapters.out.jpa.entity.MembershipJpaId
import com.pantryflow.adapters.out.jpa.mapper.toDomain
import com.pantryflow.adapters.out.jpa.mapper.toJpa
import com.pantryflow.application.port.out.HouseholdRepository
import com.pantryflow.domain.model.Household
import com.pantryflow.domain.model.Membership
import com.pantryflow.domain.valueobject.InviteCode
import java.util.UUID

class JpaHouseholdRepository(private val db: JpaDatabase) : HouseholdRepository {
    override fun save(household: Household) {
        db.entityManager().persist(household.toJpa())
    }

    override fun findById(id: UUID): Household? = db.entityManager()
        .find(HouseholdJpaEntity::class.java, id)?.toDomain()

    override fun findByInviteCode(code: InviteCode): Household? = db.entityManager()
        .createQuery("select h from HouseholdJpaEntity h where h.inviteCode = :code", HouseholdJpaEntity::class.java)
        .setParameter("code", code.value)
        .resultList.firstOrNull()?.toDomain()

    override fun listForUser(userId: UUID): List<Household> = db.entityManager()
        .createQuery(
            "select h from HouseholdJpaEntity h where h.id in " +
                "(select m.id.householdId from MembershipJpaEntity m where m.id.userId = :userId) order by h.name",
            HouseholdJpaEntity::class.java,
        )
        .setParameter("userId", userId)
        .resultList.map { it.toDomain() }

    override fun addMember(membership: Membership): Boolean {
        val manager = db.entityManager()
        if (manager.find(MembershipJpaEntity::class.java,
                MembershipJpaId(membership.householdId, membership.userId)) != null) return false
        manager.persist(membership.toJpa())
        return true
    }
}
