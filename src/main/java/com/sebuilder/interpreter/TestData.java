package com.sebuilder.interpreter;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import org.apache.commons.jexl3.*;
import org.openqa.selenium.Keys;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestData {

    public static final String ROW_NUMBER = "_rowNumber";
    private static final HashMap<String, String> EMPTY = Maps.newHashMap();

    private final Map<String, String> row;

    private final boolean lastRow;

    public TestData() {
        this(EMPTY, true);
    }

    public TestData(Map<String, String> row) {
        this(row, false);
    }

    public TestData(Map<String, String> row, boolean lastRow) {
        this.row = Maps.newHashMap(row);
        this.lastRow = lastRow;
    }

    public String rowNumber() {
        return this.get(ROW_NUMBER);
    }

    public String get(String key) {
        return this.row.get(key);
    }

    public TestData clearRowNumber() {
        TestData result = copy();
        result.row.remove(ROW_NUMBER);
        return result;
    }

    public TestData add(TestData shareInput) {
        TestData result = this.copy();
        result.row.putAll(shareInput.row);
        return result;
    }

    public TestData add(Map<String, String> initialVars) {
        TestData result = this.copy();
        result.row.putAll(initialVars);
        return result;
    }

    public TestData add(String key, String value) {
        TestData result = this.copy();
        result.row.put(key, value);
        return result;
    }

    public TestData lastRow(boolean isLastRow) {
        return new TestData(this.row, isLastRow);
    }

    public boolean evaluate(String target) {
        String result = this.bind(target);
        String exp = this.extractExpression(result);
        if (Objects.equal(result, exp)) {
            return Boolean.valueOf(result);
        }
        try {
            JexlEngine jexl = new JexlBuilder().create();
            JexlExpression expression = jexl.createExpression(exp);
            JexlContext jc = new MapContext(new HashMap(this.row));
            return Boolean.valueOf(expression.evaluate(jc).toString());
        } catch (JexlException ex) {
            return false;
        }
    }

    public String bind(String s) {
        // Sub special keys using the !{keyname} syntax.
        String result = this.replaceKeys(s);
        // This kind of variable substitution makes for short code, but it's inefficient.
        result = this.replaceVars(result);
        if (!Objects.equal(s, result)) {
            return this.bind(result);
        }
        return result;
    }

    public Builder builder() {
        return new Builder(Maps.newHashMap(this.row), this.lastRow);
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return this.row.entrySet();
    }

    public boolean isLastRow() {
        return this.lastRow;
    }

    protected TestData copy() {
        return new TestData(this.row, this.lastRow);
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
        return result.replaceAll("\\$\\{(.+)\\}", "$1");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestData testData = (TestData) o;
        return isLastRow() == testData.isLastRow() &&
                Objects.equal(row, testData.row);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(row, isLastRow());
    }

    @Override
    public String toString() {
        return "TestData{" +
                "row=" + row +
                ", lastRow=" + lastRow +
                '}';
    }

    public static class Builder {
        private final Map<String, String> row;

        private final boolean lastRow;

        public Builder(Map<String, String> row, boolean lastRow) {
            this.row = row;
            this.lastRow = lastRow;
        }

        public TestData build() {
            return new TestData(this.row, this.lastRow);
        }

        public Builder add(TestData shareInput) {
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
