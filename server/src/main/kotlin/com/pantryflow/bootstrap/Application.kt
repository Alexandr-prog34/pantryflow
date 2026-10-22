package com.pantryflow.bootstrap

import com.pantryflow.adapters.`in`.web.configureHttp
import com.pantryflow.adapters.`in`.web.controller.UseCases
import com.pantryflow.adapters.`in`.web.controller.configureRoutes
import com.pantryflow.adapters.out.jpa.JpaDatabase
import com.pantryflow.adapters.out.jpa.repository.*
import com.pantryflow.application.service.*
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped

fun Application.module() {
    val url = environment.config.propertyOrNull("db.url")?.getString()
        ?: System.getenv("DB_URL") ?: error("DB_URL is required")
    val user = environment.config.propertyOrNull("db.user")?.getString()
        ?: System.getenv("DB_USER") ?: error("DB_USER is required")
    val password = environment.config.propertyOrNull("db.password")?.getString()
        ?: System.getenv("DB_PASSWORD") ?: error("DB_PASSWORD is required")

    val db = JpaDatabase(url, user, password)
    db.migrate()
    monitor.subscribe(ApplicationStopped) { db.close() }

    val userRepository = JpaUserRepository(db)
    val householdRepository = JpaHouseholdRepository(db)
    val inventory = InventoryService(JpaInventoryRepository(db), householdRepository, db)
    val useCases = UseCases(
        users = UserService(userRepository, db),
        households = HouseholdService(householdRepository, userRepository, db),
        inventory = inventory,
        shopping = ShoppingService(JpaShoppingRepository(db), inventory, householdRepository, db),
    )

    configureHttp()
    configureRoutes(useCases)
}
