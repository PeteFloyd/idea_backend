package com.learn.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.learn.demo.entity.User;
import com.learn.demo.enums.UserRole;
import com.learn.demo.enums.UserStatus;
import com.learn.demo.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternalSetsAuthenticationForValidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        UserDetails userDetails = new UserPrincipal(buildUser("alice", null));

        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid-token")).thenReturn("alice");
        when(customUserDetailsService.loadUserByUsername("alice")).thenReturn(userDetails);

        filter.doFilterInternal(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("alice", authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternalContinuesWhenMissingAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtTokenProvider);
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternalContinuesWhenTokenInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
        verifyNoInteractions(customUserDetailsService);
    }

    @Test
    void doFilterInternalContinuesWhenTokenBlacklisted() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer blacklisted-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtTokenProvider.validateToken("blacklisted-token")).thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
        verifyNoInteractions(customUserDetailsService);
    }

    @Test
    void doFilterInternalSkipsAuthenticationWhenPasswordChangedAfterTokenIssued() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer stale-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        Instant issuedAt = Instant.parse("2024-01-01T00:00:00Z");
        LocalDateTime passwordChangedAt = LocalDateTime.ofInstant(issuedAt.plusSeconds(60), ZoneId.systemDefault());
        UserDetails userDetails = new UserPrincipal(buildUser("alice", passwordChangedAt));

        when(jwtTokenProvider.validateToken("stale-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("stale-token")).thenReturn("alice");
        when(customUserDetailsService.loadUserByUsername("alice")).thenReturn(userDetails);
        when(jwtTokenProvider.getIssuedAtFromToken("stale-token")).thenReturn(Date.from(issuedAt));

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    private User buildUser(String username, LocalDateTime passwordChangedAt) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword("secret");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordChangedAt(passwordChangedAt);
        return user;
    }
}
