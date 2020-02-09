package com.sebuilder.interpreter.application;

import com.sebuilder.interpreter.Context;
import org.junit.Test;

import java.io.File;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CommandLineOptionTest {

    private CommandLineOption target = new CommandLineOption();

    @Test
    public void parse() throws Exception {
        target.parse(new String[]{
                CommandLineArgument.IMPLICITLY_WAIT.getArgument("1000"),
                CommandLineArgument.PAGE_LOAD_TIMEOUT.getArgument("5000"),
                CommandLineArgument.DRIVER.getArgument("Edge"),
                CommandLineArgument.DRIVER_PATH.getArgument("C:/driver/chromedriver.exe"),
                CommandLineArgument.DRIVER_CONFIG_PREFIX.key() + "experimental=extension",
                CommandLineArgument.DRIVER_CONFIG_PREFIX.key() + "binary=C:/binary/chrome.exe",
                CommandLineArgument.DATASOURCE_ENCODING.getArgument("ascii"),
                CommandLineArgument.DATASOURCE_DIRECTORY.getArgument("datasource"),
                CommandLineArgument.SCREENSHOT_OUTPUT.getArgument("saveImage"),
                CommandLineArgument.TEMPLATE_OUTPUT.getArgument("reverse"),
                CommandLineArgument.RESULT_OUTPUT.getArgument("report"),
                CommandLineArgument.JUNIT_REPORT_PREFIX.getArgument("timestamp"),
                CommandLineArgument.DOWNLOAD_OUTPUT.getArgument("file"),
                CommandLineArgument.ASPECT.getArgument("test.json"),
                CommandLineArgument.ENVIRONMENT_PROPERTIES.getArgument("some_environment.properties"),
                CommandLineArgument.ENVIRONMENT_PROPERTIES_PREFIX.key() + "locale=dn",
                CommandLineArgument.LOCALE.getArgument("ja"),
                CommandLineArgument.LOCALE_CONF.getArgument("locale_text.properties"),
                "test.script", "test2.script"
        });
        assertEquals(Long.valueOf(1000), target.getImplicitlyWait());
        assertEquals(Long.valueOf(5000), target.getPageLoadTimeout());
        assertEquals("Edge", target.getDriver());
        assertEquals("C:/driver/chromedriver.exe", target.getDriverPath());
        assertEquals("extension", target.getDriverConfig("experimental"));
        assertEquals("C:/binary/chrome.exe", target.getDriverConfig("binary"));
        assertEquals("ascii", target.getDatasourceEncoding());
        assertEquals("datasource", target.getDatasourceDirectory());
        assertEquals("saveImage", target.getScreenshotoutput());
        assertEquals("reverse", target.getTemplateoutput());
        assertEquals("report", target.getResultoutput());
        assertEquals(Context.TestNamePrefix.TIMESTAMP, target.getJunitReportPrefix());
        assertEquals("file", target.getDownloadoutput());
        assertEquals("test.json", target.getAspectFile());
        assertEquals("some_environment.properties", target.getEnvironmentProperties());
        assertEquals("dn", target.getEnvVar("locale"));
        assertEquals(Locale.JAPANESE, target.getLocale());
        assertEquals(new File("locale_text.properties"), target.getLocaleConf());
        assertEquals(Set.of("test.script", "test2.script"), target.getScripts());
    }

    @Test
    public void parse_fromTextFile() throws Exception {

        target.parse(new String[]{
                "@" + this.getClass().getResource("testparam.text").getFile()
        });
        assertEquals(Long.valueOf(1000), target.getImplicitlyWait());
        assertEquals(Long.valueOf(5000), target.getPageLoadTimeout());
        assertEquals("Edge", target.getDriver());
        assertEquals("C:/driver/chromedriver.exe", target.getDriverPath());
        assertEquals("extension", target.getDriverConfig("experimental"));
        assertEquals("C:/binary/chrome.exe", target.getDriverConfig("binary"));
        assertEquals("ascii", target.getDatasourceEncoding());
        assertEquals("datasource", target.getDatasourceDirectory());
        assertEquals("saveImage", target.getScreenshotoutput());
        assertEquals("reverse", target.getTemplateoutput());
        assertEquals("report", target.getResultoutput());
        assertEquals(Context.TestNamePrefix.NONE, target.getJunitReportPrefix());
        assertEquals("file", target.getDownloadoutput());
        assertEquals("test.json", target.getAspectFile());
        assertEquals("some_environment.properties", target.getEnvironmentProperties());
        assertEquals("dn", target.getEnvVar("locale"));
        assertEquals(Locale.JAPANESE, target.getLocale());
        assertEquals(new File("locale_text.properties"), target.getLocaleConf());
        assertEquals(Set.of("test.script", "test2.script"), target.getScripts());
    }

    @Test
    public void parse_overrideTextFile() throws Exception {

        target.parse(new String[]{
                "@" + this.getClass().getResource("testparam.text").getFile(),
                CommandLineArgument.IMPLICITLY_WAIT.getArgument("1002"),
                CommandLineArgument.PAGE_LOAD_TIMEOUT.getArgument("5002"),
                CommandLineArgument.DRIVER.getArgument("Chrome2"),
                CommandLineArgument.DRIVER_PATH.getArgument("C:/driver/chromedriver2.exe"),
                CommandLineArgument.DRIVER_CONFIG_PREFIX.key() + "experimental=extension2",
                CommandLineArgument.DRIVER_CONFIG_PREFIX.key() + "binary=C:/binary/chrome2.exe",
                CommandLineArgument.DATASOURCE_ENCODING.getArgument("MS932"),
                CommandLineArgument.DATASOURCE_DIRECTORY.getArgument("input2"),
                CommandLineArgument.SCREENSHOT_OUTPUT.getArgument("saveImage2"),
                CommandLineArgument.TEMPLATE_OUTPUT.getArgument("reverse2"),
                CommandLineArgument.RESULT_OUTPUT.getArgument("report2"),
                CommandLineArgument.JUNIT_REPORT_PREFIX.getArgument("resultDir"),
                CommandLineArgument.DOWNLOAD_OUTPUT.getArgument("file2"),
                CommandLineArgument.ASPECT.getArgument("test2.json"),
                CommandLineArgument.ENVIRONMENT_PROPERTIES.getArgument("some_environment2.properties"),
                CommandLineArgument.ENVIRONMENT_PROPERTIES_PREFIX.key() + "locale=us",
                CommandLineArgument.LOCALE.getArgument("ja-JP"),
                CommandLineArgument.LOCALE_CONF.getArgument("locale_text2.properties"),
                "test3.script", "test4.script"
        });
        assertEquals(Long.valueOf(1002), target.getImplicitlyWait());
        assertEquals(Long.valueOf(5002), target.getPageLoadTimeout());
        assertEquals("Chrome2", target.getDriver());
        assertEquals("C:/driver/chromedriver2.exe", target.getDriverPath());
        assertEquals("extension2", target.getDriverConfig("experimental"));
        assertEquals("C:/binary/chrome2.exe", target.getDriverConfig("binary"));
        assertEquals("MS932", target.getDatasourceEncoding());
        assertEquals("input2", target.getDatasourceDirectory());
        assertEquals("saveImage2", target.getScreenshotoutput());
        assertEquals("reverse2", target.getTemplateoutput());
        assertEquals("report2", target.getResultoutput());
        assertEquals(Context.TestNamePrefix.RESULT_DIR, target.getJunitReportPrefix());
        assertEquals("file2", target.getDownloadoutput());
        assertEquals("test2.json", target.getAspectFile());
        assertEquals("some_environment2.properties", target.getEnvironmentProperties());
        assertEquals("us", target.getEnvVar("locale"));
        assertEquals(Locale.JAPAN, target.getLocale());
        assertEquals(new File("locale_text2.properties"), target.getLocaleConf());
        assertEquals(Set.of("test.script", "test2.script", "test3.script", "test4.script"), target.getScripts());
    }

    @Test
    public void parse_default() throws Exception {
        target.parse(new String[]{});
        assertEquals(Long.valueOf(-1), target.getImplicitlyWait());
        assertEquals(Long.valueOf(-1), target.getPageLoadTimeout());
        assertEquals("Chrome", target.getDriver());
        assertNull(target.getDriverPath());
        assertEquals(0, target.getDriverConfig().size());
        assertEquals("UTF-8", target.getDatasourceEncoding());
        assertEquals("input", target.getDatasourceDirectory());
        assertEquals("screenshot", target.getScreenshotoutput());
        assertEquals("template", target.getTemplateoutput());
        assertEquals("result", target.getResultoutput());
        assertEquals("download", target.getDownloadoutput());
        assertNull(target.getAspectFile());
        assertNull(target.getEnvironmentProperties());
        assertEquals(0, target.getEnvVar().size());
        assertNull(target.getLocale());
        assertNull(target.getLocaleConf());
        assertEquals(0, target.getScripts().size());
    }

}