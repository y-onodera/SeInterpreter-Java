package com.sebuilder.interpreter.browser;

import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Map;

public class Edge extends Chrome {
    @Override
    public RemoteWebDriver make(Map<String, String> config) {
        return new EdgeDriver(new EdgeOptions().merge(this.getOptions(config)));
    }

    @Override
    public void setDriverPath(String driverPath) {
        System.setProperty("webdriver.edge.driver", driverPath);
    }

    @Override
    public String getDriverPath() {
        return System.getProperty("webdriver.edge.driver");
    }

    @Override
    public String getDriverName() {
        return "msedgedriver.exe";
    }
}
