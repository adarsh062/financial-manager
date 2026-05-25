package com.adarsh.financemanager.repository;

import com.adarsh.financemanager.dto.CategorySummary;
import com.adarsh.financemanager.entity.Category;
import com.adarsh.financemanager.entity.CategoryType;
import com.adarsh.financemanager.entity.Transaction;
import com.adarsh.financemanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // ── Basic lookups ─────────────────────────────────────────────────────────

    List<Transaction> findAllByUserOrderByDateDesc(User user);

    // ADDED FOR CATEGORY FILTER FIX
    List<Transaction> findByUserOrderByDateDesc(User user);

    Optional<Transaction> findByIdAndUser(Long id, User user);

    boolean existsByCategoryAndUser(Category category, User user);

    // ── Filtered listing ──────────────────────────────────────────────────────

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.user = :user
              AND (:startDate IS NULL OR t.date >= :startDate)
              AND (:endDate   IS NULL OR t.date <= :endDate)
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
            ORDER BY t.date DESC, t.id DESC
            """)
    List<Transaction> findFiltered(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Long categoryId
    );

    // ── Aggregations for goals & reports ──────────────────────────────────────

    /**
     * Sum of all transactions of a given type for a user, on or after fromDate.
     */
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.user = :user
              AND t.category.type = :type
              AND t.date >= :fromDate
            """)
    BigDecimal sumByUserAndTypeFromDate(
            @Param("user") User user,
            @Param("type") CategoryType type,
            @Param("fromDate") LocalDate fromDate
    );

    /**
     * Monthly grouped totals by category (for reports).
     */
    @Query("""
            SELECT new com.adarsh.financemanager.dto.CategorySummary(
                t.category.name,
                t.category.type,
                SUM(t.amount)
            )
            FROM Transaction t
            WHERE t.user = :user
              AND YEAR(t.date) = :year
              AND MONTH(t.date) = :month
              AND t.category.type = :type
            GROUP BY t.category.id, t.category.name, t.category.type
            ORDER BY SUM(t.amount) DESC
            """)
    List<CategorySummary> getMonthlyGroupedByCategory(
            @Param("user") User user,
            @Param("year") int year,
            @Param("month") int month,
            @Param("type") CategoryType type
    );

    /**
     * Yearly total by type.
     */
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.user = :user
              AND YEAR(t.date) = :year
              AND t.category.type = :type
            """)
    BigDecimal getYearlyTotal(
            @Param("user") User user,
            @Param("year") int year,
            @Param("type") CategoryType type
    );

    /**
     * Yearly grouped totals by category (for yearly report).
     */
    @Query("""
            SELECT new com.adarsh.financemanager.dto.CategorySummary(
                t.category.name,
                t.category.type,
                SUM(t.amount)
            )
            FROM Transaction t
            WHERE t.user = :user
              AND YEAR(t.date) = :year
              AND t.category.type = :type
            GROUP BY t.category.id, t.category.name, t.category.type
            ORDER BY SUM(t.amount) DESC
            """)
    List<CategorySummary> getYearlyGroupedByCategory(
            @Param("user") User user,
            @Param("year") int year,
            @Param("type") CategoryType type
    );
}