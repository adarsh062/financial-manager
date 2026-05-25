package com.adarsh.financemanager.config;

import com.adarsh.financemanager.entity.Category;
import com.adarsh.financemanager.entity.CategoryType;
import com.adarsh.financemanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds default categories on every application startup.
 * Idempotent — will not insert duplicates.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    private static final List<Object[]> DEFAULT_CATEGORIES = List.of(
            new Object[]{"Salary",         CategoryType.INCOME},
            new Object[]{"Food",           CategoryType.EXPENSE},
            new Object[]{"Rent",           CategoryType.EXPENSE},
            new Object[]{"Transportation", CategoryType.EXPENSE},
            new Object[]{"Entertainment",  CategoryType.EXPENSE},
            new Object[]{"Healthcare",     CategoryType.EXPENSE},
            new Object[]{"Utilities",      CategoryType.EXPENSE}
    );

    @Override
    public void run(String... args) {
        int seeded = 0;
        for (Object[] row : DEFAULT_CATEGORIES) {
            String name = (String) row[0];
            CategoryType type = (CategoryType) row[1];

            if (!categoryRepository.existsByNameAndIsCustomFalse(name)) {
                categoryRepository.save(
                        Category.builder()
                                .name(name)
                                .type(type)
                                .isCustom(false)
                                .isDeleted(false)
                                .user(null)
                                .build()
                );
                seeded++;
            }
        }
        if (seeded > 0) {
            log.info("DataSeeder: seeded {} default categories", seeded);
        } else {
            log.info("DataSeeder: default categories already present, skipping");
        }
    }
}
