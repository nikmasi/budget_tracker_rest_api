package org.example.budget_tracker_api.transaction

import jakarta.validation.Validation
import org.example.budget_tracker_api.database.Category
import org.example.budget_tracker_api.database.Transaction
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class TransactionValidationTest {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    private fun createTransaction(
        type: TransactionType = TransactionType.EXPENSE,
        amount: BigDecimal = BigDecimal("10.00"),
        date: LocalDate = LocalDate.of(2026, 10, 8),
        description: String? = "Lunch",
        category: Category = Category(
            id = 1L,
            name = "Food",
            color = "#FF0000"
        )
    ) = Transaction(
        type = type,
        amount = amount,
        date = date,
        description = description,
        category = category
    )

    private fun hasViolation(
        transaction: Transaction,
        field: String
    ): Boolean {
        return validator.validate(transaction).any {
            it.propertyPath.toString() == field
        }
    }

    @Test
    fun incomeTypeIsValid() {
        assertFalse(
            hasViolation(
                transaction = createTransaction(type = TransactionType.INCOME),
                field = "type"
            )
        )
    }

    @Test
    fun expenseTypeIsValid() {
        assertFalse(
            hasViolation(
                transaction = createTransaction(type = TransactionType.EXPENSE),
                field = "type"
            )
        )
    }

    @Test
    fun positiveAmountIsValid() {
        assertFalse(
            hasViolation(
                transaction = createTransaction(amount = BigDecimal("10.50")),
                field = "amount"
            )
        )
    }

    @Test
    fun zeroAmountIsRejected() {
        assertTrue(
            hasViolation(
                transaction = createTransaction(amount = BigDecimal("0.00")),
                field = "amount"
            )
        )
    }

    @Test
    fun negativeAmountIsRejected() {
        assertTrue(
            hasViolation(
                transaction = createTransaction(amount = BigDecimal("-10.00")),
                field = "amount"
            )
        )
    }

    @Test
    fun amountAboveMaximumIsRejected() {
        assertTrue(
            hasViolation(
                transaction = createTransaction(amount = BigDecimal("1000000.01")),
                field = "amount"
            )
        )
    }

    @Test
    fun maximumAmountIsValid() {
        assertFalse(
            hasViolation(
                transaction = createTransaction(amount = BigDecimal("1000000.00")),
                field = "amount"
            )
        )
    }

    @Test
    fun validDateIsAccepted() {
        assertFalse(
            hasViolation(
                transaction = createTransaction(date = LocalDate.of(2026, 10, 8)),
                field = "date"
            )
        )
    }

    @Test
    fun leapDayIsAccepted() {
        assertFalse(
            hasViolation(
                transaction = createTransaction(date = LocalDate.of(2024, 2, 29)),
                field = "date"
            )
        )
    }

    @Test
    fun nullDescriptionIsAccepted() {
        assertFalse(
            hasViolation(
                transaction = createTransaction(description = null),
                field = "description"
            )
        )
    }

    @Test
    fun emptyDescriptionIsAccepted() {
        assertFalse(
            hasViolation(
                transaction = createTransaction(description = ""),
                field = "description"
            )
        )
    }

    @Test
    fun character200DescriptionIsAccepted() {
        assertFalse(
            hasViolation(
                transaction = createTransaction(description = "A".repeat(200)),
                field = "description"
            )
        )
    }

    @Test
    fun character201DescriptionIsRejected() {
        assertTrue(
            hasViolation(
                transaction = createTransaction(description = "A".repeat(201)),
                field = "description"
            )
        )
    }
}