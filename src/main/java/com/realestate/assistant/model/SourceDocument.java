package com.realestate.assistant.model;
import java.time.LocalDate;
public record SourceDocument(String id, String publisher, String title, String url, LocalDate fetchedAt, String content) {}
