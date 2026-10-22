package com.pantryflow.application.service

import com.pantryflow.application.dto.CreateUserCommand
import com.pantryflow.application.dto.UserData
import com.pantryflow.application.mapper.toData
import com.pantryflow.application.port.`in`.UserUseCase
import com.pantryflow.application.port.out.TransactionRunner
import com.pantryflow.application.port.out.UserRepository
import com.pantryflow.domain.exception.Conflict
import com.pantryflow.domain.exception.NotFound
import com.pantryflow.domain.model.User
import com.pantryflow.domain.valueobject.DisplayName
import com.pantryflow.domain.valueobject.Email
import java.util.UUID

class UserService(
    private val users: UserRepository,
    private val transactions: TransactionRunner,
) : UserUseCase {
    override fun create(command: CreateUserCommand): UserData = transactions.transaction {
        val email = Email.of(command.email)
        val name = DisplayName.of(command.displayName)
        if (users.findByEmail(email) != null) throw Conflict("Email is already registered")
        User(UUID.randomUUID(), email, name).also(users::save).toData()
    }

    override fun get(userId: UUID): UserData = transactions.transaction {
        (users.findById(userId) ?: throw NotFound("User not found")).toData()
    }

}
