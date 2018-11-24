package com.sebuilder.interpreter.webdriverfactory;

import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.HashMap;

public class Edge implements WebDriverFactory {
    /**
     * @param config A key/value mapping of configuration options specific to this factory.
     * @return A RemoteWebDriver of the type produced by this factory.
     */
    @Override
    public RemoteWebDriver make(HashMap<String, String> config) throws Exception {
        HashMap<String, String> caps = new HashMap<String, String>(config);
        DesiredCapabilities capabilities = new DesiredCapabilities(caps);
        EdgeOptions options = new EdgeOptions().merge(capabilities);
        return new EdgeDriver(options);
    }

    @Override
    public void setDriverPath(String driverPath) {
        System.setProperty("webdriver.edge.driver", driverPath);
    }
}
