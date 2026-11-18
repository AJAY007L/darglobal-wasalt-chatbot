package com.realestate.assistant.api;
import com.realestate.assistant.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api")
public class ChatController {
    private final ChatService chatService;
    public ChatController(ChatService chatService) { this.chatService = chatService; }
    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) { return chatService.answer(request.question()); }
}
