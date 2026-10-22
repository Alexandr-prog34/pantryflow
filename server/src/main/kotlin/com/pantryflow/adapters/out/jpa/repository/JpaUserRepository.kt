package com.pantryflow.adapters.out.jpa.repository

import com.pantryflow.adapters.out.jpa.JpaDatabase
import com.pantryflow.adapters.out.jpa.entity.UserJpaEntity
import com.pantryflow.adapters.out.jpa.mapper.toDomain
import com.pantryflow.adapters.out.jpa.mapper.toJpa
import com.pantryflow.application.port.out.UserRepository
import com.pantryflow.domain.model.User
import com.pantryflow.domain.valueobject.Email
import java.util.UUID

class JpaUserRepository(private val db: JpaDatabase) : UserRepository {
    override fun findByEmail(email: Email): User? = db.entityManager()
        .createQuery("select u from UserJpaEntity u where u.email = :email", UserJpaEntity::class.java)
        .setParameter("email", email.value)
        .resultList.firstOrNull()?.toDomain()

    override fun findById(id: UUID): User? = db.entityManager()
        .find(UserJpaEntity::class.java, id)?.toDomain()

    override fun save(user: User) {
        db.entityManager().persist(user.toJpa())
    }
}
