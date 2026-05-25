package com.adarsh.financemanager.controller;

import com.adarsh.financemanager.dto.TransactionRequest;
import com.adarsh.financemanager.dto.TransactionResponse;
import com.adarsh.financemanager.dto.TransactionUpdateRequest;
import com.adarsh.financemanager.entity.User;
import com.adarsh.financemanager.security.SecurityUtils;
import com.adarsh.financemanager.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request
    ) {
        User user = securityUtils.getCurrentUser();

        TransactionResponse response =
                transactionService.createTransaction(request, user);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Map<String, List<TransactionResponse>>> getTransactions(

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            String category

    ) {

        User user = securityUtils.getCurrentUser();

        List<TransactionResponse> transactions =
                transactionService.getTransactions(
                        user,
                        startDate,
                        endDate,
                        category
                );

        return ResponseEntity.ok(
                Map.of("transactions", transactions)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request
    ) {

        User user = securityUtils.getCurrentUser();

        TransactionResponse response =
                transactionService.updateTransaction(id, request, user);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTransaction(
            @PathVariable Long id
    ) {

        User user = securityUtils.getCurrentUser();

        transactionService.deleteTransaction(id, user);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Transaction deleted successfully"
                )
        );
    }
}