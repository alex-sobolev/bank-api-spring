package io.alex.bank.utils

import java.util.UUID

fun parseUuidFromString(
    str: String,
    errorMsg: String? = "Invalid UUID",
): UUID {
    return try {
        UUID.fromString(str)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException(errorMsg)
    }
}
