package com.example.bankcards.service;

import com.example.bankcards.dto.requests.AuthRequest;
import com.example.bankcards.dto.response.AuthResponse;

public interface AuthService {
     void register(AuthRequest request) ;
     AuthResponse login(AuthRequest request);
}
