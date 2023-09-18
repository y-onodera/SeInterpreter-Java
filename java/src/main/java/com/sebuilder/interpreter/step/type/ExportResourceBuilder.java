package com.sebuilder.interpreter.step.type;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ExportResourceBuilder {
    private final DataSourceFactory dataSourceFactory = Context.getDataSourceFactory();
    private final TestRun ctx;
    private final Map<String, String> variables;
    private final Map<String, Integer> duplicate;
    private final TestCaseBuilder source;
    private final ArrayList<StepBuilder> steps;
    private StepBuilder currentStep;
    private final boolean needDataSource;
    private final WebElement extractFrom;


    public ExportResourceBuilder(final TestRun aCtx) {
        this.variables = new LinkedHashMap<>();
        this.duplicate = new HashMap<>();
        this.source = new TestCaseBuilder();
        this.steps = new ArrayList<>();
        this.currentStep = null;
        this.ctx = aCtx;
        this.needDataSource = this.ctx.containsKey("datasource");
        final Locator locator;
        if (this.ctx.hasLocator()) {
            locator = this.ctx.locator();
        } else {
            locator = new Locator("css selector", "body");
        }
        this.extractFrom = locator.find(this.ctx);
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
        return new ExportResource(this.source.build(), this.variables, dataSourceFile);
    }

    public ExportResourceBuilder addInputStep(final boolean aIsAppend) {
        if (!aIsAppend) {
            return this;
        }
        this.extractFrom
                .findElements(By.tagName("input"))
                .forEach(element -> {
                    final String type = element.getAttribute("type");
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

    public ExportResourceBuilder addSelectStep(final boolean aIsAppend) {
        if (!aIsAppend) {
            return this;
        }
        this.extractFrom
                .findElements(By.tagName("select"))
                .forEach(element -> this.addStep(new SelectElementValue(), element));
        return this;
    }

    public ExportResourceBuilder addLinkClickStep(final boolean aIsAppend) {
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

    public ExportResourceBuilder addButtonClickStep(final boolean aIsAppend) {
        if (!aIsAppend) {
            return this;
        }
        this.extractFrom
                .findElements(By.tagName("button"))
                .forEach(element -> this.addStep(new ClickElement(), element));
        return this;
    }

    public ExportResourceBuilder addDivClickStep(final boolean aIsAppend) {
        if (!aIsAppend) {
            return this;
        }
        this.extractFrom
                .findElements(By.tagName("div"))
                .forEach(element -> this.addStep(new ClickElement(), element));
        return this;
    }

    public ExportResourceBuilder addSpanClickStep(final boolean aIsAppend) {
        if (!aIsAppend) {
            return this;
        }
        this.extractFrom
                .findElements(By.tagName("span"))
                .forEach(element -> this.addStep(new ClickElement(), element));
        return this;
    }

    public ExportResourceBuilder addStep(final Exportable source, final WebElement element) {
        this.addStep(source.getTypeName())
                .addLocator(source.hasLocator(), element);
        source.addElement(this, this.ctx.driver(), element);
        return this;
    }

    public ExportResourceBuilder addStep(final String typeName) {
        this.currentStep = Context.createStepBuilder(typeName);
        this.steps.add(this.currentStep);
        return this;
    }

    public ExportResourceBuilder addLocator(final boolean hasLocator, final WebElement element) {
        if (!hasLocator) {
            return this;
        }
        return this.addLocator(this.ctx.detectLocator(element));
    }

    public ExportResourceBuilder addLocator(final Locator element) {
        this.currentStep.put("locator", element);
        return this;
    }

    public ExportResourceBuilder stepOption(final String opt, final String value) {
        if (this.needDataSource && this.currentStep.containsLocatorParam("locator")) {
            final Locator locator = this.currentStep.getLocatorParams().get("locator");
            final String valuable = this.addVariable(locator.toPrettyString(), value);
            this.currentStep.put(opt, valuable)
                    .skip("${!has('" + valuable.replace("${", "")
                            .replace("}", "") + "')}");
        } else {
            this.currentStep.put(opt, value);
        }
        return this;
    }

    private String addVariable(final String aVariable, final String aValue) {
        if (this.variables.containsKey(aVariable)) {
            int newNo = 2;
            final Integer no = this.duplicate.get(aVariable);
            if (no != null) {
                newNo = no + 1;
            }
            return "${" + this.resolveDuplicate(aVariable, newNo, aValue) + "}";
        }
        this.variables.put(aVariable, aValue);
        return "${" + aVariable + "}";
    }

    private String resolveDuplicate(final String aVariable, final Integer newNo, final String aValue) {
        this.duplicate.put(aVariable, newNo);
        final String result = aVariable + newNo.toString();
        this.variables.put(result, aValue);
        return result;
    }

}
