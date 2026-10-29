package org.example.budget_tracker_api.budget

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BudgetRequestValidationTest {

    private val validator = Validation
        .buildDefaultValidatorFactory()
        .validator

    private fun request(
        month: String = "2026-10",
        categoryId: Long = 1L,
        amount: BigDecimal = BigDecimal("100.00")
    ) = BudgetRequest(
        month = month,
        categoryId = categoryId,
        amount = amount
    )

    @Test
    fun validBudgetRequestIsAccepted() {
        assertTrue(validator.validate(request()).isEmpty())
    }

    @Test
    fun invalidMonthFormatIsRejected() {
        val violations = validator.validate(request(month = "10-2026"))

        assertTrue(violations.any { it.propertyPath.toString() == "month" })
    }

    @Test
    fun monthWithInvalidNumberIsRejected() {
        val violations = validator.validate(request(month = "2026-13"))

        assertTrue(violations.any { it.propertyPath.toString() == "month" })
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
            request(amount = BigDecimal("-10.00"))
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
    fun amountWithMoreThanTwoDecimalPlacesIsRejected() {
        val violations = validator.validate(
            request(amount = BigDecimal("10.123"))
        )

        assertTrue(violations.any { it.propertyPath.toString() == "amount" })
    }

    @Test
    fun maximumAllowedAmountIsAccepted() {
        val violations = validator.validate(
            request(amount = BigDecimal("1000000.00"))
        )

        assertTrue(violations.isEmpty())
    }

    @Test
    fun zeroCategoryIDisRejected() {
        val violations = validator.validate(request(categoryId = 0L))

        assertTrue(violations.any { it.propertyPath.toString() == "categoryId" })
    }

    @Test
    fun negativeCategoryIDIsRejected() {
        val violations = validator.validate(request(categoryId = -1L))

        assertTrue(violations.any { it.propertyPath.toString() == "categoryId" })
    }
}