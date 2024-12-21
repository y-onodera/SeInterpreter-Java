package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;

public class SelectElementValue extends AbstractStepType implements ConditionalStep, Exportable, LocatorHolder {

    @Override
    public boolean doRun(final TestRun ctx) {
        final WebElement e = ctx.locator().find(ctx);
        final Select select = new Select(e);
        select.selectByValue(ctx.string("value"));
        return true;
    }

    @Override
    public void addElement(final ExportResourceBuilder builder, final RemoteWebDriver driver, final WebElement element) {
        element.findElements(By.tagName("option"))
                .stream()
                .filter(WebElement::isSelected)
                .findFirst()
                .ifPresent(option -> builder.stepOption("value", option.getDomAttribute("value")));
    }

    @Override
    public StepBuilder addDefaultParam(final StepBuilder o) {
        if (!o.containsStringParam("value")) {
            o.put("value", "");
        }
        return o.apply(LocatorHolder.super::addDefaultParam);
    }

}

