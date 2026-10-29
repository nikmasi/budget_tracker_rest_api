package org.example.budget_tracker_api.budget

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.math.BigDecimal

class BudgetControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var budgetService: BudgetService
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        budgetService = mockk()
        objectMapper = ObjectMapper().findAndRegisterModules()

        mockMvc = MockMvcBuilders
            .standaloneSetup(BudgetController(budgetService))
            .build()
    }

    @Test
    fun shouldCreateOrUpdateBudgetSuccessfully() {
        val request = BudgetRequest(
            month = "2026-10",
            categoryId = 4L,
            amount = BigDecimal("300.00")
        )

        val response = BudgetResponse(
            id = 1L,
            month = "2026-10",
            categoryId = 4L,
            amount = BigDecimal("300.00"),
            categoryName = "Food"
        )

        every {
            budgetService.setBudget(any())
        } returns response

        mockMvc.perform(
            put("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.month").value("2026-10"))
            .andExpect(jsonPath("$.categoryId").value(4))
            .andExpect(jsonPath("$.amount").value(300.00))

        verify(exactly = 1) {
            budgetService.setBudget(any())
        }
    }

    @Test
    fun shouldReturnBadRequestWhenRequestBodyIsInvali() {
        val invalidRequest = """
            {
                "month": "",
                "categoryId": 4,
                "amount": -100
            }
        """.trimIndent()

        mockMvc.perform(
            put("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturnBudgetsForRequestedMonth() {
        val budgets = listOf(
            BudgetResponse(
                id = 1L,
                month = "2026-10",
                categoryId = 1L,
                amount = BigDecimal("300.00"),
                categoryName = "Food"
            ),
            BudgetResponse(
                id = 2L,
                month = "2026-10",
                categoryId = 2L,
                amount = BigDecimal("100.00"),
                categoryName = "Food"
            )
        )

        every {
            budgetService.getBudgets("2026-10")
        } returns budgets

        mockMvc.perform(
            get("/api/budgets")
                .param("month", "2026-10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].month").value("2026-10"))
            .andExpect(jsonPath("$[0].categoryId").value(1))
            .andExpect(jsonPath("$[0].amount").value(300.00))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].categoryId").value(2))

        verify(exactly = 1) {
            budgetService.getBudgets("2026-10")
        }
    }

    @Test
    fun shouldReturnEmptyListWhenMonthHasNoBudgets() {
        every {
            budgetService.getBudgets("2026-11")
        } returns emptyList()

        mockMvc.perform(
            get("/api/budgets")
                .param("month", "2026-11")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun shouldReturnBadRequestWhenMonthParameterIsMissing() {
        mockMvc.perform(
            get("/api/budgets")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldDeleteBudgetSuccessfully() {
        every {
            budgetService.deleteBudget("2026-10", 4L)
        } returns Unit

        mockMvc.perform(
            delete("/api/budgets")
                .param("month", "2026-10")
                .param("categoryId", "4")
        )
            .andExpect(status().isNoContent)

        verify(exactly = 1) {
            budgetService.deleteBudget("2026-10", 4L)
        }
    }

    @Test
    fun shouldReturnBadRequestWhenCategoryIdIsMissing() {
        mockMvc.perform(
            delete("/api/budgets")
                .param("month", "2026-10")
        )
            .andExpect(status().isBadRequest)
    }
}