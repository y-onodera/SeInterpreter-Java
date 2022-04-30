package com.sebuilder.interpreter.report;

import com.sebuilder.interpreter.TestRunListener;
import org.apache.logging.log4j.Logger;

import java.util.stream.Stream;

public enum ReportFormat implements TestRunListener.Factory {
    JUNIT("Junit"), EXTENT_REPORTS("ExtentReports");
    private final String name;

    ReportFormat(String name) {
        this.name = name;
    }

    public static ReportFormat fromName(String name) {
        return Stream.of(ReportFormat.values())
                .filter(it -> it.name.equals(name))
                .findFirst()
                .get();
    }

    @Override
    public TestRunListener create(Logger log) {
        if (this == EXTENT_REPORTS) {
            return new ExtentReportsTestRunListener(log);
        }
        return new JunitTestRunListener(log);
    }
}
