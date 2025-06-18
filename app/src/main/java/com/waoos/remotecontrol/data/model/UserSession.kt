package com.waoos.remotecontrol.data.model

import java.util.*

data class UserSession(
    val email: String,
    val loginTime: Date,
    val deviceInfo: Map<String, String>
)
