package com.jeka8833.tntclientendpoints.services.discordbot.repositories;

import com.jeka8833.tntclientendpoints.services.discordbot.models.ConnectedPlayerModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnectedPlayerRepository extends CrudRepository<ConnectedPlayerModel, Long> {
}
