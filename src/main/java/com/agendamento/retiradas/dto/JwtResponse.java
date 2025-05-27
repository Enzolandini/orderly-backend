package com.agendamento.retiradas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String name;
    private String email;
    private String role;

    public JwtResponse(String token, Long id, String username, String name, String email, String role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
