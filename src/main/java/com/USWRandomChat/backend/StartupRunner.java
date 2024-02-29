package com.USWRandomChat.backend;

import com.USWRandomChat.backend.emailAuth.service.EmailAuthSchedulerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//@Component
public class StartupRunner implements CommandLineRunner {

    private final EmailAuthSchedulerService schedulerService;

    public StartupRunner(EmailAuthSchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @Override
    public void run(String... args) throws Exception {
        schedulerService.removeMember();
    }
}

