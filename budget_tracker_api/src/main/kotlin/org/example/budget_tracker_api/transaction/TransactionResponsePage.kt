package org.example.budget_tracker_api.transaction

import io.swagger.v3.oas.annotations.media.Schema


// only for docs
@Schema(description = "Paginated transaction response")
data class TransactionResponsePage(
    val content: List<TransactionResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
)