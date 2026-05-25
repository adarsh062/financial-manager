package com.adarsh.financemanager.service;

import com.adarsh.financemanager.dto.LoginRequest;
import com.adarsh.financemanager.dto.LoginResponse;
import com.adarsh.financemanager.dto.LogoutResponse;
import com.adarsh.financemanager.dto.RegisterRequest;
import com.adarsh.financemanager.dto.RegisterResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    LogoutResponse logout();
}