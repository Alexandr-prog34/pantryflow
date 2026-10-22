package com.pantryflow.application.port.`in`

import com.pantryflow.application.dto.CreateUserCommand
import com.pantryflow.application.dto.UserData
import java.util.UUID

interface UserUseCase {
    fun create(command: CreateUserCommand): UserData
    fun get(userId: UUID): UserData
}
