package com.jeka8833.tntclientendpoints.services.discordbot.repositories;

import com.jeka8833.tntclientendpoints.services.discordbot.models.ConnectedPlayerModel;
import org.springframework.data.domain.Limit;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ConnectedPlayerRepository extends CrudRepository<ConnectedPlayerModel, Long> {
    Collection<ConnectedPlayerModel> findAllByDiscord(long discordID, Limit limit);
}
