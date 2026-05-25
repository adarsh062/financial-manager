// TransactionServiceImpl.java

package com.adarsh.financemanager.service.impl;

import com.adarsh.financemanager.dto.TransactionRequest;
import com.adarsh.financemanager.dto.TransactionResponse;
import com.adarsh.financemanager.dto.TransactionUpdateRequest;
import com.adarsh.financemanager.entity.Category;
import com.adarsh.financemanager.entity.Transaction;
import com.adarsh.financemanager.entity.User;
import com.adarsh.financemanager.exception.ResourceNotFoundException;
import com.adarsh.financemanager.repository.CategoryRepository;
import com.adarsh.financemanager.repository.TransactionRepository;
import com.adarsh.financemanager.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public TransactionResponse createTransaction(
            TransactionRequest request,
            User user
    ) {

        // Resolve category by name (default or user's own custom)
        Category category = resolveCategoryByName(
                request.getCategory(),
                user
        );

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .description(request.getDescription())
                .category(category)
                .user(user)
                .build();

        return TransactionResponse.from(
                transactionRepository.save(transaction)
        );
    }

    @Override
    public List<TransactionResponse> getTransactions(
            User user,
            LocalDate startDate,
            LocalDate endDate,
            String category
    ) {

        List<Transaction> transactions =
                transactionRepository.findByUserOrderByDateDesc(user);

        // Filter by start date
        if (startDate != null) {
            transactions = transactions.stream()
                    .filter(t ->
                            !t.getDate().isBefore(startDate)
                    )
                    .toList();
        }

        // Filter by end date
        if (endDate != null) {
            transactions = transactions.stream()
                    .filter(t ->
                            !t.getDate().isAfter(endDate)
                    )
                    .toList();
        }

        // Filter by category name
        if (category != null && !category.isBlank()) {
            transactions = transactions.stream()
                    .filter(t ->
                            t.getCategory()
                                    .getName()
                                    .equalsIgnoreCase(category)
                    )
                    .toList();
        }

        return transactions.stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(
            Long id,
            TransactionUpdateRequest request,
            User user
    ) {

        Transaction transaction =
                findOwnedTransaction(id, user);

        if (request.getAmount() != null) {
            if (request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be greater than 0");
            }
            transaction.setAmount(request.getAmount());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        return TransactionResponse.from(
                transactionRepository.save(transaction)
        );
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id, User user) {

        Transaction transaction =
                findOwnedTransaction(id, user);

        transactionRepository.delete(transaction);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Resolves a category by name for the given user.
     * Matches default categories (system-wide)
     * or the user's own custom categories.
     */
    private Category resolveCategoryByName(
            String name,
            User user
    ) {

        return categoryRepository
                .findVisibleByNameAndUser(name, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found with name: " + name
                        )
                );
    }

    private Transaction findOwnedTransaction(
            Long id,
            User user
    ) {
        return transactionRepository.findByIdAndUser(id, user)
                .orElseGet(() -> {
                    Transaction transaction = transactionRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Transaction not found with id: " + id
                            ));
                    if (!transaction.getUser().getId().equals(user.getId())) {
                        throw new com.adarsh.financemanager.exception.ForbiddenAccessException(
                                "You do not have permission to access this transaction"
                        );
                    }
                    return transaction;
                });
    }
}