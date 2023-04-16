package com.sebuilder.interpreter;

import org.apache.commons.jexl3.*;
import org.openqa.selenium.Keys;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public record InputData(LinkedHashMap<String, String> row, boolean lastRow) {

    public static final String ROW_NUMBER = "_rowNumber";
    private static final LinkedHashMap<String, String> EMPTY = new LinkedHashMap<>();
    private static final String REGEX_EXPRESSION = ".*\\$\\{(.+)}.*";
    private static final String STEP_INDEX = "_stepIndex";

    private static String extractExpression(final String result) {
        return result.replaceAll(REGEX_EXPRESSION, "$1");
    }

    public InputData() {
        this(EMPTY, true);
    }

    public InputData(final LinkedHashMap<String, String> row) {
        this(row, false);
    }

    public Map<String, String> input() {
        return this.entrySet()
                .stream()
                .filter(it -> !it.getKey().startsWith("_"))
                .collect(Collectors.toMap(Map.Entry::getKey
                        , Map.Entry::getValue
                        , (e1, e2) -> e1
                        , LinkedHashMap::new));
    }

    public String rowNumber() {
        return this.get(ROW_NUMBER);
    }

    public int stepIndex() {
        return Integer.parseInt(this.get(STEP_INDEX));
    }

    public String get(final String key) {
        return this.row.get(key);
    }

    public InputData stepIndex(final int index) {
        return this.add(STEP_INDEX, String.valueOf(index));
    }

    public InputData clearRowNumber() {
        final InputData result = this.copy();
        result.row.remove(ROW_NUMBER);
        return result;
    }

    public InputData add(final InputData shareInput) {
        final InputData result = this.copy();
        result.row.putAll(shareInput.row);
        return result;
    }

    public InputData add(final Map<String, String> initialVars) {
        final InputData result = this.copy();
        result.row.putAll(initialVars);
        return result;
    }

    public InputData add(final String key, final String value) {
        final InputData result = this.copy();
        result.row.put(key, value);
        return result;
    }

    public InputData remove(final String key) {
        final InputData result = this.copy();
        result.row.remove(key);
        return result;
    }

    public InputData filter(final Predicate<Map.Entry<String, String>> predicate) {
        return new InputData(new LinkedHashMap<>(this.row.entrySet()
                .stream()
                .filter(predicate)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
    }

    public InputData replaceKey(final Function<Map.Entry<String, String>, String> function) {
        return new InputData(new LinkedHashMap<>(this.row.entrySet()
                .stream()
                .collect(Collectors.toMap(function, Map.Entry::getValue))));
    }

    public InputData lastRow(final boolean isLastRow) {
        return new InputData(this.row, isLastRow);
    }

    public boolean evaluate(final String target) {
        return Boolean.parseBoolean(this.evaluateString(target));
    }

    public String evaluateString(final String target) {
        final String result = this.bind(target);
        final String exp = extractExpression(result);
        if (Objects.equals(result, exp)) {
            return result;
        }
        try {
            final JexlEngine jexl = new JexlBuilder().create();
            final JexlExpression expression = jexl.createExpression(exp);
            final JexlContext jc = new MapContext(new HashMap<>(this.row));
            return Optional.ofNullable(expression.evaluate(jc))
                    .map(it -> result.replace("${" + exp + "}", it.toString()))
                    .orElse(result);
        } catch (final JexlException ex) {
            return result;
        }
    }

    public String bind(final String s) {
        // Sub special keys using the !{keyname} syntax.
        String result = this.replaceKeys(s);
        // This kind of variable substitution makes for short code, but it's inefficient.
        result = this.replaceVars(result);
        if (!Objects.equals(s, result)) {
            return this.bind(result);
        }
        result = Context.bindEnvironmentProperties(result);
        if (!Objects.equals(s, result)) {
            return this.bind(result);
        }
        return result;
    }

    public Builder builder() {
        return new Builder(new LinkedHashMap<>(this.row), this.lastRow);
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return this.row.entrySet();
    }

    public boolean isLastRow() {
        return this.lastRow;
    }

    public InputData copy() {
        return new InputData(new LinkedHashMap<>(this.row), this.lastRow);
    }

    private String replaceKeys(String s) {
        for (final Keys k : Keys.values()) {
            s = s.replace("!{" + k.name() + "}", k.toString());
        }
        return s;
    }

    private String replaceVars(String variable) {
        for (final Map.Entry<String, String> v : this.row.entrySet()) {
            variable = variable.replace("${" + v.getKey() + "}", v.getValue());
        }
        return variable;
    }

    public record Builder(LinkedHashMap<String, String> row, boolean lastRow) {

        public InputData build() {
            return new InputData(new LinkedHashMap<>(this.row), this.lastRow);
        }

        public Builder add(final InputData shareInput) {
            this.row.putAll(shareInput.row);
            return this;
        }

        public Builder add(final Map<String, String> initialVars) {
            this.row.putAll(initialVars);
            return this;
        }

        public Builder add(final String key, final String value) {
            this.row.put(key, value);
            return this;
        }
    }
}
