package com.xuxiaoye.api.services;

import java.io.Serializable;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

import com.xuxiaoye.api.services.db.RoleDBService;
import com.xuxiaoye.api.services.db.StudentDBService;
import com.xuxiaoye.api.services.db.UserDBService;

@Log4j2
public class PermissionServiceImpl implements PermissionEvaluator {

    private final StudentDBService studentDBService;
    private final RoleDBService roleDBService;
    private final UserDBService userDBService;

    public PermissionServiceImpl(
            StudentDBService studentDBService,
            RoleDBService roleDBService,
            UserDBService userDBService
    ) {
        this.studentDBService = studentDBService;
        this.roleDBService = roleDBService;
        this.userDBService = userDBService;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        String owner = ((User) authentication.getPrincipal()).getUsername();
        if (permission instanceof String && ((String) permission).endsWith("_own")) {
            if (targetType.equalsIgnoreCase("student")) {
                return studentDBService.isOwner((String) targetId, owner);
            }
            if (targetType.equalsIgnoreCase("role")) {
                return roleDBService.isOwner((String) targetId, owner);
            }
            if (targetType.equalsIgnoreCase("user")) {
                return userDBService.isOwner((String) targetId, owner);
            }
        }
        return hasPermission(authentication, targetType, permission);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        String authority = (targetDomainObject + ":" + permission);

        boolean result = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equalsIgnoreCase(authority)
                );
        log.info("Permission {}:{} result {}", targetDomainObject, permission, result);
        return result;
    }

    protected boolean isAuth(String authorityName, String authority) {
//        String key = authorityName + "_" + authority;
//        Boolean result = cache.getIfPresent(key);
//        if (result != null) {
//            return result;
//        }
//        if (authorityName.startsWith("ROLE_")) {
//            String roleKey = authorityName.replace("ROLE_", "");
//            Role role = this.roleDBService.getById(roleKey);
//            if (role == null) {
//                return false;
//            }
//            List<String> authorities = Arrays.stream(role.getAuthority().split(","))
//                    .map(String::trim)
//                    .map(String::toUpperCase)
//                    .toList();
//            result = authorities.contains(authority.toUpperCase()) ||
//                    authorities.contains(authority.replaceAll(":.*", ":*")) ||
//                    authorities.contains("*:*");
//        } else {
//            result = authorityName.equalsIgnoreCase(authority);
//        }
//
//        log.info("User Authority: {} Target Authority: {} Result: {}", authorityName, authority, result);
//        cache.put(authorityName + "_" + authority, result);
//        return result;
        return false;
    }
}
