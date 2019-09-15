package com.sebuilder.interpreter.step.type;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.datasource.DataSourceFactoryImpl;
import com.sebuilder.interpreter.step.StepTypeFactoryImpl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ExportResourceBuilder {
    private StepTypeFactory stepTypeFactory = new StepTypeFactoryImpl();
    private DataSourceFactory dataSourceFactory = new DataSourceFactoryImpl();
    private final TestRun ctx;
    private final Map<String, String> variables;
    private final Map<String, Integer> duplicate;
    private final TestCaseBuilder source;
    private final ArrayList<StepBuilder> steps;
    private StepBuilder currentStep;
    private final boolean needDataSource;
    private final Locator locator;
    private final WebElement extractFrom;


    public ExportResourceBuilder(TestRun aCtx) {
        this.variables = new LinkedHashMap<>();
        this.duplicate = new HashMap<>();
        this.source = new TestCaseBuilder();
        this.steps = Lists.newArrayList();
        this.currentStep = null;
        this.ctx = aCtx;
        this.needDataSource = this.ctx.containsKey("datasource");
        if (this.ctx.hasLocator()) {
            this.locator = this.ctx.locator();
        } else {
            this.locator = new Locator("css selector", "body");
        }
        extractFrom = this.locator.find(this.ctx);
    }

    public ExportResource build() {
        this.source.addSteps(this.steps.stream()
                .map(it -> it.build())
                .collect(Collectors.toCollection(ArrayList::new)));
        File dataSourceFile = null;
        if (this.needDataSource) {
            final String fileName = this.ctx.string("datasource");
            dataSourceFile = new File(this.ctx.getListener().getTemplateOutputDirectory(), fileName);
            this.source.setDataSource(this.dataSourceFactory.getDataSource("csv"), Map.of("path", fileName));
        }
        return new ExportResource(source.build(), this.variables, dataSourceFile);
    }

    public ExportResourceBuilder addInputStep(boolean aIsAppend) {
        if (!aIsAppend) {
            return this;
        }
        this.extractFrom
                .findElements(By.tagName("input"))
                .stream()
                .forEach(element -> {
                    String type = element.getAttribute("type");
                    switch (type) {
                        case "text":
                            this.addStep(new SetElementText(), this.ctx.driver(), element);
                            break;
                        case "checkbox":
                        case "radio":
                            this.addStep(new SetElementSelected(), this.ctx.driver(), element);
                            break;
                        case "hidden":
                            break;
                        default:
                            this.addStep(new SetElementText(), this.ctx.driver(), element);
                    }
                });
        this.extractFrom
                .findElements(By.tagName("textarea"))
                .stream()
                .forEach(element -> {
                    this.addStep(new SetElementText(), this.ctx.driver(), element);
                });
        return this;
    }

    public ExportResourceBuilder addSelectStep(boolean aIsAppend) {
        if (!aIsAppend) {
            return this;
        }
        this.extractFrom
                .findElements(By.tagName("select"))
                .stream()
                .forEach(element -> {
                    element.findElements(By.tagName("option"))
                            .stream()
                            .filter(option -> option.isSelected())
                            .forEach(option -> {
                                this.addStep(new SetElementSelected(), this.ctx.driver(), option);
                            });
                });
        return this;
    }

    public ExportResourceBuilder addLinkClickStep(boolean aIsAppend) {
        if (!aIsAppend) {
            return this;
        }
        this.extractFrom
                .findElements(By.tagName("a"))
                .stream()
                .filter(element -> !Strings.isNullOrEmpty(element.getText()))
                .forEach(element -> {
                    this.addStep(new ClickElement(), this.ctx.driver(), element);
                });
        return this;
    }

    public ExportResourceBuilder addButtonClickStep(boolean aIsAppend) {
        if (!aIsAppend) {
            return this;
        }
        this.extractFrom
                .findElements(By.tagName("button"))
                .stream()
                .forEach(element -> {
                    this.addStep(new ClickElement(), this.ctx.driver(), element);
                });
        return this;
    }

    public ExportResourceBuilder addDivClickStep(boolean aIsAppend) {
        if (!aIsAppend) {
            return this;
        }
        this.extractFrom
                .findElements(By.tagName("div"))
                .stream()
                .forEach(element -> {
                    this.addStep(new ClickElement(), this.ctx.driver(), element);
                });
        return this;
    }

    public ExportResourceBuilder addSpanClickStep(boolean aIsAppend) {
        if (!aIsAppend) {
            return this;
        }
        this.extractFrom
                .findElements(By.tagName("span"))
                .stream()
                .forEach(element -> {
                    this.addStep(new ClickElement(), this.ctx.driver(), element);
                });
        return this;
    }

    public ExportResourceBuilder addStep(Exportable source, RemoteWebDriver driver, WebElement element) {
        this.addStep(source.getTypeName())
                .addLocator(source.hasLocator(), driver, element);
        source.addElement(this, driver, element);
        return this;
    }

    public ExportResourceBuilder addStep(String typeName) {
        this.currentStep = new StepBuilder(this.stepTypeFactory.getStepTypeOfName(typeName));
        this.steps.add(this.currentStep);
        return this;
    }

    public ExportResourceBuilder addLocator(boolean hasLocator, RemoteWebDriver driver, WebElement element) {
        if (!hasLocator) {
            return this;
        }
        Locator result = Locator.of(driver, element);
        if (result.value.contains("//select[@id=")) {
            String id = "id:" + result.value.replaceAll(".+(?=@id='([^']+)').*", "$1");
            String value = result.value.replaceAll(".+(?=@value='([^']*)').*", "@value='$1'");
            this.addVariable(id, value.replaceAll("@value='(.*)'", "$1"));
            result = new Locator(result.type.toString(), result.value.replace(value, "@value='${" + id + "}'"));
        }
        return this.addLocator(result);
    }

    public ExportResourceBuilder addLocator(Locator element) {
        this.currentStep.put("locator", element);
        return this;
    }

    public ExportResourceBuilder stepOption(String opt, String value) {
        if (this.needDataSource && this.currentStep.containsLocatorParam("locator")) {
            Locator locator = this.currentStep.getLocatorParams().get("locator");
            if (locator.value.contains("//select[@id=")) {
                this.currentStep.put(opt, value);
            } else {
                String valuable = addVariable(locator.toPrettyString(), value);
                this.currentStep.put(opt, valuable);
            }
        } else {
            this.currentStep.put(opt, value);
        }
        return this;
    }

    private String addVariable(String aVariable, String aValue) {
        if (variables.containsKey(aVariable)) {
            Integer newNo = Integer.valueOf(2);
            Integer no = duplicate.get(aVariable);
            if (no != null) {
                newNo = Integer.valueOf(no.intValue() + 1);
            }
            return "${" + resolveDuplicate(aVariable, newNo, aValue) + "}";
        }
        variables.put(aVariable, aValue);
        return "${" + aVariable + "}";
    }

    private String resolveDuplicate(String aVariable, Integer newNo, String aValue) {
        duplicate.put(aVariable, newNo);
        String result = aVariable + newNo.toString();
        variables.put(result, aValue);
        return result;
    }

}
