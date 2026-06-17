package com.xuxiaoye.api.services;

import java.util.*;

import com.github.benmanes.caffeine.cache.Cache;
import com.xuxiaoye.api.services.db.StudentDBService;
import com.xuxiaoye.api.services.db.UserDBService;
import lombok.extern.log4j.Log4j2;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import com.xuxiaoye.api.Application;
import com.xuxiaoye.api.services.db.RoleDBService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureStubRunner
@ActiveProfiles("test")
@Log4j2
class PermissionServiceImplTest {
    EasyRandom easyRandom = new EasyRandom();

    @InjectMocks
    PermissionServiceImpl permissionService;

    @Mock
    Cache<String, Boolean> cache;

    @Mock
    StudentDBService studentDBService;

    @Mock
    UserDBService userDBService;

    @Mock
    RoleDBService roleDBService;

    @Mock
    Authentication authentication;

    @Nested
    class WithAuthorityObject {
        @Test
        void testAuthorityIsBlank() {
            String target = easyRandom.nextObject(String.class);
            String permission = easyRandom.nextObject(String.class);

            when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
            assertThat(permissionService.hasPermission(authentication, target, permission)).isFalse();
        }

        @Test
        void testAuthorityMatches() {
            String target = easyRandom.nextObject(String.class);
            String permission = easyRandom.nextObject(String.class);

            Collection<GrantedAuthority> list = new ArrayList<>(List.of(
                    new SimpleGrantedAuthority(target + ":" + permission)
            ));

            when(authentication.getAuthorities()).thenReturn((Collection) list);
            assertThat(permissionService.hasPermission(authentication, target, permission)).isTrue();
        }

        @Test
        void testAuthorityPermissionIsWildcard() {
            String target = easyRandom.nextObject(String.class);
            String permission = easyRandom.nextObject(String.class);

            Collection<GrantedAuthority> list = new ArrayList<>(List.of(
                    new SimpleGrantedAuthority(target + ":*")
            ));

            when(authentication.getAuthorities()).thenReturn((Collection) list);
            assertThat(permissionService.hasPermission(authentication, target, permission)).isFalse();
        }

        @Test
        void testAuthorityObjectAndPermissionIsWildcard() {
            String target = easyRandom.nextObject(String.class);
            String permission = easyRandom.nextObject(String.class);

            Collection<GrantedAuthority> list = new ArrayList<>(List.of(
                    new SimpleGrantedAuthority("*:*")
            ));

            when(authentication.getAuthorities()).thenReturn((Collection) list);
            assertThat(permissionService.hasPermission(authentication, target, permission)).isFalse();
        }
    }

    @Nested
    class WithRole {
        @Test
        void testAuthorityRolePermission() {
            String role = easyRandom.nextObject(String.class);
            String target = easyRandom.nextObject(String.class);
            String permission = easyRandom.nextObject(String.class);

            Collection<GrantedAuthority> list = new ArrayList<>(List.of(
                    new SimpleGrantedAuthority("ROLE_" + role)
            ));

            com.xuxiaoye.api.services.db.dto.entity.Role dbRole = new com.xuxiaoye.api.services.db.dto.entity.Role();
            dbRole.setAuthority(target + ":" + permission);

            when(roleDBService.getById(role)).thenReturn(dbRole);
            when(authentication.getAuthorities()).thenReturn((Collection) list);
            assertThat(permissionService.hasPermission(authentication, target, permission)).isTrue();
        }

        @Test
        void testAuthorityRolePermissionIsWildcard() {
            String role = easyRandom.nextObject(String.class);
            String target = easyRandom.nextObject(String.class);
            String permission = easyRandom.nextObject(String.class);

            Collection<GrantedAuthority> list = new ArrayList<>(List.of(
                    new SimpleGrantedAuthority("ROLE_" + role)
            ));

            com.xuxiaoye.api.services.db.dto.entity.Role dbRole = new com.xuxiaoye.api.services.db.dto.entity.Role();
            dbRole.setAuthority(target + ":*");

            when(roleDBService.getById(role)).thenReturn(dbRole);
            when(authentication.getAuthorities()).thenReturn((Collection) list);
            assertThat(permissionService.hasPermission(authentication, target, permission)).isTrue();
        }

        @Test
        void testAuthorityRoleObjectAndPermissionIsWildcard() {
            String role = easyRandom.nextObject(String.class);
            String target = easyRandom.nextObject(String.class);
            String permission = easyRandom.nextObject(String.class);

            Collection<GrantedAuthority> list = new ArrayList<>(List.of(
                    new SimpleGrantedAuthority("ROLE_" + role)
            ));

            com.xuxiaoye.api.services.db.dto.entity.Role dbRole = new com.xuxiaoye.api.services.db.dto.entity.Role();
            dbRole.setAuthority("*:*");

            when(roleDBService.getById(role)).thenReturn(dbRole);
            when(authentication.getAuthorities()).thenReturn((Collection) list);
            assertThat(permissionService.hasPermission(authentication, target, permission)).isTrue();
        }
    }

}