package com.sebuilder.interpreter.step.type;

import com.opencsv.CSVWriter;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExportResource {
    private final Map<String, String> variables;
    private final TestCase script;
    private final File dataSourceFile;

    public ExportResource(TestCase script, Map<String, String> variables, File dataSourceFile) {
        this.script = script;
        this.variables = new LinkedHashMap<>(variables);
        this.dataSourceFile = dataSourceFile;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public TestCase getScript() {
        return script;
    }

    public File getDataSourceFile() {
        return dataSourceFile;
    }

    public boolean hasDataSource() {
        return this.dataSourceFile != null;
    }

    public void outputDataSourceTemplate() throws IOException {
        Map<String, String> valuables = this.getVariables();
        CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(this.dataSourceFile),
                Charset.forName(Context.getDataSourceEncoding())));
        writer.writeNext(valuables.keySet().toArray(new String[valuables.keySet().size()]));
        writer.writeNext(valuables.values().toArray(new String[valuables.values().size()]));
        writer.flush();
        writer.close();
    }

}
