package com.realestate.assistant.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.realestate.assistant.model.SourceDocument;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
@Service
public class CorpusService {
    private static final Pattern SPLIT = Pattern.compile("[^\\p{L}\\p{N}]+");
    private static final Set<String> STOP_WORDS = Set.of("the","and","for","from","with","what","which","where","when","does","are","is","about","tell","me","show","please","can","you","how","much","that","this","have","has","their","they","in","on","of","to","a","an","at","by");
    private final List<SourceDocument> documents;
    public CorpusService(ObjectMapper mapper) {
        try (InputStream input = new ClassPathResource("data/catalog.json").getInputStream()) {
            documents = List.copyOf(mapper.readValue(input, new TypeReference<List<SourceDocument>>() {}));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load bundled source corpus", ex);
        }
    }
    public List<SourceDocument> documents() { return documents; }
    public List<SourceDocument> retrieve(String question, int limit) {
        List<String> terms = tokenize(question);
        if (terms.isEmpty()) return List.of();
        int n = documents.size();
        List<List<String>> tokenized = documents.stream().map(d -> tokenize(d.title() + " " + d.content())).toList();
        double avgLength = tokenized.stream().mapToInt(List::size).average().orElse(1);
        List<Scored> scored = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            List<String> tokens = tokenized.get(i);
            Map<String, Integer> frequencies = new HashMap<>();
            tokens.forEach(t -> frequencies.merge(t, 1, Integer::sum));
            double score = 0;
            for (String term : terms) {
                int tf = frequencies.getOrDefault(term, 0);
                if (tf == 0) continue;
                long df = tokenized.stream().filter(doc -> doc.contains(term)).count();
                double idf = Math.log(1.0 + (n - df + 0.5) / (df + 0.5));
                double k1 = 1.5, b = 0.75;
                score += idf * tf * (k1 + 1) / (tf + k1 * (1 - b + b * tokens.size() / Math.max(1.0, avgLength)));
            }
            if (score > 0) scored.add(new Scored(documents.get(i), score));
        }
        return scored.stream().sorted(Comparator.comparingDouble(Scored::score).reversed()).limit(limit).map(Scored::document).toList();
    }
    private static List<String> tokenize(String text) {
        return Arrays.stream(SPLIT.split(text.toLowerCase(Locale.ROOT)))
                .filter(t -> t.length() > 1 && !STOP_WORDS.contains(t)).toList();
    }
    private record Scored(SourceDocument document, double score) {}
}
