package com.sebuilder.interpreter;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestDataInput {

    private InputData target = new InputData()
            .add("aTrue", "true")
            .add("aFalse", "false")
            .add("number", "1")
            .add("var1", "string")
            .add("empty", "")
            .add("nestedVar", "${var1}");

    @Test
    public void lastRow() {
        assertTrue(target.isLastRow());
        assertTrue(target.lastRow(true).isLastRow());
        assertFalse(target.lastRow(false).isLastRow());
    }

    @Test
    public void evaluateLiteralIsBooleanValueOf() {
        assertTrue(target.evaluate("true"));
        assertTrue(target.evaluate("TRUE"));
        assertTrue(target.evaluate("True"));
        assertFalse(target.evaluate("false"));
        assertFalse(target.evaluate("FALSE"));
        assertFalse(target.evaluate("False"));
        assertFalse(target.evaluate("1"));
        assertFalse(target.evaluate("0"));
    }

    @Test
    public void evaluateLiteralAfterReplaceKeyValue() {
        assertTrue(target.evaluate("${aTrue}"));
        assertFalse(target.evaluate("${aFalse}"));
    }

    @Test
    public void evaluateExpression() {
        assertTrue(target.evaluate("${aTrue == true}"));
        assertTrue(target.evaluate("${(number * 1) + 5 == var1.length()}"));
    }

    @Test
    public void evaluateReplaceAndEval() {
        assertTrue(target.evaluate("${'${var1}' == '${nestedVar}'}"));
        assertTrue(target.evaluate("${'string' == '${nestedVar}'}"));
        assertTrue(target.evaluate("${var1 == '${nestedVar}'}"));
    }
}