package com.wolt.wm.training.bank.common.models

data class ApiErrorResponseBody(
    val error: String? = "Something went wrong",
    val status: Int,
)
