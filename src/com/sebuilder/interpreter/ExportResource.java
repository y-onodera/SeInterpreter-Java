package com.sebuilder.interpreter;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExportResource {
    private final Map<String, String> variables;
    private final JSONObject script;
    private final String datasourceFile;

    public ExportResource(JSONObject script, Map<String, String> variables, String datasourceFile) {
        this.script = script;
        this.variables = new LinkedHashMap<>(variables);
        this.datasourceFile = datasourceFile;
    }

    public static Builder builder(TestRun ctx) {
        return new Builder(ctx);
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public String getScript() throws JSONException {
        return script.toString(4);
    }

    public boolean hasDataSource() {
        return !Strings.isNullOrEmpty(datasourceFile);
    }

    public void outputDataSourceTemplate() throws IOException {
        Map<String, String> valuables = this.getVariables();
        File outputTo = new File(Context.getInstance().getTemplateOutputDirectory(), this.datasourceFile);
        CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(outputTo),
                Charset.forName(Context.getInstance().getDataSourceEncording())));
        writer.writeNext(valuables.keySet().toArray(new String[valuables.keySet().size()]));
        writer.writeNext(valuables.values().toArray(new String[valuables.values().size()]));
        writer.flush();
        writer.close();
    }

    public static class Builder {
        private final TestRun ctx;
        final Map<String, String> variables;
        final Map<String, Integer> duplicate;
        final JSONObject source;
        final JSONArray steps;
        JSONObject currentStep;
        final boolean needDatasource;

        public Builder(TestRun aCtx) {
            variables = new LinkedHashMap<>();
            duplicate = new HashMap<>();
            source = new JSONObject();
            steps = new JSONArray();
            currentStep = null;
            ctx = aCtx;
            needDatasource = this.ctx.containsKey("datasource");
        }

        public ExportResource build() throws JSONException {
            this.source.put("steps", this.steps);
            String datasourceFile = null;
            if (this.needDatasource) {
                datasourceFile = this.ctx.string("datasource");
                JSONObject configs = new JSONObject();
                JSONObject data = new JSONObject();
                JSONObject csv = new JSONObject();
                csv.put("path", datasourceFile);
                configs.put("csv", csv);
                data.put("configs", configs);
                data.put("source", "csv");
                this.source.put("data", data);
            }
            return new ExportResource(source, this.variables, datasourceFile);
        }

        public Builder addStep(Exportable source, RemoteWebDriver driver, WebElement element) {
            this.addStep(source.getTypeName())
                    .addLocator(source.hasLocator(), driver, element);
            source.addElement(this, driver, element);
            return this;
        }

        public Builder addStep(String typeName) {
            JSONObject step = new JSONObject();
            try {
                step.put("type", typeName);
                this.currentStep = step;
                this.steps.put(step);
            } catch (JSONException e) {
                this.ctx.log().error(e);
            }
            return this;
        }

        public Builder addLocator(boolean hasLocator, RemoteWebDriver driver, WebElement element) {
            if (!hasLocator) {
                return this;
            }
            return this.addLocator(Locator.of(driver, element));
        }

        public Builder addLocator(Locator element) {
            try {
                this.currentStep.put("locator", element.toJSON());
            } catch (JSONException e) {
                this.ctx.log().error(e);
            }
            return this;
        }

        public Builder stepOption(String opt, String value) {
            try {
                if (this.needDatasource && this.currentStep.has("locator")) {
                    JSONObject locatorJSON = (JSONObject) this.currentStep.get("locator");
                    Locator locator = new Locator(locatorJSON.getString("type"), locatorJSON.getString("value"));
                    String valuable = addVariable(locator.toPrettyString(), value);
                    this.currentStep.put(opt, valuable);
                } else {
                    this.currentStep.put(opt, value);
                }
            } catch (JSONException e) {
                this.ctx.log().error(e);
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
}
