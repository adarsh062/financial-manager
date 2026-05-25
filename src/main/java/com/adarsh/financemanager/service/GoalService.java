package com.adarsh.financemanager.service;

import com.adarsh.financemanager.dto.GoalRequest;
import com.adarsh.financemanager.dto.GoalResponse;
import com.adarsh.financemanager.dto.GoalUpdateRequest;
import com.adarsh.financemanager.entity.User;

import java.util.List;

public interface GoalService {

    GoalResponse createGoal(GoalRequest request, User user);

    List<GoalResponse> getGoals(User user);

    GoalResponse getGoalById(Long id, User user);

    GoalResponse updateGoal(Long id, GoalUpdateRequest request, User user);

    void deleteGoal(Long id, User user);
}
