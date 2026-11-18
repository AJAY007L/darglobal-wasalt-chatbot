package com.realestate.assistant.api;
import com.realestate.assistant.model.SourceDocument;
import java.util.List;
public record ChatResponse(String answer, List<SourceReference> sources, boolean grounded) {
    public record SourceReference(String publisher, String title, String url, String fetchedAt) {
        public static SourceReference from(SourceDocument source) {
            return new SourceReference(source.publisher(), source.title(), source.url(),
                    source.fetchedAt() == null ? "unknown" : source.fetchedAt().toString());
        }
    }
}
