package com.adarsh.financemanager.service.impl;

import com.adarsh.financemanager.dto.LoginRequest;
import com.adarsh.financemanager.dto.LoginResponse;
import com.adarsh.financemanager.dto.LogoutResponse;
import com.adarsh.financemanager.dto.RegisterRequest;
import com.adarsh.financemanager.dto.RegisterResponse;
import com.adarsh.financemanager.entity.User;
import com.adarsh.financemanager.exception.ResourceAlreadyExistsException;
import com.adarsh.financemanager.repository.UserRepository;
import com.adarsh.financemanager.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final HttpSession httpSession;

    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("User already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                "User registered successfully",
                savedUser.getId()
        );
    }

    @Override
    public LoginResponse login(LoginRequest request,
                               HttpServletRequest httpRequest,
                               HttpServletResponse httpResponse) {

        // 1. Authenticate credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2. Store in SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // 3. Persist SecurityContext to the HTTP session so the JSESSIONID cookie
        //    carries a valid authenticated session on all subsequent requests
        new HttpSessionSecurityContextRepository()
                .saveContext(context, httpRequest, httpResponse);

        return new LoginResponse("Login successful");
    }

    @Override
    public LogoutResponse logout() {
        try {
            httpSession.invalidate();
        } catch (IllegalStateException e) {
            // Already invalidated, ignore
        }
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        return new LogoutResponse("Logout successful");
    }
}