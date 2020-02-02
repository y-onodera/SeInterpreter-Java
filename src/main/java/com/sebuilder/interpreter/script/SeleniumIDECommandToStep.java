package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.step.LocatorHolder;
import com.sebuilder.interpreter.step.type.*;
import org.json.JSONException;

import java.util.Map;

public class SeleniumIDECommandToStep {

    private static final Map<String, StepType> CONVERT_MAP = Map.of(
            "click", new ClickElement()
            , "click at", new ClickElementAt()
            , "type", new SetElementText()
            , "send keys", new SendKeysToElement()
            , "select", new SetElementSelected()
            , "open", new Get()
            , "setWindowSize", new SetWindowSize()
    );

    public static boolean support(SeleniumIDE.Command command) throws JSONException {
        return CONVERT_MAP.containsKey(command.command());
    }

    public static Step convert(SeleniumIDE.Command command) throws JSONException {
        StepBuilder builder = new StepBuilder(CONVERT_MAP.get(command.command()));
        if (builder.getStepType() instanceof SetElementSelected) {
            String optionPath = String.format("/option[. = '%s']", command.value().replace("label=", ""));
            builder.locator(new Locator("xpath", targetToXpath(command) + optionPath));
        } else if (builder.getStepType() instanceof LocatorHolder) {
            String[] locatorTarget = command.target().split("=", 2);
            if (locatorTarget.length == 2) {
                builder.locator(new Locator(convertLocatorType(locatorTarget[0]), locatorTarget[1]));
            }
            if (builder.getStepType() instanceof SetElementText
                    || builder.getStepType() instanceof SendKeysToElement) {
                builder.getStringParams().put("text", command.value());
            } else if (builder.getStepType() instanceof ClickElementAt) {
                String[] point = command.value().split(",");
                if (point.length == 2) {
                    builder.getStringParams().put("pointX", point[0]);
                    builder.getStringParams().put("pointY", point[1]);
                }
            }
        } else if (builder.getStepType() instanceof SetWindowSize) {
            String[] size = command.target().split("x", 2);
            if (size.length == 2) {
                builder.getStringParams().put("width", size[0]);
                builder.getStringParams().put("height", size[1]);
            }
        } else if (builder.getStepType() instanceof Get) {
            builder.getStringParams().put("url", command.target());
        }
        return builder.build();
    }

    private static String convertLocatorType(String s) {
        return s.replace("css", "css selector")
                .replace("linkText", "link text");
    }

    private static String targetToXpath(SeleniumIDE.Command command) throws JSONException {
        String target = command.target();
        if (target.startsWith("id=")) {
            return String.format("//*[@id='%s']", target.replace("id=", ""));
        } else if (target.startsWith("name=")) {
            return String.format("//*[@name='%s']", target.replace("name=", ""));
        }
        return target.replace("xpath=", "");
    }

    private static String targetsToXpath(SeleniumIDE.Command command) throws JSONException {
        Map<String, String> locators = command.targets();
        if (locators.containsKey("xpath:attributes")) {
            return locators.get("xpath:attributes").replace("xpath=", "");
        } else if (locators.containsKey("xpath:idRelative")) {
            return locators.get("xpath:idRelative").replace("xpath=", "");
        }
        return locators.get("xpath:position").replace("xpath=", "");
    }

}
