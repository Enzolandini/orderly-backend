package com.agendamento.retiradas.controller;

import com.agendamento.retiradas.dto.JwtResponse;
import com.agendamento.retiradas.dto.LoginRequest;
import com.agendamento.retiradas.dto.UserDto;
import com.agendamento.retiradas.model.User;
import com.agendamento.retiradas.security.JwtUtils;
import com.agendamento.retiradas.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        User userDetails = (User) authentication.getPrincipal();
        
        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getName(),
                userDetails.getEmail(),
                userDetails.getRole().name()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto userDto) {
        if (userService.existsByUsername(userDto.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Erro: Nome de usuário já está em uso!");
        }

        if (userService.existsByEmail(userDto.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Erro: Email já está em uso!");
        }

        User user = userService.save(userDto);
        
        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }
}
