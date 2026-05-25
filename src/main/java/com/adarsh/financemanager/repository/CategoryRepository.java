package com.adarsh.financemanager.repository;

import com.adarsh.financemanager.entity.Category;
import com.adarsh.financemanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Returns all non-deleted categories visible to the user:
     * - All default categories (user IS NULL, isCustom = false)
     * - All custom categories belonging to this user
     */
    @Query("""
            SELECT c FROM Category c
            WHERE c.isDeleted = false
              AND (c.isCustom = false OR c.user = :user)
            ORDER BY c.isCustom ASC, c.name ASC
            """)
    List<Category> findAllVisibleForUser(@Param("user") User user);

    /**
     * Find a category by name belonging to a specific user (custom only).
     */
    Optional<Category> findByNameAndUser(String name, User user);

    /**
     * Find a default (non-custom) category by name.
     */
    Optional<Category> findByNameAndIsCustomFalse(String name);

    /**
     * Check if a custom category with the given name already exists for this user.
     */
    boolean existsByNameAndUserAndIsDeletedFalse(String name, User user);

    /**
     * Check if a default category with the given name exists (idempotency in seeder).
     */
    boolean existsByNameAndIsCustomFalse(String name);

    /**
     * Find any visible category (default or user's own custom) by name.
     * Used when creating transactions via category name.
     */
    @Query("""
            SELECT c FROM Category c
            WHERE c.isDeleted = false
              AND c.name = :name
              AND (c.isCustom = false OR c.user = :user)
            """)
    Optional<Category> findVisibleByNameAndUser(@Param("name") String name, @Param("user") User user);
}
