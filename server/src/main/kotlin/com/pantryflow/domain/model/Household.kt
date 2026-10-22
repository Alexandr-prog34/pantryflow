package com.pantryflow.domain.model

import com.pantryflow.domain.valueobject.HouseholdName
import com.pantryflow.domain.valueobject.InviteCode
import java.util.UUID

data class Household(val id: UUID, val name: HouseholdName, val inviteCode: InviteCode)
