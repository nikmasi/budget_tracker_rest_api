package org.example.budget_tracker_api.transaction

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.example.budget_tracker_api.category.CategoryRepository
import org.example.budget_tracker_api.database.Category
import org.example.budget_tracker_api.database.Transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockMultipartFile
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class TransactionServiceCsvImportTest {

    @MockK
    lateinit var transactionRepository: TransactionRepository

    @MockK
    lateinit var categoryRepository: CategoryRepository

    @InjectMockKs
    lateinit var transactionService: TransactionService

    @Test
    fun shouldSuccessfullyImportValidRowsAndReportInvalidRows() {
        val foodCategory = Category(id = 1, name = "Food", color = "#FF5733")
        val salaryCategory = Category(id = 2, name = "Salary", color = "#00FF00")

        every { categoryRepository.findAll() } returns listOf(foodCategory, salaryCategory)
        val transactionSlot = slot<List<Transaction>>()
        every { transactionRepository.saveAll(capture(transactionSlot)) } answers { transactionSlot.captured }

        val csvContent = """
            type,amount,date,description,category
            expense,12.50,2026-10-08,Lunch,Food
            expense,50.00,2026-10-09,Dinner,Other
            income,invalid_amount,2026-10-10,,Salary
            income,300.00,2026-10-09,,Salary
        """.trimIndent()

        val multipartFile = MockMultipartFile(
            "file",
            "transactions.csv",
            "text/csv",
            csvContent.toByteArray()
        )

        val result = transactionService.importCsv(multipartFile)

        assertEquals(2, result.importedCount)

        assertEquals(2, result.failedRows.size)

        val failedRow1 = result.failedRows[0]
        assertEquals(3, failedRow1.rowNumber)
        assertTrue(failedRow1.reason.contains("Category 'Other' does not exist"))

        val failedRow2 = result.failedRows[1]
        assertEquals(4, failedRow2.rowNumber)
        assertTrue(failedRow2.reason.contains("Amount is required and must be a valid number"))

        val savedTransactions = transactionSlot.captured
        assertEquals(2, savedTransactions.size)
        assertEquals(BigDecimal("12.50"), savedTransactions[0].amount)
        assertEquals(BigDecimal("300.00"), savedTransactions[1].amount)
    }

    @Test
    fun shouldImportAllValidRows() {
        val foodCategory = Category(id = 1, name = "Food", color = "#FF5733")

        every { categoryRepository.findAll() } returns listOf(foodCategory)

        val transactionSlot = slot<List<Transaction>>()
        every {
            transactionRepository.saveAll(capture(transactionSlot))
        } answers {
            transactionSlot.captured
        }

        val csvContent = """
        type,amount,date,description,category
        expense,12.50,2026-10-08,Lunch,Food
        expense,25.00,2026-10-09,Dinner,Food
    """.trimIndent()

        val file = MockMultipartFile(
            "file",
            "transactions.csv",
            "text/csv",
            csvContent.toByteArray()
        )

        val result = transactionService.importCsv(file)

        assertEquals(2, result.importedCount)
        assertTrue(result.failedRows.isEmpty())

        val savedTransactions = transactionSlot.captured
        assertEquals(2, savedTransactions.size)
        assertEquals(BigDecimal("12.50"), savedTransactions[0].amount)
        assertEquals(BigDecimal("25.00"), savedTransactions[1].amount)
    }

    @Test
    fun shouldNotSaveTransactionsWhenAllRowsAreInvalid() {
        val foodCategory = Category(id = 1, name = "Food", color = "#FF5733")

        every { categoryRepository.findAll() } returns listOf(foodCategory)

        val csvContent = """
        type,amount,date,description,category
        expense,invalid,2026-10-08,Lunch,Food
        expense,invalid,2026-10-09,Dinner,Food
    """.trimIndent()

        val file = MockMultipartFile(
            "file",
            "transactions.csv",
            "text/csv",
            csvContent.toByteArray()
        )

        val result = transactionService.importCsv(file)

        assertEquals(0, result.importedCount)
        assertEquals(2, result.failedRows.size)

        io.mockk.verify(exactly = 0) {
            transactionRepository.saveAll(any<List<Transaction>>())
        }
    }

    @Test
    fun shouldReportErrorWhenCategoryDoesNotExist() {
        every { categoryRepository.findAll() } returns emptyList()

        val csvContent = """
        type,amount,date,description,category
        expense,12.50,2026-10-08,Lunch,Food
    """.trimIndent()

        val file = MockMultipartFile(
            "file",
            "transactions.csv",
            "text/csv",
            csvContent.toByteArray()
        )

        val result = transactionService.importCsv(file)

        assertEquals(0, result.importedCount)
        assertEquals(1, result.failedRows.size)
        assertTrue(
            result.failedRows[0].reason.contains("Category 'Food' does not exist")
        )

        io.mockk.verify(exactly = 0) {
            transactionRepository.saveAll(any<List<Transaction>>())
        }
    }

    @Test
    fun shouldReturnZeroImportedRowsWhenCsvContainsOnlyHeader() {
        every { categoryRepository.findAll() } returns emptyList()

        val csvContent = "type,amount,date,description,category"

        val file = MockMultipartFile(
            "file",
            "transactions.csv",
            "text/csv",
            csvContent.toByteArray()
        )

        val result = transactionService.importCsv(file)

        assertEquals(0, result.importedCount)
        assertTrue(result.failedRows.isEmpty())

        io.mockk.verify(exactly = 0) {
            transactionRepository.saveAll(any<List<Transaction>>())
        }
    }

    @Test
    fun shouldMatchCategoryNameIgnoringCase() {
        val foodCategory = Category(
            id = 1,
            name = "Food",
            color = "#FF5733"
        )

        every { categoryRepository.findAll() } returns listOf(foodCategory)

        val transactionSlot = slot<List<Transaction>>()

        every {
            transactionRepository.saveAll(capture(transactionSlot))
        } answers {
            transactionSlot.captured
        }

        val csvContent = """
        type,amount,date,description,category
        expense,12.50,2026-10-08,Lunch,food
        expense,25.00,2026-10-09,Dinner,FOOD
    """.trimIndent()

        val file = MockMultipartFile(
            "file",
            "transactions.csv",
            "text/csv",
            csvContent.toByteArray()
        )

        val result = transactionService.importCsv(file)

        assertEquals(2, result.importedCount)
        assertTrue(result.failedRows.isEmpty())
        assertEquals(2, transactionSlot.captured.size)
    }

    @Test
    fun shouldImportTransactionWithEmptyDescription() {
        val category = Category(
            id = 1,
            name = "Food",
            color = "#FF5733"
        )

        every { categoryRepository.findAll() } returns listOf(category)

        val transactionSlot = slot<List<Transaction>>()

        every {
            transactionRepository.saveAll(capture(transactionSlot))
        } answers {
            transactionSlot.captured
        }

        val csvContent = """
        type,amount,date,description,category
        expense,12.50,2026-10-08,,Food
    """.trimIndent()

        val file = MockMultipartFile(
            "file",
            "transactions.csv",
            "text/csv",
            csvContent.toByteArray()
        )

        val result = transactionService.importCsv(file)

        assertEquals(1, result.importedCount)
        assertTrue(result.failedRows.isEmpty())
        assertEquals(null, transactionSlot.captured.single().description)
    }

    @Test
    fun shouldRejectAmountAboveMaximum() {
        val category = Category(
            id = 1,
            name = "Food",
            color = "#FF5733"
        )

        every { categoryRepository.findAll() } returns listOf(category)

        val csvContent = """
        type,amount,date,description,category
        expense,1000000.01,2026-10-08,Lunch,Food
    """.trimIndent()

        val file = MockMultipartFile(
            "file",
            "transactions.csv",
            "text/csv",
            csvContent.toByteArray()
        )

        val result = transactionService.importCsv(file)

        assertEquals(0, result.importedCount)
        assertEquals(1, result.failedRows.size)
        assertTrue(
            result.failedRows[0].reason.isNotBlank()
        )

        io.mockk.verify(exactly = 0) {
            transactionRepository.saveAll(any<List<Transaction>>())
        }
    }
}