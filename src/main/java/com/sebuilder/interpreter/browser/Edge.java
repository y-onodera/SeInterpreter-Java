package com.sebuilder.interpreter.browser;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Map;

public class Edge extends Chrome {
    @Override
    public RemoteWebDriver make(Map<String, String> config) {
        ChromeOptions options = this.getOptions(config);
        options.setCapability("browserName", "MicrosoftEdge");
        EdgeOptions edgeOptions = new EdgeOptions();
        edgeOptions.setCapability("ms:edgeOptions", options.asMap().get("goog:chromeOptions"));
        return new EdgeDriver(edgeOptions);
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
