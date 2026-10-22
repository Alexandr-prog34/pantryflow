package com.pantryflow.application.port.out

import com.pantryflow.domain.model.User
import com.pantryflow.domain.valueobject.Email
import java.util.UUID

interface UserRepository {
    fun findByEmail(email: Email): User?
    fun findById(id: UUID): User?
    fun save(user: User)
}
