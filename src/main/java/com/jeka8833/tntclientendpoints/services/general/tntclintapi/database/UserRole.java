package com.jeka8833.tntclientendpoints.services.general.tntclintapi.database;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@UtilityClass
public class UserRole {
    public static final SimpleGrantedAuthority HAS_ACCESSORIES = new SimpleGrantedAuthority("ACCESSORIES");
    public static final SimpleGrantedAuthority HAS_CAPE = new SimpleGrantedAuthority("CAPE");
    public static final SimpleGrantedAuthority HAS_TAB = new SimpleGrantedAuthority("HEART");

    public static final SimpleGrantedAuthority AUTH_BLOCKED = new SimpleGrantedAuthority("AUTH_BLOCKED");
    public static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ADMIN");
}
