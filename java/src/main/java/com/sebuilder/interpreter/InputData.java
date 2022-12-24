package com.sebuilder.interpreter;

import org.apache.commons.jexl3.*;
import org.openqa.selenium.Keys;

import java.util.*;
import java.util.stream.Collectors;

public record InputData(LinkedHashMap<String, String> row, boolean lastRow) {

    public static final String ROW_NUMBER = "_rowNumber";
    private static final LinkedHashMap<String, String> EMPTY = new LinkedHashMap<>();
    private static final String REGEX_EXPRESSION = ".*\\$\\{(.+)}.*";

    public InputData() {
        this(EMPTY, true);
    }

    public InputData(LinkedHashMap<String, String> row) {
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

    public String get(String key) {
        return this.row.get(key);
    }

    public InputData clearRowNumber() {
        InputData result = this.copy();
        result.row.remove(ROW_NUMBER);
        return result;
    }

    public InputData add(InputData shareInput) {
        InputData result = this.copy();
        result.row.putAll(shareInput.row);
        return result;
    }

    public InputData add(Map<String, String> initialVars) {
        InputData result = this.copy();
        result.row.putAll(initialVars);
        return result;
    }

    public InputData add(String key, String value) {
        InputData result = this.copy();
        result.row.put(key, value);
        return result;
    }

    public InputData remove(String key) {
        InputData result = this.copy();
        result.row.remove(key);
        return result;
    }

    public InputData lastRow(boolean isLastRow) {
        return new InputData(this.row, isLastRow);
    }

    public boolean evaluate(String target) {
        return Boolean.parseBoolean(this.evaluateString(target));
    }

    public String evaluateString(String target) {
        String result = this.bind(target);
        String exp = this.extractExpression(result);
        if (Objects.equals(result, exp)) {
            return result;
        }
        try {
            JexlEngine jexl = new JexlBuilder().create();
            JexlExpression expression = jexl.createExpression(exp);
            JexlContext jc = new MapContext(new HashMap<>(this.row));
            return result.replace("${" + exp + "}", expression.evaluate(jc).toString());
        } catch (JexlException ex) {
            return result;
        }
    }

    public String bind(String s) {
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
        for (Keys k : Keys.values()) {
            s = s.replace("!{" + k.name() + "}", k.toString());
        }
        return s;
    }

    private String replaceVars(String variable) {
        for (Map.Entry<String, String> v : this.row.entrySet()) {
            variable = variable.replace("${" + v.getKey() + "}", v.getValue());
        }
        return variable;
    }

    private String extractExpression(String result) {
        return result.replaceAll(REGEX_EXPRESSION, "$1");
    }

    public record Builder(LinkedHashMap<String, String> row, boolean lastRow) {

        public InputData build() {
            return new InputData(new LinkedHashMap<>(this.row), this.lastRow);
        }

        public Builder add(InputData shareInput) {
            this.row.putAll(shareInput.row);
            return this;
        }

        public Builder add(Map<String, String> initialVars) {
            this.row.putAll(initialVars);
            return this;
        }

        public Builder add(String key, String value) {
            this.row.put(key, value);
            return this;
        }
    }
}
