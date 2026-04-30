package com.xuxiaoye.api.services;

import java.io.Serializable;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

import com.xuxiaoye.api.services.db.StudentDBService;

public class PermissionServiceImpl implements PermissionEvaluator {

    private final StudentDBService studentDBService;

    public PermissionServiceImpl(StudentDBService studentDBService) {
        this.studentDBService = studentDBService;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (permission instanceof String && ((String) permission).endsWith("_own")) {
            if (targetType.equalsIgnoreCase("student")) {
                String owner = ((User) authentication.getPrincipal()).getUsername();
                return studentDBService.isOwner((String) targetId, owner);
            }
        }
        return hasPermission(authentication, targetType, permission);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        String authority = (targetDomainObject + ":" + permission).toUpperCase();

        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals(authority));
    }
}
