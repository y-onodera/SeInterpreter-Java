package com.sebuilder.interpreter.step.type;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.datasource.DataSourceFactoryImpl;
import com.sebuilder.interpreter.step.StepTypeFactoryImpl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ExportResourceBuilder {
    private final StepTypeFactory stepTypeFactory = new StepTypeFactoryImpl();
    private final DataSourceFactory dataSourceFactory = new DataSourceFactoryImpl();
    private final TestRun ctx;
    private final Map<String, String> variables;
    private final Map<String, Integer> duplicate;
    private final TestCaseBuilder source;
    private final ArrayList<StepBuilder> steps;
    private StepBuilder currentStep;
    private final boolean needDataSource;
    private final WebElement extractFrom;


    public ExportResourceBuilder(TestRun aCtx) {
        this.variables = new LinkedHashMap<>();
        this.duplicate = new HashMap<>();
        this.source = new TestCaseBuilder();
        this.steps = Lists.newArrayList();
        this.currentStep = null;
        this.ctx = aCtx;
        this.needDataSource = this.ctx.containsKey("datasource");
        Locator locator;
        if (this.ctx.hasLocator()) {
            locator = this.ctx.locator();
        } else {
            locator = new Locator("css selector", "body");
        }
        extractFrom = locator.find(this.ctx);
    }

    public ExportResource build() {
        this.source.addSteps(this.steps.stream()
                .map(StepBuilder::build)
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
                .forEach(element -> {
                    String type = element.getAttribute("type");
                    switch (type) {
                        case "checkbox":
                        case "radio":
                            this.addStep(new SetElementSelected(), element);
                            break;
                        case "hidden":
                            break;
                        case "text":
                        default:
                            this.addStep(new SetElementText(), element);
                    }
                });
        this.extractFrom
                .findElements(By.tagName("textarea"))
                .forEach(element -> this.addStep(new SetElementText(), element));
        return this;
    }

    public ExportResourceBuilder addSelectStep(boolean aIsAppend) {
        if (!aIsAppend) {
            return this;
        }
        this.extractFrom
                .findElements(By.tagName("select"))
                .forEach(element -> this.addStep(new SelectElementValue(), element));
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
                .forEach(element -> this.addStep(new ClickElement(), element));
        return this;
    }

    public ExportResourceBuilder addButtonClickStep(boolean aIsAppend) {
        if (!aIsAppend) {
            return this;
        }
        this.extractFrom
                .findElements(By.tagName("button"))
                .forEach(element -> this.addStep(new ClickElement(), element));
        return this;
    }

    public ExportResourceBuilder addDivClickStep(boolean aIsAppend) {
        if (!aIsAppend) {
            return this;
        }
        this.extractFrom
                .findElements(By.tagName("div"))
                .forEach(element -> this.addStep(new ClickElement(), element));
        return this;
    }

    public ExportResourceBuilder addSpanClickStep(boolean aIsAppend) {
        if (!aIsAppend) {
            return this;
        }
        this.extractFrom
                .findElements(By.tagName("span"))
                .forEach(element -> this.addStep(new ClickElement(), element));
        return this;
    }

    public ExportResourceBuilder addStep(Exportable source, WebElement element) {
        this.addStep(source.getTypeName())
                .addLocator(source.hasLocator(), element);
        source.addElement(this, this.ctx.driver(), element);
        return this;
    }

    public ExportResourceBuilder addStep(String typeName) {
        this.currentStep = new StepBuilder(this.stepTypeFactory.getStepTypeOfName(typeName));
        this.steps.add(this.currentStep);
        return this;
    }

    public ExportResourceBuilder addLocator(boolean hasLocator, WebElement element) {
        if (!hasLocator) {
            return this;
        }
        return this.addLocator(this.ctx.detectLocator(element));
    }

    public ExportResourceBuilder addLocator(Locator element) {
        this.currentStep.put("locator", element);
        return this;
    }

    public ExportResourceBuilder stepOption(String opt, String value) {
        if (this.needDataSource && this.currentStep.containsLocatorParam("locator")) {
            Locator locator = this.currentStep.getLocatorParams().get("locator");
            String valuable = this.addVariable(locator.toPrettyString(), value);
            this.currentStep.put(opt, valuable)
                    .skip("${!has('" + valuable.replace("${", "")
                            .replace("}", "") + "')}");
        } else {
            this.currentStep.put(opt, value);
        }
        return this;
    }

    private String addVariable(String aVariable, String aValue) {
        if (variables.containsKey(aVariable)) {
            int newNo = 2;
            Integer no = duplicate.get(aVariable);
            if (no != null) {
                newNo = no + 1;
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
