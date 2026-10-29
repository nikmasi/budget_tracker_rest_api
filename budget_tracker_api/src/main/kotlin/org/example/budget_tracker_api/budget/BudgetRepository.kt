package org.example.budget_tracker_api.budget

import org.example.budget_tracker_api.database.Budget
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BudgetRepository : JpaRepository<Budget, Long> {

    fun findAllByMonthOrderByCategory_NameAsc(month: String): List<Budget>

    fun findByMonthAndCategory_Id(month: String, categoryId: Long): Budget?

    fun deleteByMonthAndCategory_Id(month: String, categoryId: Long): Long

    fun findAllByMonth(month: String): List<Budget>

    fun findByMonth(month: String): List<Budget>

    fun deleteAllByCategoryId(categoryId: Long)
}

