package com.jeka8833.tntclientendpoints.services.restapi.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.UUID;

@Getter
@Entity
@Table(name = "TCA_UserPrivileges")
public class TNTClientUser {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "roles")
    private String roles;

    @Column(name = "staticKey")
    private UUID staticKey;
}
