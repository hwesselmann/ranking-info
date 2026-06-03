package de.hdawg.rankinginfo.service;

/**
 * A display label / ISO-date pair representing one selectable quarter in the UI.
 *
 * <p>Field names {@code first} and {@code second} intentionally mirror Kotlin's {@code Pair} so
 * that Thymeleaf expressions like {@code ${opt.first}} and Kotlin callers using {@code it.second}
 * continue to work without changes.
 */
public record QuarterEntry(String first, String second) {}
