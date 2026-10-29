package org.example.budget_tracker_api.transaction

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class TransactionRequestValidationTest {

    private val validator = Validation
        .buildDefaultValidatorFactory()
        .validator

    private fun request(
        type: String = "expense",
        amount: BigDecimal = BigDecimal("25.50"),
        date: LocalDate = LocalDate.of(2026, 10, 8),
        description: String? = "Lunch",
        categoryId: Long = 1L
    ) = TransactionRequest(
        type = TransactionType.EXPENSE,
        amount = amount,
        date = date,
        description = description,
        categoryId = categoryId
    )

    @Test
    fun validTransactionRequestIsAccepted() {
        assertTrue(validator.validate(request()).isEmpty())
    }

    @Test
    fun incomeTransactionIsAccepted() {
        assertTrue(
            validator.validate(request(type = "income")).isEmpty()
        )
    }

    @Test
    fun zeroAmountIsRejected() {
        val violations = validator.validate(
            request(amount = BigDecimal("0.00"))
        )

        assertTrue(violations.any { it.propertyPath.toString() == "amount" })
    }

    @Test
    fun negativeAmountIsRejected() {
        val violations = validator.validate(
            request(amount = BigDecimal("-1.00"))
        )

        assertTrue(violations.any { it.propertyPath.toString() == "amount" })
    }

    @Test
    fun amountAboveMaximumIsRejected() {
        val violations = validator.validate(
            request(amount = BigDecimal("1000000.01"))
        )

        assertTrue(violations.any { it.propertyPath.toString() == "amount" })
    }

    @Test
    fun amountWithMoreThan2decimalPlacesIsRejected() {
        val violations = validator.validate(
            request(amount = BigDecimal("10.123"))
        )

        assertTrue(violations.any { it.propertyPath.toString() == "amount" })
    }

    @Test
    fun maximumAllowedAmountIsAccepted() {
        assertTrue(
            validator.validate(
                request(amount = BigDecimal("1000000.00"))
            ).isEmpty()
        )
    }

    @Test
    fun descriptionWith200CharactersIsAccepted() {
        val violations = validator.validate(
            request(description = "A".repeat(200))
        )

        assertTrue(violations.isEmpty())
    }

    @Test
    fun descriptionWithMoreThan200CharactersIsRejected() {
        val violations = validator.validate(
            request(description = "A".repeat(201))
        )

        assertTrue(
            violations.any { it.propertyPath.toString() == "description" }
        )
    }

    @Test
    fun emptyDescriptionIsAccepted() {
        assertTrue(
            validator.validate(request(description = "")).isEmpty()
        )
    }

    @Test
    fun zeroCategoryIDIsRejected() {
        val violations = validator.validate(request(categoryId = 0L))

        assertTrue(violations.any { it.propertyPath.toString() == "categoryId" })
    }

    @Test
    fun negativeCategoryIDisRejected() {
        val violations = validator.validate(request(categoryId = -1L))

        assertTrue(violations.any { it.propertyPath.toString() == "categoryId" })
    }
}