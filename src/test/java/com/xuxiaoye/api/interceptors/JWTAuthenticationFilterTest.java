package com.xuxiaoye.api.interceptors;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.github.benmanes.caffeine.cache.Cache;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.conf.ResourceConfig;
import com.xuxiaoye.api.constant.HeaderConstant;
import com.xuxiaoye.api.utils.FileUtils;
import com.xuxiaoye.api.utils.JwtUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Log4j2
class JWTAuthenticationFilterTest {

    @Mock
    private RequestContext requestContext;

    @Mock
    private ResourceConfig resourceConfig;

    @Mock
    private Cache<String, Boolean> cache;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    private JWTAuthenticationFilter filter;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
        filter = new JWTAuthenticationFilter(requestContext, resourceConfig, cache);
    }

    @Nested
    class AuthenticateRequestTest {

        @Test
        void testDuplicatedRequestThrowsForbiddenException() {
            String traceId = UUID.randomUUID().toString();
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(HeaderConstant.X_TRACE_ID, traceId);

            when(cache.getIfPresent(traceId)).thenReturn(Boolean.TRUE);

            assertThrows(RuntimeException.class, () -> filter.authenticateRequest(request));
            verify(cache, never()).put(anyString(), any());
        }

        @Test
        void testBlankAuthorizationSetsGuestAuthentication() {
            String traceId = UUID.randomUUID().toString();
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(HeaderConstant.X_TRACE_ID, traceId);
            // No Authorization header → guest auth expected

            when(cache.getIfPresent(traceId)).thenReturn(null);

            filter.authenticateRequest(request);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getName()).isEqualTo("Guest");
            assertThat(auth.getAuthorities())
                    .anyMatch(a -> a.getAuthority().equals("ROLE_GUEST"));
        }

        @Test
        void testInvalidJwtTokenThrowsBadCredentialsException() {
            String traceId = UUID.randomUUID().toString();
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(HeaderConstant.X_TRACE_ID, traceId);
            request.addHeader("Authorization", "Bearer this.is.not.a.valid.jwt");

            when(cache.getIfPresent(traceId)).thenReturn(null);
            when(resourceConfig.getPublicKey()).thenReturn(new byte[0]);

            assertThrows(BadCredentialsException.class, () -> filter.authenticateRequest(request));
        }

        @Test
        void testExpiredJwtTokenThrowsBadCredentialsException() {
            String traceId = UUID.randomUUID().toString();
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(HeaderConstant.X_TRACE_ID, traceId);
            request.addHeader("Authorization", "Bearer some.jwt.token");

            when(cache.getIfPresent(traceId)).thenReturn(null);
            when(resourceConfig.getPublicKey()).thenReturn(new byte[0]);

            try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
                // Token "validates" structurally but its expiry is in the past
                when(claims.getExpiration())
                        .thenReturn(new Date(System.currentTimeMillis() - 60_000));
                mockedJwtUtils
                        .when(() -> JwtUtils.validateJWTToken(anyString(), any()))
                        .thenReturn(claims);

                assertThrows(BadCredentialsException.class, () -> filter.authenticateRequest(request));
            }
        }

        @Test
        void testValidJwtTokenSetsAuthentication() throws Exception {
            String traceId = UUID.randomUUID().toString();
            String userId = UUID.randomUUID().toString();

            byte[] privateKeyBytes = FileUtils.readFileToBytes("test_certs/test_pri_key.der");
            byte[] publicKeyBytes = FileUtils.readFileToBytes("test_certs/test_pub_key.der");

            Map<String, Object> jwtClaims = Map.of(
                    "id", userId,
                    "roles", "ADMIN,USER",
                    "authorities", "READ,WRITE"
            );
            String token = JwtUtils.generateJWTToken(privateKeyBytes, userId, jwtClaims);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(HeaderConstant.X_TRACE_ID, traceId);
            request.addHeader("Authorization", "Bearer " + token);

            when(cache.getIfPresent(traceId)).thenReturn(null);
            when(resourceConfig.getPublicKey()).thenReturn(publicKeyBytes);

            filter.authenticateRequest(request);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getName()).isEqualTo(userId);
            assertThat(auth.getAuthorities())
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))
                    .anyMatch(a -> a.getAuthority().equals("READ"))
                    .anyMatch(a -> a.getAuthority().equals("WRITE"));

            verify(requestContext).setXUserId(userId);
        }

        @Test
        void testAuthorizationTokenWithoutBearerPrefixThrowsBadCredentialsException() {
            String traceId = UUID.randomUUID().toString();
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(HeaderConstant.X_TRACE_ID, traceId);
            request.addHeader("Authorization", "invalid-non-bearer-token");

            when(cache.getIfPresent(traceId)).thenReturn(null);
            when(resourceConfig.getPublicKey()).thenReturn(new byte[0]);

            assertThrows(BadCredentialsException.class, () -> filter.authenticateRequest(request));
        }

        @Test
        void testJwtUtilsRuntimeExceptionWrappedAsBadCredentialsException() {
            String traceId = UUID.randomUUID().toString();
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(HeaderConstant.X_TRACE_ID, traceId);
            request.addHeader("Authorization", "Bearer some.jwt.token");

            when(cache.getIfPresent(traceId)).thenReturn(null);
            when(resourceConfig.getPublicKey()).thenReturn(new byte[0]);

            try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
                mockedJwtUtils
                        .when(() -> JwtUtils.validateJWTToken(anyString(), any()))
                        .thenThrow(new RuntimeException("Unexpected JWT error"));

                BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                        () -> filter.authenticateRequest(request));
                assertThat(ex.getMessage()).contains("Invalid Authorization token");
            }
        }
    }

    @Nested
    class DoFilterInternalTest {

        @Test
        void testDoFilterInternalAuthenticatesAndContinuesChain() throws Exception {
            String traceId = UUID.randomUUID().toString();
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.addHeader(HeaderConstant.X_TRACE_ID, traceId);
            // No Authorization header → guest auth

            when(cache.getIfPresent(traceId)).thenReturn(null);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("Guest");
        }

        @Test
        void testDoFilterInternalSkipsAuthWhenAlreadyAuthenticated() throws Exception {
            // Pre-populate SecurityContext so the filter should skip authenticateRequest
            Authentication existingAuth = mock(Authentication.class);
            SecurityContextHolder.getContext().setAuthentication(existingAuth);

            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            // cache must never be consulted because auth was already present
            verify(cache, never()).getIfPresent(anyString());
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existingAuth);
        }

        @Test
        void testDoFilterInternalDuplicatedRequestPropagatesForbiddenException() throws Exception {
            String traceId = UUID.randomUUID().toString();
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.addHeader(HeaderConstant.X_TRACE_ID, traceId);

            when(cache.getIfPresent(traceId)).thenReturn(Boolean.TRUE);

            assertThrows(RuntimeException.class,
                    () -> filter.doFilterInternal(request, response, filterChain));
            verify(filterChain, never()).doFilter(any(), any());
        }

        @Test
        void testDoFilterInternalInvalidTokenPropagatesBadCredentialsException() throws Exception {
            String traceId = UUID.randomUUID().toString();
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.addHeader(HeaderConstant.X_TRACE_ID, traceId);
            request.addHeader("Authorization", "Bearer bad.token.value");

            when(cache.getIfPresent(traceId)).thenReturn(null);
            when(resourceConfig.getPublicKey()).thenReturn(new byte[0]);

            assertThrows(BadCredentialsException.class,
                    () -> filter.doFilterInternal(request, response, filterChain));
            verify(filterChain, never()).doFilter(any(), any());
        }
    }
}