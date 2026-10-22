package com.pantryflow

import com.pantryflow.domain.exception.InvalidInput
import com.pantryflow.domain.valueobject.BatchDates
import com.pantryflow.domain.valueobject.DisplayName
import com.pantryflow.domain.valueobject.Email
import com.pantryflow.domain.valueobject.HouseholdName
import com.pantryflow.domain.valueobject.InviteCode
import com.pantryflow.domain.valueobject.ProductName
import com.pantryflow.domain.valueobject.Quantity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class DomainValueObjectsTest {
    @Test
    fun `text values normalize and validate at construction`() {
        assertEquals("user@example.com", Email.of("  USER@Example.COM  ").value)
        assertEquals("Alex", DisplayName.of(" Alex ").value)
        assertEquals("Home", HouseholdName.of(" Home ").value)
        assertEquals("Milk", ProductName.of(" Milk ").value)
        assertEquals("ABCDEFGHJKL2", InviteCode.of(" abcdefghjkl2 ").value)
        assertNull(InviteCode.parseOrNull("invalid"))

        assertThrows(InvalidInput::class.java) { Email.of("invalid") }
        assertThrows(InvalidInput::class.java) { DisplayName.of(" A ") }
        assertThrows(InvalidInput::class.java) { HouseholdName.of(" ") }
        assertThrows(InvalidInput::class.java) { ProductName.of(" ") }
        assertThrows(InvalidInput::class.java) { InviteCode.of("invalid") }
    }

    @Test
    fun `quantity checks input bounds while total can sum valid batches`() {
        assertEquals(BigDecimal("0.125"), Quantity.positive(BigDecimal("0.125")).value)
        assertEquals(BigDecimal("1000000000.000"), Quantity.total(BigDecimal("1000000000.000")).value)
        assertThrows(InvalidInput::class.java) { Quantity.positive(BigDecimal.ZERO) }
        assertThrows(InvalidInput::class.java) { Quantity.of(BigDecimal("-1")) }
        assertThrows(InvalidInput::class.java) { Quantity.of(BigDecimal("1.0000")) }
        assertThrows(InvalidInput::class.java) { Quantity.of(BigDecimal("1000000000")) }
    }

    @Test
    fun `expiry cannot precede purchase date`() {
        val day = LocalDate.of(2026, 10, 21)
        assertEquals(day, BatchDates(day, day).expiresOn)
        assertThrows(InvalidInput::class.java) { BatchDates(day, day.minusDays(1)) }
    }
}
