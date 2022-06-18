package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.TestRunListener;
import com.sebuilder.interpreter.report.JunitTestRunListener;
import com.sebuilder.interpreter.datasource.DataSourceFactoryImpl;
import com.sebuilder.interpreter.script.seleniumide.SeleniumIDE;
import com.sebuilder.interpreter.step.StepTypeFactoryImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class SeleniumIDETest {
    static {
        Context.getInstance()
                .setStepTypeFactory(new StepTypeFactoryImpl())
                .setDataSourceFactory(new DataSourceFactoryImpl());
    }

    static Logger log = LogManager.getLogger(SeleniumIDETest.class);
    private String baseDir = SeleniumIDETest.class.getResource("./seleniumide").getPath();
    private SeleniumIDE target = new SeleniumIDE();
    private SebuilderToStringConverter toStringConverter = new SebuilderToStringConverter();
    protected TestRunListener testRunListener = new JunitTestRunListener(log);

    @Test
    public void load() throws IOException {
        TestCase expect = new Sebuilder().load(new File(baseDir, "expect/sampleScript.json"));
        TestCase loaded = target.load(new File(baseDir, "sampleScript.side"));
        assertEquals(toStringConverter.toString(expect), toStringConverter.toString(loaded));
        assertChainCaseEquals(expect, loaded);
    }

    private void assertChainCaseEquals(TestCase expect, TestCase loaded) {
        assertEquals(expect.chains().size(), loaded.chains().size());
        for (int i = 0, j = expect.chains().size(); i < j; i++) {
            assertEquals(toStringConverter.toString(expect.chains().get(i)), toStringConverter.toString(loaded.chains().get(i)));
            assertChainCaseEquals(expect.chains().get(i), loaded.chains().get(i));
        }
    }

}