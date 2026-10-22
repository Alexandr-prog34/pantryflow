package com.pantryflow.domain.model

import com.pantryflow.domain.valueobject.DisplayName
import com.pantryflow.domain.valueobject.Email
import java.util.UUID

data class User(val id: UUID, val email: Email, val displayName: DisplayName){}
