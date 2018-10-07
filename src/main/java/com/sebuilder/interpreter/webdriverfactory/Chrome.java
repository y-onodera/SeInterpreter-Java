package com.sebuilder.interpreter.webdriverfactory;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.HashMap;

public class Chrome implements WebDriverFactory {

    /**
     * @param config A key/value mapping of configuration options specific to this factory.
     * @return A RemoteWebDriver of the type produced by this factory.
     */
    @Override
    public RemoteWebDriver make(HashMap<String, String> config) throws Exception {
        HashMap<String, String> caps = new HashMap<String, String>(config);
        DesiredCapabilities capabilities = new DesiredCapabilities(caps);
        ChromeOptions option = new ChromeOptions().merge(capabilities);
        ChromeDriver result = new ChromeDriver(option);
        return result;
    }

    @Override
    public void setDriverPath(String driverPath) {
        System.setProperty("webdriver.chrome.driver", driverPath);
    }
}
