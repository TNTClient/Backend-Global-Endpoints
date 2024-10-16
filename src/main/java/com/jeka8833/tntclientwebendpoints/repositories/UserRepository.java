package com.jeka8833.tntclientwebendpoints.repositories;

import com.jeka8833.tntclientwebendpoints.models.TNTClientUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<TNTClientUser, UUID> {
}
