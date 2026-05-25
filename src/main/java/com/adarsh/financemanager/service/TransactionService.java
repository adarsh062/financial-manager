// TransactionService.java

package com.adarsh.financemanager.service;

import com.adarsh.financemanager.dto.TransactionRequest;
import com.adarsh.financemanager.dto.TransactionResponse;
import com.adarsh.financemanager.dto.TransactionUpdateRequest;
import com.adarsh.financemanager.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    TransactionResponse createTransaction(TransactionRequest request, User user);

    List<TransactionResponse> getTransactions(
            User user,
            LocalDate startDate,
            LocalDate endDate,
            String category
    );

    TransactionResponse updateTransaction(
            Long id,
            TransactionUpdateRequest request,
            User user
    );

    void deleteTransaction(Long id, User user);
}