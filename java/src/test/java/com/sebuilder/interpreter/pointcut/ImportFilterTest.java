package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.script.ImportLoader;
import com.sebuilder.interpreter.script.PointcutLoader;
import com.sebuilder.interpreter.script.SebuilderTest;
import com.sebuilder.interpreter.step.type.ClickElement;
import com.sebuilder.interpreter.step.type.SelectElementValue;
import com.sebuilder.interpreter.step.type.SetElementSelected;
import com.sebuilder.interpreter.step.type.SetElementText;
import junit.framework.TestCase;

import java.io.File;
import java.util.Objects;

public class ImportFilterTest extends TestCase {
    private static final String baseDir = Objects.requireNonNull(SebuilderTest.class.getResource(".")).getPath();

    public void testWhereBlank() {
        final ImportFilter target = new ImportFilter("${excludeImport}", "", new PointcutLoader.ImportFunction(new PointcutLoader(new ImportLoader())
                , new File(baseDir).getAbsoluteFile()));
        assertTrue(target.isHandle(null, new SetElementText().toStep().build(), new InputData().add("excludeImport", "../script/pointcut/typeFilter.json")));
        assertTrue(target.isHandle(null, new SelectElementValue().toStep().build(), new InputData().add("excludeImport", "../script/pointcut/typeFilter.json")));
        assertTrue(target.isHandle(null, new SetElementSelected().toStep().build(), new InputData().add("excludeImport", "../script/pointcut/typeFilter.json")));
        assertFalse(target.isHandle(null, new ClickElement().toStep().build(), new InputData().add("excludeImport", "../script/pointcut/typeFilter.json")));
    }

    public void testWhereNotBlank() {
        final ImportFilter target = new ImportFilter("typeFilter.json", "../script/pointcut", new PointcutLoader.ImportFunction(new PointcutLoader(new ImportLoader())
                , new File(baseDir).getAbsoluteFile()));
        assertTrue(target.isHandle(null, new SetElementText().toStep().build(), new InputData()));
        assertTrue(target.isHandle(null, new SelectElementValue().toStep().build(), new InputData()));
        assertTrue(target.isHandle(null, new SetElementSelected().toStep().build(), new InputData()));
        assertFalse(target.isHandle(null, new ClickElement().toStep().build(), new InputData()));
    }

}