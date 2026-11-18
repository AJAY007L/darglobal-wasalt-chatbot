# Property Assistant

Java 21 / Spring Boot chatbot using public project and platform information from DarGlobal and Wasalt. It ranks relevant source notes locally, sends those excerpts to OpenRouter, and returns source links with the answer.

## Run locally

Set OPENROUTER_API_KEY in the environment, then run:

    mvn spring-boot:run

Open http://localhost:8080. GET /api/health reports corpus and AI-key status. POST /api/chat accepts JSON such as {"question":"What projects does DarGlobal have in Oman?"}.

## Refresh the public-page snapshot

The optional Jsoup ingestion accepts HTTPS URLs on darglobal.co.uk or wasalt.sa. Use only URLs covered by applicable authorization, and run it where network egress is available. It rejects empty responses so anti-bot pages cannot be saved as successful records:

    INGEST_URLS="https://darglobal.co.uk/projects/villas,https://wasalt.sa/en" \
    INGEST_OUTPUT="src/main/resources/data/catalog.json" \
    mvn spring-boot:run

Stop the process after the ingestion message, then rebuild/restart the application. The bundled corpus records each source URL, capture date, and retrieval method. The Wasalt homepage and Riyadh page were fetched directly with Jsoup. DarGlobal blocked direct requests in this environment, so the two DarGlobal records contain text extracted from publicly indexed official pages; the index crawl can be older than the capture date. Dynamic property details should be checked on the linked source.

## Docker and Render

Build and run with:

    docker build -t property-assistant .
    docker run --rm -p 8080:8080 -e OPENROUTER_API_KEY="$OPENROUTER_API_KEY" property-assistant

Render can deploy this repository using render.yaml. Set OPENROUTER_API_KEY in the service's secret environment settings. Free instances may sleep after inactivity and take about a minute to wake.
