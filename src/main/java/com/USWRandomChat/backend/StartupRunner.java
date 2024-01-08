package com.USWRandomChat.backend;

import com.USWRandomChat.backend.service.SchedulerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    private final SchedulerService schedulerService;

    public StartupRunner(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @Override
    public void run(String... args) throws Exception {
        schedulerService.removeMember();
    }
}

