package com.realestate.assistant.ingest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.realestate.assistant.model.SourceDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/**
 * Optional refresh:
 * INGEST_URLS="https://darglobal.co.uk/projects/villas,https://wasalt.sa/en"
 * INGEST_OUTPUT="src/main/resources/data/catalog.json" mvn spring-boot:run
 * Only explicitly permitted hosts are accepted; no login or anti-bot bypass is attempted.
 */
@Component
public class SiteIngestor implements CommandLineRunner {
    private static final Set<String> ALLOWED_HOSTS = Set.of("darglobal.co.uk", "www.darglobal.co.uk", "wasalt.sa", "www.wasalt.sa");
    private final Environment environment;
    private final ObjectMapper mapper;
    public SiteIngestor(Environment environment, ObjectMapper mapper) {
        this.environment = environment;
        this.mapper = mapper;
    }
    @Override
    public void run(String... args) throws Exception {
        String configuredUrls = environment.getProperty("INGEST_URLS", "");
        if (configuredUrls.isBlank()) return;
        String output = environment.getProperty("INGEST_OUTPUT", "src/main/resources/data/catalog.json");
        List<SourceDocument> documents = new ArrayList<>();
        for (String raw : configuredUrls.split(",")) {
            String url = raw.trim();
            if (url.isEmpty()) continue;
            URI uri = URI.create(url);
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null || !ALLOWED_HOSTS.contains(host.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Refusing non-allowlisted ingestion URL: " + url);
            }
            Document page = Jsoup.connect(url)
                    .userAgent("PropertyAssistantResearchBot/1.0")
                    .timeout(15000).maxBodySize(5_000_000).get();
            page.select("script,style,nav,footer,header,form,noscript,svg").remove();
            String text = page.body() == null ? "" : page.body().text().replaceAll("\\s+", " ").trim();
            if (text.length() < 80) {
                throw new IllegalStateException("Refusing to save an empty or challenge response from " + url);
            }
            if (text.length() > 12000) text = text.substring(0, 12000);
            String publisher = host.contains("darglobal") ? "DarGlobal" : "Wasalt";
            String title = page.title().isBlank() ? url : page.title().trim();
            String id = publisher.toLowerCase(Locale.ROOT) + "-" + Integer.toUnsignedString(url.hashCode(), 36);
            documents.add(new SourceDocument(id, publisher, title, url, LocalDate.now(), "jsoup", text));
            Thread.sleep(1200);
        }
        if (documents.isEmpty()) throw new IllegalStateException("No source pages were ingested");
        Path target = Path.of(output).toAbsolutePath();
        Files.createDirectories(target.getParent());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(target.toFile(), documents);
        System.out.println("Ingested " + documents.size() + " pages to " + target);
    }
}
