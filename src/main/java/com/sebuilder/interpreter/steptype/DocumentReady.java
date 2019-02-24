package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.Getter;
import com.sebuilder.interpreter.TestRun;

public class DocumentReady implements Getter {

    /**
     * @param ctx Current test run.
     * @return The value this getter gets, eg the page title.
     */
    @Override
    public String get(TestRun ctx) {
        return String.valueOf(ctx.driver().executeScript("return document.readyState").equals("complete"));
    }

    /**
     * @return The name of the parameter to compare the result of the get to, or null if the get
     * returns a boolean "true"/"false".
     */
    @Override
    public String cmpParamName() {
        return null;
    }
}