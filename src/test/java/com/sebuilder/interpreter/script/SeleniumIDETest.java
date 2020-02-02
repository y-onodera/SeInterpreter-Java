package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.TestRunListener;
import com.sebuilder.interpreter.application.TestRunListenerImpl;
import com.sebuilder.interpreter.datasource.DataSourceFactoryImpl;
import com.sebuilder.interpreter.step.StepTypeFactoryImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
    protected TestRunListener testRunListener = new TestRunListenerImpl(log);

    @Test
    public void load() throws IOException {
        TestCase expect = new Sebuilder().load(new File(baseDir, "expect/sampleScript.json"), testRunListener);
        TestCase loaded = target.load(new File(baseDir, "sampleScript.side"), testRunListener);
        assertEquals(toStringConverter.toString(expect), toStringConverter.toString(loaded));
        assertEquals(toStringConverter.toString(expect.getChains().get(0)), toStringConverter.toString(loaded.getChains().get(0)));
        assertEquals(toStringConverter.toString(expect.getChains().get(1)), toStringConverter.toString(loaded.getChains().get(1)));
    }

}