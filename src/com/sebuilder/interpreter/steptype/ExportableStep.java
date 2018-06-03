package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.StepType;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public abstract class ExportableStep implements StepType {
    public JSONObject toJSON(RemoteWebDriver driver, WebElement element) throws JSONException {
        JSONObject step = new JSONObject();
        String simpleClassName = this.getClass().getSimpleName();
        step.put("type", simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1));
        if (this.hasLocator()) {
            step.put("locator", Locator.toJson(driver, element));
        }
        addElement(driver, element, step);
        return step;
    }

    protected boolean hasLocator() {
        return true;
    }

    protected void addElement(RemoteWebDriver driver, WebElement element, JSONObject step) throws JSONException {
        // non defalut implementation
    }
}
