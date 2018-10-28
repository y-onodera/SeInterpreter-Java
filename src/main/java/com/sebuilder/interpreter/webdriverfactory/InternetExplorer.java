package com.sebuilder.interpreter.webdriverfactory;

import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.HashMap;

public class InternetExplorer implements WebDriverFactory {
    /**
     * @param config A key/value mapping of configuration options specific to this factory.
     * @return A RemoteWebDriver of the type produced by this factory.
     */
    @Override
    public RemoteWebDriver make(HashMap<String, String> config) {
        HashMap<String, String> caps = new HashMap<String, String>(config);
        DesiredCapabilities capabilities = new DesiredCapabilities(caps);
        return new InternetExplorerDriver(new InternetExplorerOptions(capabilities));
    }

    @Override
    public void setDriverPath(String driverPath) {
        System.setProperty("webdriver.ie.driver", driverPath);
    }
}
