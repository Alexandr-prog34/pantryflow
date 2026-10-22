package com.pantryflow.domain.model

import com.pantryflow.domain.enums.MemberRole
import java.util.UUID

data class Membership(val householdId: UUID, val userId: UUID, val role: MemberRole)
