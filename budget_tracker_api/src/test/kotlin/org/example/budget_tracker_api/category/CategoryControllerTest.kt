package org.example.budget_tracker_api.category


import org.example.budget_tracker_api.configuration.JacksonConfig
import org.example.budget_tracker_api.exception.ExceptionHandler
import org.example.budget_tracker_api.transaction.TransactionRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(CategoryController::class)
@Import(JacksonConfig::class, ExceptionHandler::class)
class CategoryControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var categoryRepository: CategoryRepository

    @MockitoBean
    lateinit var transactionRepository: TransactionRepository

    @Test
    fun shouldReturn400WhenNameIsNumber() {
        mockMvc.post("/api/categories") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "name": 1,
                    "color": "#FF5733"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.error") { value("Bad Request") }
            jsonPath("$.message") {
                value("Invalid value for field 'name'. Expected a string.")
            }
            jsonPath("$.path") { value("/api/categories") }
        }
    }

    @Test
    fun shouldReturn400WhenColorIsNumber() {
        mockMvc.post("/api/categories") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "name": "Eating out",
                    "color": 123
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.message") {
                value("Invalid value for field 'color'. Expected a string.")
            }
        }
    }

    @Test
    fun shouldReturn400WhenNameIsBlank() {
        mockMvc.post("/api/categories") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "name": "   ",
                    "color": "#FF5733"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.name") {
                value("Category name is required")
            }
        }
    }

    @Test
    fun shouldReturn400WhenColorHasInvalidFormat() {
        mockMvc.post("/api/categories") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "name": "Eating out",
                    "color": "red"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.color") {
                value("Color must be in #RRGGBB format")
            }
        }
    }
}
