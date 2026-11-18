package com.realestate.assistant.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CorpusServiceTest {
    private final CorpusService corpus = new CorpusService(new ObjectMapper().findAndRegisterModules());
    @Test
    void loadsBothPublishersWithSourceUrls() {
        assertTrue(corpus.documents().stream().anyMatch(d -> d.publisher().equals("DarGlobal")));
        assertTrue(corpus.documents().stream().anyMatch(d -> d.publisher().equals("Wasalt")));
        assertTrue(corpus.documents().stream().allMatch(d -> d.url().startsWith("https://")));
    }
    @Test
    void retrievesMatchingProjectEvidence() {
        var results = corpus.retrieve("What projects does DarGlobal have in Oman?", 5);
        assertFalse(results.isEmpty());
        assertEquals("darglobal-projects-oman", results.getFirst().id());
    }
    @Test
    void retrievesWasaltProjectEvidence() {
        var results = corpus.retrieve("What is the Shams Al Riyadh project price range?", 5);
        assertFalse(results.isEmpty());
        assertEquals("wasalt-project-search", results.getFirst().id());
    }
    @Test
    void returnsNoEvidenceForUnmatchedQuery() {
        assertTrue(corpus.retrieve("moonbase unicorn Atlantis", 5).isEmpty());
    }
}
