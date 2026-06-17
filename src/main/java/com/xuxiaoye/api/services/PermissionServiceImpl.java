package com.xuxiaoye.api.services;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

import com.xuxiaoye.api.interfaces.OwnerChecker;
import com.xuxiaoye.api.services.db.dto.entity.Role;
import com.xuxiaoye.api.services.db.RoleDBService;
import com.xuxiaoye.api.services.db.StudentDBService;
import com.xuxiaoye.api.services.db.UserDBService;

@Log4j2
public class PermissionServiceImpl implements PermissionEvaluator {

    private final Cache<String, Boolean> cache;
    private final StudentDBService studentDBService;
    private final RoleDBService roleDBService;
    private final UserDBService userDBService;

    private final Map<String, OwnerChecker> OWNER_CHECKERS;

    public PermissionServiceImpl(
            Cache<String, Boolean> cache,
            StudentDBService studentDBService,
            RoleDBService roleDBService,
            UserDBService userDBService
    ) {
        this.cache = cache;
        this.studentDBService = studentDBService;
        this.roleDBService = roleDBService;
        this.userDBService = userDBService;

        this.OWNER_CHECKERS = Map.of(
                "student", studentDBService,
                "role", roleDBService,
                "user", userDBService
        );
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        String owner = ((User) authentication.getPrincipal()).getUsername();
        if (permission instanceof String && ((String) permission).endsWith("_own")) {
            OwnerChecker checker = OWNER_CHECKERS.get(targetType.toLowerCase());
            if (checker != null) {
                return checker.isOwner((String) targetId, owner);
            }
        }
        return hasPermission(authentication, targetType, permission);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        String authority = (targetDomainObject + ":" + permission);

        boolean result = authentication.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals(authority) || hasAuthorization(a.getAuthority(), authority)
                );
        log.info("Permission {}:{} result {}", targetDomainObject, permission, result);
        return result;
    }

    protected boolean hasAuthorization(String authorityName, String authority) {
        String key = authorityName + "_" + authority;
        Boolean result = cache.getIfPresent(key);
        if (result != null) {
            return result;
        }
        if (authorityName.startsWith("ROLE_")) {
            String roleKey = authorityName.replace("ROLE_", "");
            Role role = this.roleDBService.getById(roleKey);
            if (role == null) {
                return false;
            }
            List<String> authorities = Arrays.stream(role.getAuthority().split(","))
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .toList();
            result = authorities.contains(authority.toUpperCase()) ||
                    authorities.contains(authority.toUpperCase().replaceAll(":.*", ":*")) ||
                    authorities.contains("*:*");
        } else {
            result = authorityName.equalsIgnoreCase(authority);
        }

        log.info("User Authority: {} Target Authority: {} Result: {}", authorityName, authority, result);
        cache.put(authorityName + "_" + authority, result);
        return result;
    }
}
