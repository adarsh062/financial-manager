package com.adarsh.financemanager.controller;

import com.adarsh.financemanager.dto.GoalRequest;
import com.adarsh.financemanager.dto.GoalResponse;
import com.adarsh.financemanager.dto.GoalUpdateRequest;
import com.adarsh.financemanager.entity.User;
import com.adarsh.financemanager.security.SecurityUtils;
import com.adarsh.financemanager.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(
            @Valid @RequestBody GoalRequest request
    ) {
        User user = securityUtils.getCurrentUser();
        return new ResponseEntity<>(
                goalService.createGoal(request, user),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<Map<String, List<GoalResponse>>> getGoals() {
        User user = securityUtils.getCurrentUser();
        List<GoalResponse> goals = goalService.getGoals(user);
        return ResponseEntity.ok(Map.of("goals", goals));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoalById(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser();
        return ResponseEntity.ok(goalService.getGoalById(id, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody GoalUpdateRequest request
    ) {
        User user = securityUtils.getCurrentUser();
        return ResponseEntity.ok(goalService.updateGoal(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser();
        goalService.deleteGoal(id, user);
        return ResponseEntity.ok(Map.of("message", "Goal deleted successfully"));
    }
}
