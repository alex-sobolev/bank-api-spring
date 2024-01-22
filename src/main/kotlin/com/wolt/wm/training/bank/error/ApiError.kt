package com.wolt.wm.training.bank.error

data class ApiErrorResponseBody(
    val error: String? = "Something went wrong",
    val status: Int,
)
