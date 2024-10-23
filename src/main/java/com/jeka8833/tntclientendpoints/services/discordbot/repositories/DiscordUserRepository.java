package com.jeka8833.tntclientendpoints.services.discordbot.repositories;

import com.jeka8833.tntclientendpoints.services.discordbot.models.DiscordUserModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordUserRepository extends CrudRepository<DiscordUserModel, Long> {
}
