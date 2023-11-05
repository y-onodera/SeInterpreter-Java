package com.sebuilder.interpreter;

import org.junit.Test;

import static org.junit.Assert.*;

public class InputDataTest {

    private final InputData target = new InputData()
            .add("aTrue", "true")
            .add("aFalse", "false")
            .add("number", "1")
            .add("var1", "string")
            .add("empty", "")
            .add("nestedVar", "${var1}");

    @Test
    public void hasExpression() {
        assertTrue(InputData.hasExpression("${aTrue == true}"));
        assertTrue(InputData.hasExpression("${(number * 1) + 5 == var1.length()}"));
        assertTrue(InputData.hasExpression("${aTrue == true} && ${aTrue == false}"));
        assertTrue(InputData.hasExpression("${var1 == '${nestedVar}'}"));
        assertFalse(InputData.hasExpression("{aTrue == true}"));
        assertFalse(InputData.hasExpression("aTrue == true"));
        assertFalse(InputData.hasExpression(""));
    }

    @Test
    public void lastRow() {
        assertTrue(this.target.isLastRow());
        assertTrue(this.target.lastRow(true).isLastRow());
        assertFalse(this.target.lastRow(false).isLastRow());
    }

    @Test
    public void evaluateLiteralIsBooleanValueOf() {
        assertTrue(this.target.evaluate("true"));
        assertTrue(this.target.evaluate("TRUE"));
        assertTrue(this.target.evaluate("True"));
        assertFalse(this.target.evaluate("false"));
        assertFalse(this.target.evaluate("FALSE"));
        assertFalse(this.target.evaluate("False"));
        assertFalse(this.target.evaluate("1"));
        assertFalse(this.target.evaluate("0"));
    }

    @Test
    public void evaluateLiteralAfterReplaceKeyValue() {
        assertTrue(this.target.evaluate("${aTrue}"));
        assertFalse(this.target.evaluate("${aFalse}"));
    }

    @Test
    public void evaluateExpression() {
        assertTrue(this.target.evaluate("${aTrue == true}"));
        assertTrue(this.target.evaluate("${(number * 1) + 5 == var1.length()}"));
    }

    @Test
    public void evaluateReplaceAndEval() {
        assertTrue(this.target.evaluate("${'${var1}' == '${nestedVar}'}"));
        assertTrue(this.target.evaluate("${'string' == '${nestedVar}'}"));
        assertTrue(this.target.evaluate("${var1 == '${nestedVar}'}"));
    }

    @Test
    public void evaluateString() {
        assertEquals("true && true", this.target.evaluateString("${aTrue == true} && ${aFalse == false}"));
        assertEquals("true", this.target.evaluateString("${aTrue == true && aFalse == false}"));
        assertEquals("true", this.target.evaluateString("${${aTrue == true} && ${aFalse == false}}"));
    }

}