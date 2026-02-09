package com.aviation.service;

import com.aviation.dto.AuthRequest;
import com.aviation.dto.AuthResponse;
import com.aviation.entity.User;
import com.aviation.repository.UserRepository;
import com.aviation.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        
        // Reuse the already-loaded UserDetails from the authentication result
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // Single DB lookup to get the role (UserDetails only has authorities, not the raw role)
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String token = jwtUtil.generateToken(userDetails, user.getRole().name());
        
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}
