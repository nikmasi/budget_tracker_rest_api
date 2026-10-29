package org.example.budget_tracker_api.category

import org.example.budget_tracker_api.database.Category
import org.example.budget_tracker_api.transaction.TransactionRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CategoryServiceTest {

    @Mock
    private lateinit var categoryRepository: CategoryRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @InjectMocks
    private lateinit var categoryService: CategoryService

    private lateinit var category: Category

    @BeforeEach
    fun setUp() {
        category = Category(
            id = 1L,
            name = "Food",
            color = "#FF0000"
        )
    }

    @Test
    fun getAllCategoriesReturnsSortedCategoriesFromRepository() {
        `when`(categoryRepository.findAllByOrderByNameAsc())
            .thenReturn(listOf(category))

        val result = categoryService.getAllCategories()

        assertEquals(1, result.size)
        assertEquals("Food", result[0].name)

        verify(categoryRepository).findAllByOrderByNameAsc()
    }

    @Test
    fun createCategoryTrimsNameAndSavesCategory() {
        val newCategory = Category(
            name = "  Food  ",
            color = "#FF0000"
        )

        `when`(
            categoryRepository.existsByNameIgnoreCase("Food")
        ).thenReturn(false)

        `when`(categoryRepository.save(any(Category::class.java)))
            .thenAnswer { invocation ->
                invocation.getArgument<Category>(0)
            }

        val result = categoryService.createCategory(newCategory)

        assertEquals("Food", result.name)
        verify(categoryRepository).save(newCategory)
    }

    @Test
    fun createCategoryRejectsDuplicateName() {
        val newCategory = Category(
            name = "food",
            color = "#FF0000"
        )

        `when`(
            categoryRepository.existsByNameIgnoreCase("food")
        ).thenReturn(true)

        assertThrows(ResponseStatusException::class.java) {
            categoryService.createCategory(newCategory)
        }

        verify(categoryRepository, never())
            .save(any(Category::class.java))
    }

    @Test
    fun updateCategoryChangesNameAndColor() {
        val updatedData = Category(
            name = "Groceries",
            color = "#00FF00"
        )

        `when`(categoryRepository.findById(1L))
            .thenReturn(Optional.of(category))

        `when`(
            categoryRepository.findByNameIgnoreCase("Groceries")
        ).thenReturn(Optional.empty())

        `when`(categoryRepository.save(any(Category::class.java)))
            .thenAnswer { invocation ->
                invocation.getArgument<Category>(0)
            }

        val result = categoryService.updateCategory(1L, updatedData)

        assertEquals("Groceries", result.name)
        assertEquals("#00FF00", result.color)

        verify(categoryRepository).save(category)
    }

    @Test
    fun updateCategoryRejectsDuplicateNameBelongingToAnotherCategory() {
        val anotherCategory = Category(
            id = 2L,
            name = "Groceries",
            color = "#00FF00"
        )

        val updatedData = Category(
            name = "Groceries",
            color = "#FF0000"
        )

        `when`(categoryRepository.findById(1L))
            .thenReturn(Optional.of(category))

        `when`(
            categoryRepository.findByNameIgnoreCase("Groceries")
        ).thenReturn(Optional.of(anotherCategory))

        assertThrows(ResponseStatusException::class.java) {
            categoryService.updateCategory(1L, updatedData)
        }

        verify(categoryRepository, never())
            .save(any(Category::class.java))
    }

    @Test
    fun getCategoryThrowsExceptionWhenCategoryDoesNotExist() {
        `when`(categoryRepository.findById(99L))
            .thenReturn(Optional.empty())

        val exception = assertThrows(
            ResponseStatusException::class.java
        ) {
            categoryService.getCategoryById(99L)
        }

        assertEquals(404, exception.statusCode.value())
    }

    @Test
    fun deleteCategoryRejectsCategoryWithTransactions() {
        `when`(categoryRepository.findById(1L))
            .thenReturn(Optional.of(category))

        `when`(transactionRepository.existsByCategoryId(1L))
            .thenReturn(true)

        val exception = assertThrows(
            ResponseStatusException::class.java
        ) {
            categoryService.deleteCategory(1L)
        }

        assertEquals(400, exception.statusCode.value())

        verify(categoryRepository, never()).delete(category)
    }

    @Test
    fun deleteCategorySucceedsWhenItHasNoTransactions() {
        `when`(categoryRepository.findById(1L))
            .thenReturn(Optional.of(category))

        `when`(transactionRepository.existsByCategoryId(1L))
            .thenReturn(false)

        categoryService.deleteCategory(1L)

        verify(categoryRepository).delete(category)
    }


    @Test
    fun createCategoryRejectsBlankName() {
        val newCategory = Category(
            name = "   ",
            color = "#FF0000"
        )

        val exception = assertThrows(
            ResponseStatusException::class.java
        ) {
            categoryService.createCategory(newCategory)
        }

        assertEquals(400, exception.statusCode.value())

        verify(categoryRepository, never())
            .save(any(Category::class.java))
    }


    @Test
    fun updateCategoryThrowsExceptionWhenCategoryDoesNotExist() {
        `when`(categoryRepository.findById(99L))
            .thenReturn(Optional.empty())

        val updatedData = Category(
            name = "Groceries",
            color = "#00FF00"
        )

        val exception = assertThrows(
            ResponseStatusException::class.java
        ) {
            categoryService.updateCategory(99L, updatedData)
        }

        assertEquals(404, exception.statusCode.value())

        verify(categoryRepository, never())
            .save(any(Category::class.java))
    }


    @Test
    fun deleteCategoryThrowsExceptionWhenCategoryDoesNotExist() {
        `when`(categoryRepository.findById(99L))
            .thenReturn(Optional.empty())

        val exception = assertThrows(
            ResponseStatusException::class.java
        ) {
            categoryService.deleteCategory(99L)
        }

        assertEquals(404, exception.statusCode.value())

        verify(transactionRepository, never())
            .existsByCategoryId(99L)

        verify(categoryRepository, never())
            .delete(any(Category::class.java))
    }

}