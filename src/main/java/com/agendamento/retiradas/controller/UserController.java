package com.agendamento.retiradas.controller;

import com.agendamento.retiradas.dto.UserDto;
import com.agendamento.retiradas.model.Role;
import com.agendamento.retiradas.model.User;
import com.agendamento.retiradas.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('EXPEDICAO')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EXPEDICAO') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('EXPEDICAO')")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto) {
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
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EXPEDICAO') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        try {
            User user = userService.update(id, userDto);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar usuário: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EXPEDICAO')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao excluir usuário: " + e.getMessage());
        }
    }

    @GetMapping("/vendedores")
    @PreAuthorize("hasRole('EXPEDICAO')")
    public ResponseEntity<List<User>> getAllVendedores() {
        List<User> vendedores = userService.findAll().stream()
                .filter(user -> user.getRole() == Role.VENDEDOR)
                .toList();
        return ResponseEntity.ok(vendedores);
    }
}
