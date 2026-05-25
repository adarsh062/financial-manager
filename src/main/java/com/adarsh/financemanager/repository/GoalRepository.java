package com.adarsh.financemanager.repository;

import com.adarsh.financemanager.entity.Goal;
import com.adarsh.financemanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findAllByUserOrderByTargetDateAsc(User user);

    Optional<Goal> findByIdAndUser(Long id, User user);
}
