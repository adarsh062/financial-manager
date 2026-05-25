package com.adarsh.financemanager.service.impl;

import com.adarsh.financemanager.dto.GoalRequest;
import com.adarsh.financemanager.dto.GoalResponse;
import com.adarsh.financemanager.entity.CategoryType;
import com.adarsh.financemanager.entity.Goal;
import com.adarsh.financemanager.entity.User;
import com.adarsh.financemanager.exception.ResourceNotFoundException;
import com.adarsh.financemanager.repository.GoalRepository;
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
@DisplayName("GoalServiceImpl Tests")
class GoalServiceImplTest {

    @Mock private GoalRepository goalRepository;
    @Mock private TransactionRepository transactionRepository;

    @InjectMocks private GoalServiceImpl goalService;

    private User user;
    private Goal goal;
    private GoalRequest request;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();
        goal = Goal.builder()
                .id(1L).goalName("Emergency Fund")
                .targetAmount(new BigDecimal("50000.00"))
                .targetDate(LocalDate.now().plusYears(1))
                .startDate(LocalDate.now())
                .user(user).build();

        request = new GoalRequest();
        request.setGoalName("Emergency Fund");
        request.setTargetAmount(new BigDecimal("50000.00"));
        request.setTargetDate(LocalDate.now().plusYears(1));
    }

    // ── createGoal ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createGoal: success — calculates zero progress for new goal")
    void createGoal_success() {
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);
        when(transactionRepository.sumByUserAndTypeFromDate(eq(user), eq(CategoryType.INCOME), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumByUserAndTypeFromDate(eq(user), eq(CategoryType.EXPENSE), any()))
                .thenReturn(BigDecimal.ZERO);

        GoalResponse response = goalService.createGoal(request, user);

        assertThat(response.getGoalName()).isEqualTo("Emergency Fund");
        assertThat(response.getCurrentProgress()).isEqualByComparingTo("0.00");
        assertThat(response.getProgressPercentage()).isEqualByComparingTo("0.00");
        assertThat(response.getRemainingAmount()).isEqualByComparingTo("50000.00");
    }

    @Test
    @DisplayName("createGoal: with positive savings — progress > 0%")
    void createGoal_withSavings_progressCalculated() {
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);
        when(transactionRepository.sumByUserAndTypeFromDate(eq(user), eq(CategoryType.INCOME), any()))
                .thenReturn(new BigDecimal("25000.00"));
        when(transactionRepository.sumByUserAndTypeFromDate(eq(user), eq(CategoryType.EXPENSE), any()))
                .thenReturn(new BigDecimal("10000.00"));

        GoalResponse response = goalService.createGoal(request, user);

        assertThat(response.getCurrentProgress()).isEqualByComparingTo("15000.00");
        assertThat(response.getProgressPercentage()).isEqualByComparingTo("30.00");
        assertThat(response.getRemainingAmount()).isEqualByComparingTo("35000.00");
    }

    // ── getGoalById ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getGoalById: not found → throws ResourceNotFoundException")
    void getGoalById_notFound_throws() {
        when(goalRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.getGoalById(99L, user))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── deleteGoal ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteGoal: success")
    void deleteGoal_success() {
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));

        goalService.deleteGoal(1L, user);

        verify(goalRepository).delete(goal);
    }

    // ── getGoals ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getGoals: returns list for user")
    void getGoals_returnsList() {
        when(goalRepository.findAllByUserOrderByTargetDateAsc(user)).thenReturn(List.of(goal));
        when(transactionRepository.sumByUserAndTypeFromDate(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        List<GoalResponse> result = goalService.getGoals(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGoalName()).isEqualTo("Emergency Fund");
    }
}
