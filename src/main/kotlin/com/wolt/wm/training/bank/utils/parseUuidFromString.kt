package com.wolt.wm.training.bank.utils

import java.util.*

fun parseUuidFromString(str: String, errorMsg: String? = "Invalid UUID"): UUID {
    return try {
        UUID.fromString(str)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException(errorMsg)
    }
}
