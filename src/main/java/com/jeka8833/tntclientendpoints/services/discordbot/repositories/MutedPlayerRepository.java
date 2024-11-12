package com.jeka8833.tntclientendpoints.services.discordbot.repositories;

import com.jeka8833.tntclientendpoints.services.discordbot.models.MutedPlayerModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MutedPlayerRepository extends CrudRepository<MutedPlayerModel, UUID> {

    @Query("SELECT m FROM MutedPlayerModel m WHERE m.unmuteTime > CURRENT_TIMESTAMP")
    Page<MutedPlayerModel> findMuted(Pageable pageable);
}
