package com.pantryflow.adapters.out.jpa

import com.pantryflow.application.port.out.TransactionRunner
import com.pantryflow.domain.exception.Conflict
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManager
import jakarta.persistence.Persistence
import org.flywaydb.core.Flyway
import java.sql.SQLException

class JpaDatabase(url: String, username: String, password: String) : TransactionRunner, AutoCloseable {
    private val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = url
        this.username = username
        this.password = password
        maximumPoolSize = 10
        poolName = "pantryflow-db"
    })

    private val entityManagerFactoryLazy = lazy {
        Persistence.createEntityManagerFactory(
            "pantryflow",
            mapOf("jakarta.persistence.nonJtaDataSource" to dataSource),
        )
    }
    private val entityManagerFactory by entityManagerFactoryLazy
    private val current = ThreadLocal<EntityManager>()

    fun migrate() {
        Flyway.configure().dataSource(dataSource).load().migrate()
        entityManagerFactory
    }

    override fun <T> transaction(block: () -> T): T {
        if (current.get() != null) return block()
        val manager = entityManagerFactory.createEntityManager()
        current.set(manager)
        return try {
            manager.transaction.begin()
            val result = block()
            manager.transaction.commit()
            result
        } catch (error: Throwable) {
            if (manager.transaction.isActive) manager.transaction.rollback()
            if (error.hasUniqueConstraintViolation()) throw Conflict("Resource already exists")
            throw error
        } finally {
            current.remove()
            manager.close()
        }
    }

    fun entityManager(): EntityManager = checkNotNull(current.get()) {
        "JPA repository must be called inside a transaction"
    }

    override fun close() {
        if (entityManagerFactoryLazy.isInitialized()) entityManagerFactory.close()
        dataSource.close()
    }
}

private fun Throwable.hasUniqueConstraintViolation(): Boolean {
    var cause: Throwable? = this
    while (cause != null) {
        if (cause is SQLException && cause.sqlState == "23505") return true
        cause = cause.cause
    }
    return false
}
