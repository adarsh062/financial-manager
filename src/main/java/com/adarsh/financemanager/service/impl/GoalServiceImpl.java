package com.adarsh.financemanager.service.impl;

import com.adarsh.financemanager.dto.GoalRequest;
import com.adarsh.financemanager.dto.GoalResponse;
import com.adarsh.financemanager.dto.GoalUpdateRequest;
import com.adarsh.financemanager.entity.CategoryType;
import com.adarsh.financemanager.entity.Goal;
import com.adarsh.financemanager.entity.User;
import com.adarsh.financemanager.exception.ResourceNotFoundException;
import com.adarsh.financemanager.repository.GoalRepository;
import com.adarsh.financemanager.repository.TransactionRepository;
import com.adarsh.financemanager.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public GoalResponse createGoal(GoalRequest request, User user) {
        // Use provided startDate or default to today
        LocalDate startDate = request.getStartDate() != null
                ? request.getStartDate()
                : LocalDate.now();

        if (startDate.isAfter(request.getTargetDate())) {
            throw new IllegalArgumentException("Start date cannot be after target date");
        }

        Goal goal = Goal.builder()
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .startDate(startDate)
                .user(user)
                .build();

        return toResponse(goalRepository.save(goal), user);
    }

    @Override
    public List<GoalResponse> getGoals(User user) {
        return goalRepository.findAllByUserOrderByTargetDateAsc(user)
                .stream()
                .map(goal -> toResponse(goal, user))
                .toList();
    }

    @Override
    public GoalResponse getGoalById(Long id, User user) {
        Goal goal = findOwnedGoal(id, user);
        return toResponse(goal, user);
    }

    @Override
    @Transactional
    public GoalResponse updateGoal(Long id, GoalUpdateRequest request, User user) {
        Goal goal = findOwnedGoal(id, user);

        if (request.getTargetAmount() != null) {
            if (request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Target amount must be greater than 0");
            }
            goal.setTargetAmount(request.getTargetAmount());
        }
        if (request.getTargetDate() != null) {
            if (!request.getTargetDate().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Target date must be in the future");
            }
            goal.setTargetDate(request.getTargetDate());
        }

        if (goal.getStartDate().isAfter(goal.getTargetDate())) {
            throw new IllegalArgumentException("Start date cannot be after target date");
        }

        return toResponse(goalRepository.save(goal), user);
    }

    @Override
    @Transactional
    public void deleteGoal(Long id, User user) {
        Goal goal = findOwnedGoal(id, user);
        goalRepository.delete(goal);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Goal findOwnedGoal(Long id, User user) {
        return goalRepository.findByIdAndUser(id, user)
                .orElseGet(() -> {
                    Goal goal = goalRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Goal not found with id: " + id
                            ));
                    if (!goal.getUser().getId().equals(user.getId())) {
                        throw new com.adarsh.financemanager.exception.ForbiddenAccessException(
                                "You do not have permission to access this goal"
                        );
                    }
                    return goal;
                });
    }

    /**
     * Computes progress for a goal:
     *   currentProgress = SUM(INCOME) - SUM(EXPENSE) since goal.startDate
     */
    private GoalResponse toResponse(Goal goal, User user) {
        BigDecimal totalIncome = transactionRepository.sumByUserAndTypeFromDate(
                user, CategoryType.INCOME, goal.getStartDate());
        BigDecimal totalExpense = transactionRepository.sumByUserAndTypeFromDate(
                user, CategoryType.EXPENSE, goal.getStartDate());

        BigDecimal currentProgress = totalIncome.subtract(totalExpense);
        // Clamp to 0 — negative savings shouldn't show as negative progress
        if (currentProgress.compareTo(BigDecimal.ZERO) < 0) {
            currentProgress = BigDecimal.ZERO;
        }

        BigDecimal targetAmount = goal.getTargetAmount();
        BigDecimal progressPercentage = BigDecimal.ZERO;
        if (targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = currentProgress
                    .multiply(new BigDecimal("100"))
                    .divide(targetAmount, 2, RoundingMode.HALF_UP);
            // Cap at 100%
            if (progressPercentage.compareTo(new BigDecimal("100")) > 0) {
                progressPercentage = new BigDecimal("100");
            }
        }

        BigDecimal remainingAmount = targetAmount.subtract(currentProgress);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        BigDecimal formattedProgress = currentProgress.compareTo(BigDecimal.ZERO) == 0
                ? new BigDecimal("0")
                : currentProgress.setScale(2, RoundingMode.HALF_UP);

        BigDecimal formattedRemaining = remainingAmount.compareTo(BigDecimal.ZERO) == 0
                ? new BigDecimal("0")
                : remainingAmount.setScale(2, RoundingMode.HALF_UP);

        BigDecimal roundedPercentage = progressPercentage.setScale(2, RoundingMode.HALF_UP);
        BigDecimal strippedPercentage = roundedPercentage.stripTrailingZeros();
        BigDecimal formattedPercentage = strippedPercentage.scale() < 1
                ? strippedPercentage.setScale(1, RoundingMode.HALF_UP)
                : strippedPercentage;

        return GoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(targetAmount.setScale(2, RoundingMode.HALF_UP))
                .targetDate(goal.getTargetDate())
                .startDate(goal.getStartDate())
                .currentProgress(formattedProgress)
                .progressPercentage(formattedPercentage)
                .remainingAmount(formattedRemaining)
                .build();
    }
}
