package com.jeka8833.tntclientendpoints.services.restapi.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
@Table(name = "TCA_UserPrivileges")
public class TNTClientUser {
    @Id
    @Column("user")
    private final UUID id;

    @Column("roles")
    private final String roles;

    @Column("staticKey")
    private final UUID staticKey;
}
