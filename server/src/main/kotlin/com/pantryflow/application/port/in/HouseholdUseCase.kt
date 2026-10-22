package com.pantryflow.application.port.`in`

import com.pantryflow.application.dto.CreateHouseholdCommand
import com.pantryflow.application.dto.HouseholdData
import com.pantryflow.application.dto.JoinHouseholdCommand
import java.util.UUID

interface HouseholdUseCase {
    fun create(command: CreateHouseholdCommand): HouseholdData
    fun join(command: JoinHouseholdCommand): HouseholdData
    fun listForUser(userId: UUID): List<HouseholdData>
}
