package com.pantryflow.application.port.out

interface TransactionRunner {
    fun <T> transaction(block: () -> T): T
}
