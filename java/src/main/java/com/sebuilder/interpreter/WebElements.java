package com.sebuilder.interpreter;

import org.openqa.selenium.WebElement;

public enum WebElements {
    SINGLETON;

    private static final String JAVA_SCRIPT_ELEMENT_TO_XPATH = new StringBuilder()
            .append("function getElementXPath(elt){")
            .append("var path = \"\";")
            .append("for (; elt && elt.nodeType == 1; elt = elt.parentNode){")
            .append("idx = getElementIdx(elt);")
            .append("xname = elt.tagName;")
            .append("if (idx > 1){")
            .append("xname += \"[\" + idx + \"]\";")
            .append("}")
            .append("path = \"/\" + xname + path;")
            .append("}")
            .append("return path;")
            .append("}")
            .append("function getElementIdx(elt){")
            .append("var count = 1;")
            .append("for (var sib = elt.previousSibling; sib ; sib = sib.previousSibling){")
            .append("if(sib.nodeType == 1 && sib.tagName == elt.tagName){")
            .append("count++;")
            .append("}")
            .append("}")
            .append("return count;")
            .append("}")
            .append("return getElementXPath(arguments[0]).toLowerCase();")
            .toString();

    public static String toXpath(final WebDriverWrapper driver, final WebElement element) {
        return (String) driver.executeScript(JAVA_SCRIPT_ELEMENT_TO_XPATH, element);
    }
}
