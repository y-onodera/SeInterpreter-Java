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

public record ExportResource(TestCase script,
                             Map<String, String> variables, File dataSourceFile) {
    public ExportResource(final TestCase script, final Map<String, String> variables, final File dataSourceFile) {
        this.script = script;
        this.variables = new LinkedHashMap<>(variables);
        this.dataSourceFile = dataSourceFile;
    }

    public Map<String, String> getVariables() {
        return this.variables;
    }

    public TestCase getScript() {
        return this.script;
    }

    public boolean hasDataSource() {
        return this.dataSourceFile != null;
    }

    public void outputDataSourceTemplate() throws IOException {
        final Map<String, String> valuables = this.getVariables();
        final CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(this.dataSourceFile),
                Charset.forName(Context.getDataSourceEncoding())));
        writer.writeNext(valuables.keySet().toArray(new String[0]));
        writer.writeNext(valuables.values().toArray(new String[0]));
        writer.flush();
        writer.close();
    }

}
