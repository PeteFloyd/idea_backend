package com.learn.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.learn.demo.entity.User;
import com.learn.demo.enums.UserRole;
import com.learn.demo.enums.UserStatus;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class UserPrincipalTest {

    @Test
    void isEnabledReturnsTrueWhenActive() {
        User user = buildUser(UserStatus.ACTIVE);

        UserPrincipal principal = new UserPrincipal(user);

        assertTrue(principal.isEnabled());
    }

    @Test
    void isEnabledReturnsFalseWhenDisabled() {
        User user = buildUser(UserStatus.DISABLED);

        UserPrincipal principal = new UserPrincipal(user);

        assertFalse(principal.isEnabled());
    }

    @Test
    void authoritiesContainRolePrefix() {
        User user = new User();
        user.setId(10L);
        user.setUsername("admin");
        user.setPassword("secret");
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);

        UserPrincipal principal = new UserPrincipal(user);
        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.iterator().next().getAuthority());
    }

    @Test
    void exposesUserBasicsAndFlags() {
        User user = new User();
        user.setId(5L);
        user.setUsername("sam");
        user.setPassword("secret");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        UserPrincipal principal = new UserPrincipal(user);

        assertEquals(5L, principal.getId());
        assertEquals(UserRole.USER, principal.getRole());
        assertEquals("sam", principal.getUsername());
        assertEquals("secret", principal.getPassword());
        assertTrue(principal.isAccountNonExpired());
        assertTrue(principal.isAccountNonLocked());
        assertTrue(principal.isCredentialsNonExpired());
    }

    private User buildUser(UserStatus status) {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("secret");
        user.setRole(UserRole.USER);
        user.setStatus(status);
        return user;
    }
}
