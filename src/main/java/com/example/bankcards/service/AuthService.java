package com.example.bankcards.service;

import com.example.bankcards.dto.requests.AuthRequest;
import com.example.bankcards.dto.response.AuthResponse;

public interface AuthService {
    public void register(AuthRequest request) ;
    public AuthResponse login(AuthRequest request);
}
