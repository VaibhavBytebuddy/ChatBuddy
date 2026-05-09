package com.om.ChatBuddy.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.om.ChatBuddy.common.constant.ApiEndPointConstants.Public.HEALTH;

@RestController
@RequestMapping(HEALTH)
public class HealthController {

    @GetMapping
    public String checkHealth() {
        return "ChatBuddy is healthy!";
    }
}
