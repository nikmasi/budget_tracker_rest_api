package org.example.budget_tracker_api.transaction

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.example.budget_tracker_api.database.Transaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@Validated
@RestController
@RequestMapping("/api/transactions")
class TransactionController(
    private val transactionRepository: TransactionRepository,
    private val transactionService: TransactionService
) {

    @Operation(
        summary = "Get transactions",
        description = "Returns a paginated list of transactions, sorted by date and ID in descending order. Supports optional date, category, type, and description filters."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Transactions retrieved successfully",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = TransactionResponsePage::class))
                ]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid pagination parameters, dates, or filter values", content = [Content()]
            )
        ]
    )
    @GetMapping
    fun getTransactions(
        @Parameter(
            description = "Zero-based page index.",
            example = "0",
            schema = Schema(type = "integer", minimum = "0", defaultValue = "0")
        )
        @RequestParam(defaultValue = "0") @Min(0) page: Int,

        @Parameter(
            description = "Number of transactions per page. Must be between 1 and 100.",
            example = "10",
            schema = Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "10")
        )
        @RequestParam(defaultValue = "10") @Min(1) @Max(100) size: Int,

        @Parameter(
            description = "Inclusive start date in ISO format (YYYY-MM-DD).",
            example = "2026-10-01"
        )
        @RequestParam(required = false) startDate: LocalDate?,

        @Parameter(
            description = "Inclusive end date in ISO format (YYYY-MM-DD).",
            example = "2026-10-31"
        )
        @RequestParam(required = false) endDate: LocalDate?,

        @Parameter(
            description = "Filter by category ID.",
            example = "4",
            schema = Schema(type = "integer", format = "int64", minimum = "1")
        )
        @RequestParam(required = false) categoryId: Long?,

        @Parameter(
            description = "Filter by transaction type. Allowed values: income, expense. Case-insensitive.",
            example = "expense",
            schema = Schema(type = "string", allowableValues = ["income", "expense"])
        )
        @RequestParam(required = false) type: String?,

        @Parameter(
            description = "Search for text contained in the description. Case-insensitive.",
            example = "groceries"
        )
        @RequestParam(required = false) description: String?
    ): Page<TransactionResponse> {

        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.DESC, "id")))

        var spec = Specification.where<Transaction> { _, _, cb -> cb.conjunction() }

        startDate?.let { start ->
            spec = spec.and { root, _, cb -> cb.greaterThanOrEqualTo(root.get("date"), start) }
        }
        endDate?.let { end ->
            spec = spec.and { root, _, cb -> cb.lessThanOrEqualTo(root.get("date"), end) }
        }
        categoryId?.let { catId ->
            spec = spec.and { root, _, cb -> cb.equal(root.get<Any>("category").get<Any>("id"), catId) }
        }
        description?.let { desc ->
            spec = spec.and { root, _, cb -> cb.like(cb.lower(root.get("description")), "%${desc.lowercase()}%") }
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Start date must not be after end date."
            )
        }

        type?.let { value ->
            if (value.lowercase() !in setOf("income", "expense")) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Type must be either 'income' or 'expense'."
                )
            }

            spec = spec.and { root, _, cb ->
                cb.equal(root.get<String>("type"), value.uppercase())
            }
        }

        return transactionRepository.findAll(spec, pageable).map { it.toResponse() }
    }

    @Operation(
        summary = "Create a transaction",
        description = "Creates a transaction using the supplied category ID. The category must already exist."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Transaction created successfully",
                content = [Content(schema = Schema(implementation = TransactionResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid transaction data or category ID", content = [Content()]
            )
        ]
    )
    @PostMapping
    fun createTransaction(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Transaction data to create",
            required = true,
            content = [Content(mediaType = "application/json", schema = Schema(implementation = TransactionRequest::class))]
        )
        @Valid @RequestBody transaction: TransactionRequest
    ): ResponseEntity<TransactionResponse> {
        val savedTransaction = transactionService.createTransaction(transaction)

        return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction.toResponse())
    }

    @Operation(
        summary = "Get a transaction",
        description = "Retrieves a transaction by its unique ID."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Transaction retrieved successfully",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = TransactionResponse::class))
                ]
            ),
            ApiResponse(
                responseCode = "404", description = "Transaction not found", content = [Content()]
            )
        ]
    )
    @GetMapping("/{id}")
    fun getTransaction(
        @Parameter(
            description = "Unique ID of the transaction",
            example = "1",
            required = true,
            schema = Schema(type = "integer", format = "int64", minimum = "1")
        )
        @PathVariable id: Long
    ): ResponseEntity<TransactionResponse> {
        val transaction = transactionRepository.findById(id)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Transaction with ID $id was not found."
                )
            }

        return ResponseEntity.ok(transaction.toResponse())
    }

    @Operation(
        summary = "Update a transaction",
        description = "Updates an existing transaction. The supplied category ID must identify an existing category."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Transaction updated successfully",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = TransactionResponse::class))
                ]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid transaction data or category ID", content = [Content()]
            ),
            ApiResponse(
                responseCode = "404", description = "Transaction not found", content = [Content()]
            )
        ]
    )
    @PutMapping("/{id}")
    fun updateTransaction(
        @Parameter(
            description = "ID of the transaction to update",
            example = "1",
            required = true,
            schema = Schema(type = "integer", format = "int64", minimum = "1")
        )
        @PathVariable id: Long,

        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated transaction data",
            required = true,
            content = [
                Content(mediaType = "application/json", schema = Schema(implementation = TransactionRequest::class))
            ]
        )
        @Valid @RequestBody updatedTransaction: TransactionRequest
    ): ResponseEntity<TransactionResponse> {
        val savedTransaction = transactionService.updateTransaction(id, updatedTransaction)

        return ResponseEntity.ok(savedTransaction.toResponse())
    }

    @Operation(
        summary = "Delete a transaction",
        description = "Deletes a transaction by its unique ID."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204", description = "Transaction deleted successfully"
            ),
            ApiResponse(
                responseCode = "404", description = "Transaction not found", content = [Content()]
            )
        ]
    )
    @DeleteMapping("/{id}")
    fun deleteTransaction(
        @Parameter(
            description = "ID of the transaction to delete",
            example = "1",
            required = true,
            schema = Schema(type = "integer", format = "int64", minimum = "1")
        )
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        if (!transactionRepository.existsById(id)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Transaction with ID $id was not found."
            )
        }

        transactionRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "Import transactions from CSV",
        description = "Imports transactions from a CSV file and returns the import count and row-level validation errors."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "CSV file processed successfully",
                content = [
                    Content(
                        mediaType = "application/json", schema = Schema(implementation = TransactionService.CsvImportResult::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400", description = "Missing file or invalid request", content = [Content()]
            )
        ]
    )
    @PostMapping(
        "/import",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun importTransactions(
        @Parameter(
            description = "CSV file containing transactions to import",
            required = true,
            `in` = ParameterIn.QUERY,
            schema = Schema(type = "string", format = "binary")
        )
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<TransactionService.CsvImportResult> {

        val result = transactionService.importCsv(file)
        return ResponseEntity.ok(result)
    }

    private fun Transaction.toResponse(): TransactionResponse =
        TransactionResponse(
            id = requireNotNull(id) { "Transaction ID must not be null" },
            type = type,
            amount = amount,
            date = date,
            description = description,
            categoryId = requireNotNull(category.id) {
                "Category ID must not be null"
            }
        )
}