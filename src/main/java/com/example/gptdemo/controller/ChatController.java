package com.example.gptdemo.controller;

import com.example.gptdemo.config.OpenAiProperties;
import com.example.gptdemo.model.ChatRequest;
import com.example.gptdemo.model.ChatResponse;
import com.example.gptdemo.service.OpenAiChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final OpenAiChatService chatService;
    private final OpenAiProperties properties;

    public ChatController(OpenAiChatService chatService, OpenAiProperties properties) {
        this.chatService = chatService;
        this.properties = properties;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        String reply = chatService.ask(request.prompt());
        return new ChatResponse(properties.model(), reply);
    }
}
