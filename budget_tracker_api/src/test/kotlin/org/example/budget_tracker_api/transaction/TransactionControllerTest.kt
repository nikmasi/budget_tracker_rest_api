package org.example.budget_tracker_api.transaction

import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.example.budget_tracker_api.category.CategoryRepository
import org.example.budget_tracker_api.database.Category
import org.example.budget_tracker_api.database.Transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockKExtension::class)
class TransactionControllerTest {

    @MockK
    lateinit var transactionRepository: TransactionRepository

    @MockK
    lateinit var transactionService: TransactionService

    @InjectMockKs
    lateinit var controller: TransactionController

    private val foodCategory = Category(id = 1L, name = "Food", color = "#FF5733")

    private fun createTransaction(
        id: Long = 1L,
        type: TransactionType = TransactionType.EXPENSE,
        amount: BigDecimal = BigDecimal("25.50"),
        date: LocalDate = LocalDate.of(2026, 10, 20),
        description: String = "Lunch",
        category: Category = foodCategory
    ) = Transaction(
        id = id,
        type = type,
        amount = amount,
        date = date,
        description = description,
        category = category
    )

    @Test
    fun shouldReturnPaginatedTransactions() {
        val transaction = createTransaction()
        val pageableSlot = slot<Pageable>()

        every {
            transactionRepository.findAll(
                any<Specification<Transaction>>(),
                capture(pageableSlot)
            )
        } returns PageImpl(listOf(transaction))

        val result = controller.getTransactions(
            page = 0,
            size = 10,
            startDate = null,
            endDate = null,
            categoryId = null,
            type = null,
            description = null
        )

        assertEquals(1, result.totalElements)
        assertEquals(1, result.content.size)
        assertEquals(1L, result.content[0].id)
        assertEquals(BigDecimal("25.50"), result.content[0].amount)

        val pageable = pageableSlot.captured

        assertEquals(0, pageable.pageNumber)
        assertEquals(10, pageable.pageSize)
        assertEquals(
            Sort.Direction.DESC,
            pageable.sort.getOrderFor("date")?.direction
        )
        assertEquals(
            Sort.Direction.DESC,
            pageable.sort.getOrderFor("id")?.direction
        )
    }

    @Test
    fun shouldPassRequestedPaginationParameters() {
        val pageableSlot = slot<Pageable>()

        every {
            transactionRepository.findAll(
                any<Specification<Transaction>>(),
                capture(pageableSlot)
            )
        } returns PageImpl(emptyList())

        val result = controller.getTransactions(
            page = 2,
            size = 5,
            startDate = null,
            endDate = null,
            categoryId = null,
            type = null,
            description = null
        )

        assertEquals(0, result.totalElements)
        assertEquals(2, pageableSlot.captured.pageNumber)
        assertEquals(5, pageableSlot.captured.pageSize)
    }

    @Test
    fun shouldRejectStartDateAfterEndDate() {
        val exception = assertThrows(ResponseStatusException::class.java) {
            controller.getTransactions(
                page = 0,
                size = 10,
                startDate = LocalDate.of(2026, 10, 20),
                endDate = LocalDate.of(2026, 10, 1),
                categoryId = null,
                type = null,
                description = null
            )
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        assertEquals(
            "Start date must not be after end date.",
            exception.reason
        )

        verify(exactly = 0) {
            transactionRepository.findAll(
                any<Specification<Transaction>>(),
                any<Pageable>()
            )
        }
    }

    @Test
    fun shouldRejectUnsupportedTransactionType() {
        val exception = assertThrows(ResponseStatusException::class.java) {
            controller.getTransactions(
                page = 0,
                size = 10,
                startDate = null,
                endDate = null,
                categoryId = null,
                type = "TRANSFER",
                description = null
            )
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        assertEquals(
            "Type must be either 'income' or 'expense'.",
            exception.reason
        )
    }

    @Test
    fun shouldAcceptLowercaseTransactionType() {
        every {
            transactionRepository.findAll(
                any<Specification<Transaction>>(),
                any<Pageable>()
            )
        } returns PageImpl(emptyList())

        val result = controller.getTransactions(
            page = 0,
            size = 10,
            startDate = null,
            endDate = null,
            categoryId = null,
            type = "income",
            description = null
        )

        assertEquals(0, result.totalElements)

        verify(exactly = 1) {
            transactionRepository.findAll(
                any<Specification<Transaction>>(),
                any<Pageable>()
            )
        }
    }

    @Test
    fun shouldReturnTransactionById() {
        val transaction = createTransaction(id = 5L)

        every {
            transactionRepository.findById(5L)
        } returns Optional.of(transaction)

        val response = controller.getTransaction(5L)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(5L, response.body!!.id)
        assertEquals(TransactionType.EXPENSE, response.body!!.type)
        assertEquals(BigDecimal("25.50"), response.body!!.amount)
    }

    @Test
    fun shouldReturn404WhenTransactionDoesNotExist() {
        every {
            transactionRepository.findById(99L)
        } returns Optional.empty()

        val exception = assertThrows(ResponseStatusException::class.java) {
            controller.getTransaction(99L)
        }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        assertEquals(
            "Transaction with ID 99 was not found.",
            exception.reason
        )
    }

    @Test
    fun shouldCreateTransactionAndReturn201() {
        val request = mockk<TransactionRequest>()
        val savedTransaction = createTransaction(id = 10L)

        every {
            transactionService.createTransaction(request)
        } returns savedTransaction

        val response = controller.createTransaction(request)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
        assertEquals(10L, response.body!!.id)

        verify(exactly = 1) {
            transactionService.createTransaction(request)
        }
    }

    @Test
    fun shouldUpdateTransactionAndReturn200() {
        val request = mockk<TransactionRequest>()
        val updatedTransaction = createTransaction(
            id = 5L,
            amount = BigDecimal("80.00"),
            description = "Updated lunch"
        )

        every {
            transactionService.updateTransaction(5L, request)
        } returns updatedTransaction

        val response = controller.updateTransaction(5L, request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(5L, response.body!!.id)
        assertEquals(BigDecimal("80.00"), response.body!!.amount)
        assertEquals("Updated lunch", response.body!!.description)
    }

    @Test
    fun shouldDeleteTransactionAndReturn204() {
        every {
            transactionRepository.existsById(5L)
        } returns true

        every {
            transactionRepository.deleteById(5L)
        } returns Unit

        val response = controller.deleteTransaction(5L)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        assertNull(response.body)

        verify(exactly = 1) {
            transactionRepository.deleteById(5L)
        }
    }

    @Test
    fun shouldReturn404WhenDeletingNonexistentTransaction() {
        every {
            transactionRepository.existsById(99L)
        } returns false

        val exception = assertThrows(ResponseStatusException::class.java) {
            controller.deleteTransaction(99L)
        }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)

        verify(exactly = 0) {
            transactionRepository.deleteById(any())
        }
    }

    @Test
    fun shouldImportTransactionsFromCSV() {
        val file = MockMultipartFile(
            "file",
            "transactions.csv",
            "text/csv",
            "type,amount,date,description,category".toByteArray()
        )

        val importResult = mockk<TransactionService.CsvImportResult>()

        every {
            transactionService.importCsv(file)
        } returns importResult

        val response = controller.importTransactions(file)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(importResult, response.body)

        verify(exactly = 1) {
            transactionService.importCsv(file)
        }
    }
}