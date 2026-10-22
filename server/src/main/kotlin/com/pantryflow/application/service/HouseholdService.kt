package com.pantryflow.application.service

import com.pantryflow.application.dto.*
import com.pantryflow.application.mapper.toData
import com.pantryflow.application.port.`in`.HouseholdUseCase
import com.pantryflow.application.port.out.HouseholdRepository
import com.pantryflow.application.port.out.TransactionRunner
import com.pantryflow.application.port.out.UserRepository
import com.pantryflow.domain.exception.Conflict
import com.pantryflow.domain.exception.NotFound
import com.pantryflow.domain.enums.MemberRole
import com.pantryflow.domain.model.*
import com.pantryflow.domain.valueobject.HouseholdName
import com.pantryflow.domain.valueobject.InviteCode
import java.security.SecureRandom
import java.util.UUID

class HouseholdService(
    private val households: HouseholdRepository,
    private val users: UserRepository,
    private val transactions: TransactionRunner,
) : HouseholdUseCase {
    private val random = SecureRandom()

    override fun create(command: CreateHouseholdCommand): HouseholdData = transactions.transaction {
        if (users.findById(command.ownerId) == null) throw NotFound("User not found")
        val name = HouseholdName.of(command.name)
        val code = InviteCode.generate(random)
        val household = Household(UUID.randomUUID(), name, code)
        households.save(household)
        households.addMember(Membership(household.id, command.ownerId, MemberRole.OWNER))
        household.toData()
    }

    override fun join(command: JoinHouseholdCommand): HouseholdData = transactions.transaction {
        if (users.findById(command.userId) == null) throw NotFound("User not found")
        val code = InviteCode.parseOrNull(command.inviteCode) ?: throw NotFound("Invite code not found")
        val household = households.findByInviteCode(code)
            ?: throw NotFound("Invite code not found")
        if (!households.addMember(Membership(household.id, command.userId, MemberRole.MEMBER))) {
            throw Conflict("Already a member")
        }
        household.toData()
    }

    override fun listForUser(userId: UUID): List<HouseholdData> = transactions.transaction {
        if (users.findById(userId) == null) throw NotFound("User not found")
        households.listForUser(userId).map { it.toData() }
    }

}
