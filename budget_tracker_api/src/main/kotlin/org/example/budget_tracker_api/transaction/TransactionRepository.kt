package org.example.budget_tracker_api.transaction

import org.example.budget_tracker_api.database.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface TransactionRepository : JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    fun existsByCategoryId(categoryId: Long): Boolean

    fun findByDateBetween(startDate: LocalDate, endDate: LocalDate): List<Transaction>
}