package com.company.shop.security;

import com.company.shop.module.user.dto.AuthResponseDTO;
import com.company.shop.module.user.dto.LoginRequestDTO;
import com.company.shop.module.user.dto.RegisterRequestDTO;

public interface AuthService {

	AuthResponseDTO login(LoginRequestDTO request);

	void register(RegisterRequestDTO request);
}
