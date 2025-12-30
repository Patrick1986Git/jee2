package com.company.shop.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.company.shop.module.user.dto.AuthResponseDTO;
import com.company.shop.module.user.dto.LoginRequestDTO;
import com.company.shop.module.user.dto.RegisterRequestDTO;
import com.company.shop.module.user.entity.Role;
import com.company.shop.module.user.entity.User;
import com.company.shop.module.user.repository.RoleRepository;
import com.company.shop.module.user.repository.UserRepository;
import com.company.shop.security.jwt.JwtTokenProvider;

@Service
public class AuthServiceImpl implements AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider tokenProvider;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider,
			UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.authenticationManager = authenticationManager;
		this.tokenProvider = tokenProvider;
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public AuthResponseDTO login(LoginRequestDTO request) {

		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

		String token = tokenProvider.generateToken(authentication);
		return new AuthResponseDTO(token);
	}

	@Override
	public void register(RegisterRequestDTO request) {

	    if (userRepository.existsByEmail(request.getEmail())) {
	        throw new IllegalArgumentException("Email already exists");
	    }

	    User user = new User(
	        request.getEmail(),
	        passwordEncoder.encode(request.getPassword())
	    );

	    Role userRole = roleRepository.findByName("ROLE_USER")
	        .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));

	    user.addRole(userRole);

	    userRepository.save(user);
	}

}
