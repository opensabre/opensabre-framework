package io.github.opensabre.boot.sensitive.rule;

import java.util.regex.Pattern;

public class CustomSensitiveRule implements SensitiveRule {

    private final Pattern pattern;

    private final int retainPrefixCount;

    private final int retainSuffixCount;

    private char replaceChar = '*';

    public CustomSensitiveRule(int retainPrefixCount, int retainSuffixCount) {
        this.pattern = Pattern.compile("\\*");
        this.retainPrefixCount = retainPrefixCount;
        this.retainSuffixCount = retainSuffixCount;
    }

    public CustomSensitiveRule(int retainPrefixCount, int retainSuffixCount, char replaceChar) {
        this.pattern = Pattern.compile("\\*");
        this.retainPrefixCount = retainPrefixCount;
        this.retainSuffixCount = retainSuffixCount;
        this.replaceChar = replaceChar;
    }

    public CustomSensitiveRule(String pattern, int retainPrefixCount, int retainSuffixCount) {
        this.pattern = Pattern.compile(pattern);
        this.retainPrefixCount = retainPrefixCount;
        this.retainSuffixCount = retainSuffixCount;
    }

    public CustomSensitiveRule(String pattern, int retainPrefixCount, int retainSuffixCount, char replaceChar) {
        this.pattern = Pattern.compile(pattern);
        this.retainPrefixCount = retainPrefixCount;
        this.retainSuffixCount = retainSuffixCount;
        this.replaceChar = replaceChar;
    }

    @Override
    public String category() {
        return "custom";
    }

    @Override
    public Pattern pattern() {
        return this.pattern;
    }

    @Override
    public int retainPrefixCount() {
        return this.retainPrefixCount;
    }

    @Override
    public int retainSuffixCount() {
        return this.retainSuffixCount;
    }

    @Override
    public char replaceChar() {
        return this.replaceChar;
    }
}