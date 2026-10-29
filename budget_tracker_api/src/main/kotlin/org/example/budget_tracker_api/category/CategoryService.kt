package org.example.budget_tracker_api.category

import org.example.budget_tracker_api.database.Category
import org.example.budget_tracker_api.transaction.TransactionRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) {
    fun getAllCategories(): List<Category> {
        return categoryRepository.findAllByOrderByNameAsc()
    }

    fun getCategoryById(id: Long): Category {
        return categoryRepository.findById(id)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found.")
            }
    }

    @Transactional
    fun createCategory(category: Category): Category {
        category.name = category.name.trim()

        if (category.name.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name must not be empty.")
        }

        if (categoryRepository.existsByNameIgnoreCase(category.name)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Category with this name already exists.")
        }

        return categoryRepository.save(category)
    }

    @Transactional
    fun updateCategory(id: Long, updatedData: Category): Category {
        val category = getCategoryById(id)
        val newName = updatedData.name.trim()

        if (newName.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name must not be empty.")
        }

        val existingWithSameName = categoryRepository.findByNameIgnoreCase(newName)

        if (existingWithSameName.isPresent && existingWithSameName.get().id != id) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Category with this name already exists.")
        }

        category.name = newName
        category.color = updatedData.color

        return categoryRepository.save(category)
    }

    @Transactional
    fun deleteCategory(id: Long) {
        val category = getCategoryById(id)

        if (transactionRepository.existsByCategoryId(id)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "A category that has transactions cannot be deleted.")
        }

        categoryRepository.delete(category)
    }
}