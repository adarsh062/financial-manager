package com.adarsh.financemanager.mapper;

/**
 * Mapping strategy note:
 *
 * This project uses static factory methods (e.g. {@code CategoryResponse.from(Category)})
 * directly on the DTO classes instead of dedicated mapper classes.
 *
 * This is intentional — it keeps mapping logic co-located with the DTO and avoids
 * adding a MapStruct dependency for a project of this scale.
 *
 * If the project grows and mapping logic becomes complex, consider migrating to
 * MapStruct mapper interfaces placed in this package.
 *
 * Current mapping locations:
 * <ul>
 *   <li>{@code CategoryResponse#from(Category)}</li>
 *   <li>{@code TransactionResponse#from(Transaction)}</li>
 *   <li>{@code GoalResponse} — built inline in {@code GoalServiceImpl#toResponse(...)}</li>
 * </ul>
 */
public final class MappingStrategy {
    private MappingStrategy() {
        // utility class — not instantiable
    }
}
