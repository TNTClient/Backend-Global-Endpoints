package com.jeka8833.tntclientendpoints.services.restapi.repositories;

import com.jeka8833.tntclientendpoints.services.restapi.models.TNTClientUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<TNTClientUser, UUID> {
}
