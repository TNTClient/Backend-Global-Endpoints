package com.jeka8833.tntclientendpoints.services.discordbot.configs;

import discord4j.common.JacksonResources;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalCommandRegistrar implements ApplicationRunner {
    private final RestClient client;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        JacksonResources d4jMapper = JacksonResources.create();

        PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();

        List<ApplicationCommandRequest> commands = new ArrayList<>();
        for (Resource resource : matcher.getResources("commands/*.json")) {
            ApplicationCommandRequest request = d4jMapper.getObjectMapper()
                    .readValue(resource.getInputStream(), ApplicationCommandRequest.class);

            commands.add(request);
        }

        //noinspection DataFlowIssue
        long applicationId = client.getApplicationId().block();

        ApplicationService applicationService = client.getApplicationService();
        applicationService.bulkOverwriteGlobalApplicationCommand(applicationId, commands)
                .doOnNext(ignore -> log.debug("Successfully registered Global Commands"))
                .doOnError(e -> log.error("Failed to register global commands", e))
                .subscribe();
    }
}
