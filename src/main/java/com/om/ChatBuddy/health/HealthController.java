package com.om.ChatBuddy.health;

import com.om.ChatBuddy.constants.ApiEndPointConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.om.ChatBuddy.constants.ApiEndPointConstants.*;

@RestController
@RequestMapping(HEALTH)
public class HealthController {

    @GetMapping
    public String checkHealth() {
        return "ChatBuddy is healthy!";
    }
}
