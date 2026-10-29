package org.example.budget_tracker_api.monthlySummary

import org.example.budget_tracker_api.budget.BudgetRepository
import org.example.budget_tracker_api.database.Budget
import org.example.budget_tracker_api.database.Category
import org.example.budget_tracker_api.database.Transaction
import org.example.budget_tracker_api.transaction.TransactionRepository
import org.example.budget_tracker_api.transaction.TransactionType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class MonthlySummaryServiceTest {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var budgetRepository: BudgetRepository
    private lateinit var service: MonthlySummaryService

    private val food = Category(
        name = "Food",
        color = "#FF0000"
    )

    private val transport = Category(
        name = "Transport",
        color = "#0000FF"
    )

    @BeforeEach
    fun setUp() {
        transactionRepository = org.mockito.Mockito.mock(
            TransactionRepository::class.java
        )
        budgetRepository = org.mockito.Mockito.mock(
            BudgetRepository::class.java
        )

        service = MonthlySummaryService(
            transactionRepository,
            budgetRepository
        )
    }

    private fun transaction(
        amount: String,
        type: TransactionType,
        category: Category,
        date: LocalDate = LocalDate.of(2026, 10, 10)
    ) = Transaction(
        amount = BigDecimal(amount),
        type = type,
        category = category,
        date = date,
        description = null
    )

    private fun budget(
        amount: String,
        category: Category,
        month: String = "2026-10"
    ) = Budget(
        month = month,
        amount = BigDecimal(amount),
        category = category
    )

    @Test
    fun calculatesTotalIncomeExpenseAndBalance() {
        `when`(
            transactionRepository.findByDateBetween(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
            )
        ).thenReturn(
            listOf(
                transaction("300.00", TransactionType.INCOME, food),
                transaction("100.00", TransactionType.EXPENSE, food),
                transaction("50.00", TransactionType.EXPENSE, transport)
            )
        )

        `when`(budgetRepository.findAllByMonth("2026-10"))
            .thenReturn(emptyList())

        val result = service.getMonthlySummary(2026, 10)

        assertEquals(BigDecimal("300.00"), result.totalIncome)
        assertEquals(BigDecimal("150.00"), result.totalExpense)
        assertEquals(BigDecimal("150.00"), result.balance)
    }

    @Test
    fun balanceCanBeNegative() {
        `when`(
            transactionRepository.findByDateBetween(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
            )
        ).thenReturn(
            listOf(
                transaction("100.00", TransactionType.INCOME, food),
                transaction("250.00", TransactionType.EXPENSE, food)
            )
        )

        `when`(budgetRepository.findAllByMonth("2026-10"))
            .thenReturn(emptyList())

        val result = service.getMonthlySummary(2026, 10)

        assertEquals(BigDecimal("-150.00"), result.balance)
    }

    @Test
    fun calculatesSpentBudgetAndRemainingForCategory() {
        `when`(
            transactionRepository.findByDateBetween(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
            )
        ).thenReturn(
            listOf(
                transaction("100.00", TransactionType.EXPENSE, food),
                transaction("50.00", TransactionType.EXPENSE, food)
            )
        )

        `when`(budgetRepository.findAllByMonth("2026-10"))
            .thenReturn(listOf(budget("200.00", food)))

        val result = service.getMonthlySummary(2026, 10)

        assertEquals(1, result.categorySummaries.size)

        val summary = result.categorySummaries.single()

        assertEquals("Food", summary.categoryName)
        assertEquals(BigDecimal("150.00"), summary.spent)
        assertEquals(BigDecimal("200.00"), summary.budget)
        assertEquals(BigDecimal("50.00"), summary.remaining)
    }

    @Test
    fun remainingCanBeNegativeWhenOverBudget() {
        `when`(
            transactionRepository.findByDateBetween(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
            )
        ).thenReturn(
            listOf(
                transaction("250.00", TransactionType.EXPENSE, food)
            )
        )

        `when`(budgetRepository.findAllByMonth("2026-10"))
            .thenReturn(listOf(budget("200.00", food)))

        val result = service.getMonthlySummary(2026, 10)

        val summary = result.categorySummaries.single()

        assertEquals(BigDecimal("-50.00"), summary.remaining)
    }

    @Test
    fun categoryWithExpenseButWithoutBudgetHasZeroBudget() {
        `when`(
            transactionRepository.findByDateBetween(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
            )
        ).thenReturn(
            listOf(
                transaction("75.00", TransactionType.EXPENSE, food)
            )
        )

        `when`(budgetRepository.findAllByMonth("2026-10"))
            .thenReturn(emptyList())

        val result = service.getMonthlySummary(2026, 10)

        val summary = result.categorySummaries.single()

        assertEquals("Food", summary.categoryName)
        assertEquals(BigDecimal("75.00"), summary.spent)
        assertEquals(BigDecimal.ZERO, summary.budget)
        assertEquals(BigDecimal("-75.00"), summary.remaining)
    }

    @Test
    fun categoryWithBudgetButWithoutExpenseIsIncluded() {
        `when`(
            transactionRepository.findByDateBetween(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
            )
        ).thenReturn(emptyList())

        `when`(budgetRepository.findAllByMonth("2026-10"))
            .thenReturn(listOf(budget("300.00", food)))

        val result = service.getMonthlySummary(2026, 10)

        val summary = result.categorySummaries.single()

        assertEquals("Food", summary.categoryName)
        assertEquals(BigDecimal.ZERO, summary.spent)
        assertEquals(BigDecimal("300.00"), summary.budget)
        assertEquals(BigDecimal("300.00"), summary.remaining)
    }

    @Test
    fun categoriesAreSortedByHighestExpenseThenByName() {
        `when`(
            transactionRepository.findByDateBetween(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
            )
        ).thenReturn(
            listOf(
                transaction("50.00", TransactionType.EXPENSE, food),
                transaction("100.00", TransactionType.EXPENSE, transport),
                transaction("50.00", TransactionType.EXPENSE, transport)
            )
        )

        `when`(budgetRepository.findAllByMonth("2026-10"))
            .thenReturn(
                listOf(
                    budget("200.00", food),
                    budget("300.00", transport)
                )
            )

        val result = service.getMonthlySummary(2026, 10)

        assertEquals(
            listOf("Transport", "Food"),
            result.categorySummaries.map { it.categoryName }
        )
    }

    @Test
    fun monthWithoutTransactionsAndBudgetsReturnsZeroTotalsAndNoCategories() {
        `when`(
            transactionRepository.findByDateBetween(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
            )
        ).thenReturn(emptyList())

        `when`(budgetRepository.findAllByMonth("2026-10"))
            .thenReturn(emptyList())

        val result = service.getMonthlySummary(2026, 10)

        assertEquals(BigDecimal.ZERO, result.totalIncome)
        assertEquals(BigDecimal.ZERO, result.totalExpense)
        assertEquals(BigDecimal.ZERO, result.balance)
        assertTrue(result.categorySummaries.isEmpty())
    }

    @Test
    fun incomeDoesNotCountAsCategoryExpense() {
        `when`(
            transactionRepository.findByDateBetween(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
            )
        ).thenReturn(
            listOf(
                transaction("500.00", TransactionType.INCOME, food)
            )
        )

        `when`(budgetRepository.findAllByMonth("2026-10"))
            .thenReturn(emptyList())

        val result = service.getMonthlySummary(2026, 10)

        assertEquals(BigDecimal("500.00"), result.totalIncome)
        assertEquals(BigDecimal.ZERO, result.totalExpense)
        assertTrue(result.categorySummaries.isEmpty())
    }

    @Test
    fun categoriesWithEqualExpensesAreSortedByName() {
        `when`(
            transactionRepository.findByDateBetween(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
            )
        ).thenReturn(
            listOf(
                transaction("50.00", TransactionType.EXPENSE, transport),
                transaction("50.00", TransactionType.EXPENSE, food)
            )
        )

        `when`(budgetRepository.findAllByMonth("2026-10"))
            .thenReturn(emptyList())

        val result = service.getMonthlySummary(2026, 10)

        assertEquals(
            listOf("Food", "Transport"),
            result.categorySummaries.map { it.categoryName }
        )
    }
}