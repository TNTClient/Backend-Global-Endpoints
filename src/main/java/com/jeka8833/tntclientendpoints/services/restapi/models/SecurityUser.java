package com.jeka8833.tntclientendpoints.services.restapi.models;

import com.jeka8833.tntclientendpoints.services.general.tntclintapi.database.UserRole;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class SecurityUser implements UserDetails {
    private final UUID username;

    private Collection<GrantedAuthority> authorities = new ArrayList<>();

    @Setter
    private @Nullable UUID password;

    @Getter
    @Setter
    private @Nullable Long expireAtNanos = null;

    @Getter
    @Setter
    private boolean loginSuccess = false;

    public SecurityUser(UUID username) {
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        if (password == null) return null;

        return "{noop}" + password;
    }

    @Override
    public String getUsername() {
        return username.toString();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !authorities.contains(UserRole.AUTH_BLOCKED);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return expireAtNanos == null || expireAtNanos - System.nanoTime() > 0;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SecurityUser that)) return false;

        return username.equals(that.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    public UUID getPasswordUUID() {
        return password;
    }

    public UUID getUsernameUUID() {
        return username;
    }

    public void setAuthorities(String authoritiesComaSeparated) {
        this.authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(authoritiesComaSeparated);
    }

    public void expireNow() {
        setExpireAtNanos(System.nanoTime());
    }
}
