package com.pantryflow.application.port.out

import com.pantryflow.domain.model.Household
import com.pantryflow.domain.model.Membership
import com.pantryflow.domain.valueobject.InviteCode
import java.util.UUID

interface HouseholdRepository {
    fun save(household: Household)
    fun findById(id: UUID): Household?
    fun findByInviteCode(code: InviteCode): Household?
    fun listForUser(userId: UUID): List<Household>
    fun addMember(membership: Membership): Boolean
}
