package com.agendamento.retiradas.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.agendamento.retiradas.model.User;

@Component("userSecurity")
public class UserSecurity {
    
    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return false;
        }
        
        User user = (User) principal;
        return user.getId().equals(userId);
    }
}
