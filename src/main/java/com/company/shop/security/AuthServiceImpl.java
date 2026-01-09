package com.company.shop.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.shop.common.exception.UserAlreadyExistsException;
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

    public AuthServiceImpl(AuthenticationManager authenticationManager, 
                           JwtTokenProvider tokenProvider,
                           UserRepository userRepository, 
                           RoleRepository roleRepository, 
                           PasswordEncoder passwordEncoder) {
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
	@Transactional // Zapewnia atomowość rejestracji i przypisania ról
	public void register(RegisterRequestDTO request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new UserAlreadyExistsException(
					"Użytkownik o podanym adresie email już istnieje: " + request.getEmail());
		}

		// Tworzenie użytkownika z wykorzystaniem danych z DTO
		User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()), request.getFirstName(),
				request.getLastName());

		// Pobranie domyślnej roli
		Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow(
				() -> new IllegalStateException("Błąd konfiguracji systemu: ROLE_USER nie istnieje w bazie"));

		user.addRole(userRole);

		userRepository.save(user);
	}
}