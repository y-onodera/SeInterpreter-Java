package com.sebuilder.interpreter;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import org.openqa.selenium.Keys;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestData {

    public static final String ROW_NUMBER = "_rowNumber";
    private static final HashMap<String, String> EMPTY = Maps.newHashMap();

    private final Map<String, String> row;

    public TestData() {
        this.row = EMPTY;
    }

    public TestData(Map<String, String> row) {
        this.row = Maps.newHashMap(row);
    }

    public String rowNumber() {
        return this.get(ROW_NUMBER);
    }

    public String get(String key) {
        return this.row.get(key);
    }

    public TestData clearRowNumber() {
        TestData result = new TestData(this.row);
        result.row.remove(ROW_NUMBER);
        return result;
    }

    public TestData add(TestData shareInput) {
        TestData result = new TestData(this.row);
        result.row.putAll(shareInput.row);
        return result;
    }

    public TestData add(Map<String, String> initialVars) {
        TestData result = new TestData(this.row);
        result.row.putAll(initialVars);
        return result;
    }

    public TestData add(String key, String value) {
        TestData result = new TestData(this.row);
        result.row.put(key, value);
        return result;
    }

    public String bind(String s) {
        // Sub special keys using the !{keyname} syntax.
        s = replaceKeys(s);
        // This kind of variable substitution makes for short code, but it's inefficient.
        s = replaceVars(s);
        return s;
    }

    public Builder builder() {
        return new Builder(Maps.newHashMap(this.row));
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return this.row.entrySet();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestData testData = (TestData) o;
        return Objects.equal(row, testData.row);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(row);
    }

    @Override
    public String toString() {
        return "TestData{" +
                "row=" + row +
                '}';
    }

    public static class Builder {
        private final Map<String, String> row;

        public Builder(Map<String, String> row) {
            this.row = row;
        }

        public TestData build() {
            return new TestData(this.row);
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
