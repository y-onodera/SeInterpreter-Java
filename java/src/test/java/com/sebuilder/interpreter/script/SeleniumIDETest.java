package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.datasource.DataSourceFactoryImpl;
import com.sebuilder.interpreter.script.seleniumide.SeleniumIDE;
import com.sebuilder.interpreter.step.StepTypeFactoryImpl;
import org.junit.Test;

import java.io.File;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class SeleniumIDETest {
    static {
        Context.getInstance()
                .setStepTypeFactory(new StepTypeFactoryImpl())
                .setDataSourceFactory(new DataSourceFactoryImpl());
    }

    private final String baseDir = Objects.requireNonNull(SeleniumIDETest.class.getResource("./seleniumide")).getPath();
    private final SeleniumIDE target = new SeleniumIDE();
    private final SebuilderToStringConverter toStringConverter = new SebuilderToStringConverter();

    @Test
    public void load() {
        final TestCase expect = new Sebuilder().load(new File(this.baseDir, "expect/sampleScript.json"));
        final TestCase loaded = this.target.load(new File(this.baseDir, "sampleScript.side"));
        assertEquals(this.toStringConverter.toString(expect), this.toStringConverter.toString(loaded));
        this.assertChainCaseEquals(expect, loaded);
    }

    private void assertChainCaseEquals(final TestCase expect, final TestCase loaded) {
        assertEquals(expect.chains().size(), loaded.chains().size());
        for (int i = 0, j = expect.chains().size(); i < j; i++) {
            assertEquals(this.toStringConverter.toString(expect.chains().get(i)), this.toStringConverter.toString(loaded.chains().get(i)));
            this.assertChainCaseEquals(expect.chains().get(i), loaded.chains().get(i));
        }
    }

}