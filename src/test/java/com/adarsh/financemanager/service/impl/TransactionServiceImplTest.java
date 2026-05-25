package com.adarsh.financemanager.service.impl;

import com.adarsh.financemanager.dto.TransactionRequest;
import com.adarsh.financemanager.dto.TransactionResponse;
import com.adarsh.financemanager.dto.TransactionUpdateRequest;
import com.adarsh.financemanager.entity.*;
import com.adarsh.financemanager.exception.ResourceNotFoundException;
import com.adarsh.financemanager.repository.CategoryRepository;
import com.adarsh.financemanager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionServiceImpl Tests")
class TransactionServiceImplTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private TransactionServiceImpl transactionService;

    private User user;
    private Category category;
    private Transaction transaction;
    private TransactionRequest request;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();
        category = Category.builder()
                .id(1L).name("Food").type(CategoryType.EXPENSE)
                .isCustom(false).isDeleted(false).build();
        transaction = Transaction.builder()
                .id(1L).amount(new BigDecimal("500.00"))
                .date(LocalDate.now()).description("Groceries")
                .category(category).user(user).build();

        request = new TransactionRequest();
        request.setAmount(new BigDecimal("500.00"));
        request.setDate(LocalDate.now());
        request.setDescription("Groceries");
        request.setCategory("Food");
    }

    // ── createTransaction ─────────────────────────────────────────────────────

    @Test
    @DisplayName("createTransaction: success")
    void createTransaction_success() {
        when(categoryRepository.findVisibleByNameAndUser("Food", user)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.createTransaction(request, user);

        assertThat(response.getAmount()).isEqualByComparingTo("500.00");
        assertThat(response.getCategory()).isEqualTo("Food");
        verify(transactionRepository).save(any());
    }

    @Test
    @DisplayName("createTransaction: invalid category name → throws ResourceNotFoundException")
    void createTransaction_invalidCategory_throws() {
        when(categoryRepository.findVisibleByNameAndUser("InvalidCategory", user)).thenReturn(Optional.empty());
        request.setCategory("InvalidCategory");

        assertThatThrownBy(() -> transactionService.createTransaction(request, user))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getTransactions ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getTransactions: returns list with filters")
    void getTransactions_returnsFilteredList() {
        when(transactionRepository.findFiltered(eq(user), any(), any(), any()))
                .thenReturn(List.of(transaction));

        List<TransactionResponse> result = transactionService.getTransactions(user, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Groceries");
    }

    // ── updateTransaction ─────────────────────────────────────────────────────

    @Test
    @DisplayName("updateTransaction: success — amount and description updated, date unchanged")
    void updateTransaction_success_dateNotChanged() {
        LocalDate originalDate = LocalDate.of(2025, 1, 1);
        transaction.setDate(originalDate);

        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any())).thenReturn(transaction);

        TransactionUpdateRequest updateRequest = new TransactionUpdateRequest();
        updateRequest.setAmount(new BigDecimal("999.00"));
        updateRequest.setDescription("Updated");

        transactionService.updateTransaction(1L, updateRequest, user);

        // Date must NOT have changed
        assertThat(transaction.getDate()).isEqualTo(originalDate);
        assertThat(transaction.getAmount()).isEqualByComparingTo("999.00");
        assertThat(transaction.getDescription()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("updateTransaction: not found → throws ResourceNotFoundException")
    void updateTransaction_notFound_throws() {
        when(transactionRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        TransactionUpdateRequest updateRequest = new TransactionUpdateRequest();
        updateRequest.setAmount(new BigDecimal("999.00"));

        assertThatThrownBy(() -> transactionService.updateTransaction(99L, updateRequest, user))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── deleteTransaction ─────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteTransaction: success")
    void deleteTransaction_success() {
        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(transaction));

        transactionService.deleteTransaction(1L, user);

        verify(transactionRepository).delete(transaction);
    }

    @Test
    @DisplayName("deleteTransaction: not found → throws ResourceNotFoundException")
    void deleteTransaction_notFound_throws() {
        when(transactionRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(99L, user))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
