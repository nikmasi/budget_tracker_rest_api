package org.example.budget_tracker_api.category

import org.example.budget_tracker_api.database.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByNameIgnoreCase(name: String): Optional<Category>

    fun existsByNameIgnoreCase(name: String): Boolean

    fun findAllByOrderByNameAsc(): List<Category>
}