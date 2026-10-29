package org.example.budget_tracker_api.transaction

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.example.budget_tracker_api.category.CategoryRepository
import org.example.budget_tracker_api.database.Transaction
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeParseException
import org.springframework.web.server.ResponseStatusException

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {

    data class CsvImportResult(
        val importedCount: Int,
        val failedRows: List<FailedRowDetail>
    )

    data class FailedRowDetail(
        val rowNumber: Int,
        val reason: String
    )

    fun importCsv(file: MultipartFile): CsvImportResult {
        val failedRows = mutableListOf<FailedRowDetail>()
        val successfulTransactions = mutableListOf<Transaction>()

        val categoryMap = categoryRepository.findAll().associateBy { it.name.trim().lowercase() }

        BufferedReader(InputStreamReader(file.inputStream, Charsets.UTF_8)).use { reader ->
            val csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim()

            CSVParser(reader, csvFormat).use { csvParser ->
                for (record in csvParser) {
                    val rowNumber = record.recordNumber.toInt() + 1

                    try {
                        val type = record.get("type").trim()
                        val amountStr = record.get("amount").trim()
                        val dateStr = record.get("date").trim()
                        val description = record.get("description").trim().ifBlank { null }
                        val categoryName = record.get("category").trim()

                        if (type !in listOf("income", "expense")) {
                            throw IllegalArgumentException(
                                "Type must be 'income' or 'expense'."
                            )
                        }

                        val amount = amountStr.toBigDecimalOrNull()
                            ?: throw IllegalArgumentException(
                                "Amount is required and must be a valid number."
                            )

                        if (amount <= BigDecimal.ZERO) {
                            throw IllegalArgumentException(
                                "Amount must be greater than 0."
                            )
                        }

                        if (amount.scale() > 2) {
                            throw IllegalArgumentException(
                                "Amount can have at most 2 decimal places."
                            )
                        }

                        if (amount > BigDecimal("1000000.00")) {
                            throw IllegalArgumentException(
                                "Amount must be at most 1,000,000.00."
                            )
                        }

                        if (dateStr.isBlank()) {
                            throw IllegalArgumentException("Date is required.")
                        }

                        val date = try {
                            LocalDate.parse(dateStr)
                        } catch (e: DateTimeParseException) {
                            throw IllegalArgumentException(
                                "Date must be a valid date in yyyy-MM-dd format."
                            )
                        }

                        if (description != null && description.length > 200) {
                            throw IllegalArgumentException(
                                "Description can be up to 200 characters."
                            )
                        }

                        if (categoryName.isBlank()) {
                            throw IllegalArgumentException(
                                "Category is required."
                            )
                        }

                        val category = categoryMap[categoryName.lowercase()]
                            ?: throw IllegalArgumentException(
                                "Category '$categoryName' does not exist."
                            )

                        successfulTransactions.add(
                            Transaction(
                                type = TransactionType.INCOME,
                                amount = amount,
                                date = date,
                                description = description,
                                category = category
                            )
                        )

                    } catch (e: Exception) {
                        failedRows.add(
                            FailedRowDetail(
                                rowNumber = rowNumber,
                                reason = e.message ?: "Invalid CSV row."
                            )
                        )
                    }
                }
            }
        }

        if (successfulTransactions.isNotEmpty()) {
            transactionRepository.saveAll(successfulTransactions)
        }

        return CsvImportResult(
            importedCount = successfulTransactions.size,
            failedRows = failedRows
        )
    }

    @Transactional
    fun createTransaction(request: TransactionRequest): Transaction {
        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Category with ID ${request.categoryId} was not found."
                )
            }

        val transaction = Transaction(
            type = request.type,
            amount = request.amount,
            date = request.date,
            description = request.description,
            category = category
        )

        return transactionRepository.save(transaction)
    }


    @Transactional
    fun updateTransaction(
        id: Long,
        request: TransactionRequest
    ): Transaction {
        val transaction = transactionRepository.findById(id)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Transaction with ID $id was not found."
                )
            }

        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Category with ID ${request.categoryId} was not found."
                )
            }

        transaction.type = request.type
        transaction.amount = request.amount
        transaction.date = request.date
        transaction.description = request.description
        transaction.category = category

        return transactionRepository.save(transaction)
    }
}