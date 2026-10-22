package com.pantryflow.domain.valueobject

import com.pantryflow.domain.exception.requireInput
import java.security.SecureRandom

@JvmInline
value class InviteCode private constructor(val value: String) {
    companion object {
        private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        private const val LENGTH = 12

        fun of(raw: String): InviteCode {
            val normalized = raw.trim().uppercase()
            requireInput(isValid(normalized), "Invalid invite code")
            return InviteCode(normalized)
        }

        fun parseOrNull(raw: String): InviteCode? {
            val normalized = raw.trim().uppercase()
            return if (isValid(normalized)) InviteCode(normalized) else null
        }

        fun generate(random: SecureRandom): InviteCode = InviteCode(
            buildString { repeat(LENGTH) { append(ALPHABET[random.nextInt(ALPHABET.length)]) } },
        )

        private fun isValid(value: String): Boolean =
            value.length == LENGTH && value.all { it in ALPHABET }
    }
}
