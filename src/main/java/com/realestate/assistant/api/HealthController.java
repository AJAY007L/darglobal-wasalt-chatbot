package com.realestate.assistant.api;
import com.realestate.assistant.service.CorpusService;
import com.realestate.assistant.service.OpenRouterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
@RestController
public class HealthController {
    private final CorpusService corpus;
    private final OpenRouterService openRouter;
    public HealthController(CorpusService corpus, OpenRouterService openRouter) { this.corpus = corpus; this.openRouter = openRouter; }
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "documents", corpus.documents().size(), "openRouterConfigured", openRouter.configured());
    }
}
