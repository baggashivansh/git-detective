package com.gitdetective.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LanguageDetectorTest {

    private final LanguageDetector languageDetector = new LanguageDetector();

    @Test
    @DisplayName("detects supported languages by extension and Dockerfile name")
    void detectsLanguages() {
        assertThat(languageDetector.detectLanguage("Main.java")).contains("Java");
        assertThat(languageDetector.detectLanguage("app.kt")).contains("Kotlin");
        assertThat(languageDetector.detectLanguage("index.ts")).contains("TypeScript");
        assertThat(languageDetector.detectLanguage("index.js")).contains("JavaScript");
        assertThat(languageDetector.detectLanguage("main.py")).contains("Python");
        assertThat(languageDetector.detectLanguage("main.go")).contains("Go");
        assertThat(languageDetector.detectLanguage("lib.rs")).contains("Rust");
        assertThat(languageDetector.detectLanguage("Program.cs")).contains("C#");
        assertThat(languageDetector.detectLanguage("main.cpp")).contains("C++");
        assertThat(languageDetector.detectLanguage("config.yml")).contains("YAML");
        assertThat(languageDetector.detectLanguage("data.json")).contains("JSON");
        assertThat(languageDetector.detectLanguage("README.md")).contains("Markdown");
        assertThat(languageDetector.detectLanguage("query.sql")).contains("SQL");
        assertThat(languageDetector.detectLanguage("pom.xml")).contains("XML");
        assertThat(languageDetector.detectLanguage("app.properties")).contains("Properties");
        assertThat(languageDetector.detectLanguage("Dockerfile")).contains("Dockerfile");
        assertThat(languageDetector.detectLanguage("notes.txt")).contains("Other");
    }
}
