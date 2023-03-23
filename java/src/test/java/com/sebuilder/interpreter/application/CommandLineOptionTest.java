package com.sebuilder.interpreter.application;

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.report.ReportFormat;
import org.junit.Test;

import java.io.File;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CommandLineOptionTest {

    private CommandLineOption target = new CommandLineOption();

    @Test
    public void parse() throws Exception {
        this.target.parse(new String[]{
                CommandLineArgument.IMPLICITLY_WAIT.createArgument("1000"),
                CommandLineArgument.PAGE_LOAD_TIMEOUT.createArgument("5000"),
                CommandLineArgument.WAIT_FOR_MAX_MS.createArgument("60000"),
                CommandLineArgument.WAIT_FOR_INTERVAL_MS.createArgument("100"),
                CommandLineArgument.DRIVER.createArgument("Edge"),
                CommandLineArgument.DRIVER_PATH.createArgument("C:/driver/chromedriver.exe"),
                CommandLineArgument.DRIVER_CONFIG_PREFIX.key() + "experimental=extension",
                CommandLineArgument.DRIVER_CONFIG_PREFIX.key() + "binary=C:/binary/chrome.exe",
                CommandLineArgument.DATASOURCE_ENCODING.createArgument("ascii"),
                CommandLineArgument.DATASOURCE_DIRECTORY.createArgument("datasource"),
                CommandLineArgument.SCREENSHOT_OUTPUT.createArgument("saveImage"),
                CommandLineArgument.TEMPLATE_OUTPUT.createArgument("reverse"),
                CommandLineArgument.RESULT_OUTPUT.createArgument("report"),
                CommandLineArgument.JUNIT_REPORT_PREFIX.createArgument("timestamp"),
                CommandLineArgument.DOWNLOAD_OUTPUT.createArgument("file"),
                CommandLineArgument.ASPECT.createArgument("test.json"),
                CommandLineArgument.ENVIRONMENT_PROPERTIES.createArgument("some_environment.properties"),
                CommandLineArgument.ENVIRONMENT_PROPERTIES_PREFIX.key() + "locale=dn",
                CommandLineArgument.LOCALE.createArgument("ja"),
                CommandLineArgument.LOCALE_CONF.createArgument("locale_text.properties"),
                CommandLineArgument.REPORT_FORMAT.createArgument("ExtentReports"),
                "test.script", "test2.script"
        });
        assertEquals(Long.valueOf(1000), this.target.getImplicitlyWait());
        assertEquals(Long.valueOf(5000), this.target.getPageLoadTimeout());
        assertEquals(60000, this.target.getWaitForMaxMs());
        assertEquals(100, this.target.getWaitForIntervalMs());
        assertEquals("Edge", this.target.getDriver());
        assertEquals("C:/driver/chromedriver.exe", this.target.getDriverPath());
        assertEquals("extension", this.target.getDriverConfig("experimental"));
        assertEquals("C:/binary/chrome.exe", this.target.getDriverConfig("binary"));
        assertEquals("ascii", this.target.getDatasourceEncoding());
        assertEquals("datasource", this.target.getDatasourceDirectory());
        assertEquals("saveImage", this.target.getScreenshotoutput());
        assertEquals("reverse", this.target.getTemplateoutput());
        assertEquals("report", this.target.getResultoutput());
        assertEquals(Context.TestNamePrefix.TIMESTAMP, this.target.getJunitReportPrefix());
        assertEquals("file", this.target.getDownloadoutput());
        assertEquals("test.json", this.target.getAspectFile());
        assertEquals("some_environment.properties", this.target.getEnvironmentProperties());
        assertEquals("dn", this.target.getEnvVar("locale"));
        assertEquals(Locale.JAPANESE, this.target.getLocale());
        assertEquals(new File("locale_text.properties"), this.target.getLocaleConf());
        assertEquals(ReportFormat.EXTENT_REPORTS, this.target.getReportFormat());
        assertEquals(Set.of("test.script", "test2.script"), this.target.getScripts());
    }

    @Test
    public void parse_fromTextFile() throws Exception {

        this.target.parse(new String[]{
                "@" + Objects.requireNonNull(this.getClass().getResource("testparam.text")).getFile()
        });
        assertEquals(Long.valueOf(1000), this.target.getImplicitlyWait());
        assertEquals(Long.valueOf(5000), this.target.getPageLoadTimeout());
        assertEquals(60000, this.target.getWaitForMaxMs());
        assertEquals(100, this.target.getWaitForIntervalMs());
        assertEquals("Edge", this.target.getDriver());
        assertEquals("C:/driver/chromedriver.exe", this.target.getDriverPath());
        assertEquals("extension", this.target.getDriverConfig("experimental"));
        assertEquals("C:/binary/chrome.exe", this.target.getDriverConfig("binary"));
        assertEquals("ascii", this.target.getDatasourceEncoding());
        assertEquals("datasource", this.target.getDatasourceDirectory());
        assertEquals("saveImage", this.target.getScreenshotoutput());
        assertEquals("reverse", this.target.getTemplateoutput());
        assertEquals("report", this.target.getResultoutput());
        assertEquals(Context.TestNamePrefix.NONE, this.target.getJunitReportPrefix());
        assertEquals("file", this.target.getDownloadoutput());
        assertEquals("test.json", this.target.getAspectFile());
        assertEquals("some_environment.properties", this.target.getEnvironmentProperties());
        assertEquals("dn", this.target.getEnvVar("locale"));
        assertEquals(Locale.JAPANESE, this.target.getLocale());
        assertEquals(new File("locale_text.properties"), this.target.getLocaleConf());
        assertEquals(ReportFormat.EXTENT_REPORTS, this.target.getReportFormat());
        assertEquals(Set.of("test.script", "test2.script"), this.target.getScripts());
    }

    @Test
    public void parse_overrideTextFile() throws Exception {

        this.target.parse(new String[]{
                "@" + Objects.requireNonNull(this.getClass().getResource("testparam.text")).getFile(),
                CommandLineArgument.IMPLICITLY_WAIT.createArgument("1002"),
                CommandLineArgument.PAGE_LOAD_TIMEOUT.createArgument("5002"),
                CommandLineArgument.WAIT_FOR_MAX_MS.createArgument("60002"),
                CommandLineArgument.WAIT_FOR_INTERVAL_MS.createArgument("102"),
                CommandLineArgument.DRIVER.createArgument("Chrome2"),
                CommandLineArgument.DRIVER_PATH.createArgument("C:/driver/chromedriver2.exe"),
                CommandLineArgument.DRIVER_CONFIG_PREFIX.key() + "experimental=extension2",
                CommandLineArgument.DRIVER_CONFIG_PREFIX.key() + "binary=C:/binary/chrome2.exe",
                CommandLineArgument.DATASOURCE_ENCODING.createArgument("MS932"),
                CommandLineArgument.DATASOURCE_DIRECTORY.createArgument("input2"),
                CommandLineArgument.SCREENSHOT_OUTPUT.createArgument("saveImage2"),
                CommandLineArgument.TEMPLATE_OUTPUT.createArgument("reverse2"),
                CommandLineArgument.RESULT_OUTPUT.createArgument("report2"),
                CommandLineArgument.JUNIT_REPORT_PREFIX.createArgument("resultDir"),
                CommandLineArgument.DOWNLOAD_OUTPUT.createArgument("file2"),
                CommandLineArgument.ASPECT.createArgument("test2.json"),
                CommandLineArgument.ENVIRONMENT_PROPERTIES.createArgument("some_environment2.properties"),
                CommandLineArgument.ENVIRONMENT_PROPERTIES_PREFIX.key() + "locale=us",
                CommandLineArgument.LOCALE.createArgument("ja-JP"),
                CommandLineArgument.LOCALE_CONF.createArgument("locale_text2.properties"),
                CommandLineArgument.REPORT_FORMAT.createArgument("Junit"),
                "test3.script", "test4.script"
        });
        assertEquals(Long.valueOf(1002), this.target.getImplicitlyWait());
        assertEquals(Long.valueOf(5002), this.target.getPageLoadTimeout());
        assertEquals(60002, this.target.getWaitForMaxMs());
        assertEquals(102, this.target.getWaitForIntervalMs());
        assertEquals("Chrome2", this.target.getDriver());
        assertEquals("C:/driver/chromedriver2.exe", this.target.getDriverPath());
        assertEquals("extension2", this.target.getDriverConfig("experimental"));
        assertEquals("C:/binary/chrome2.exe", this.target.getDriverConfig("binary"));
        assertEquals("MS932", this.target.getDatasourceEncoding());
        assertEquals("input2", this.target.getDatasourceDirectory());
        assertEquals("saveImage2", this.target.getScreenshotoutput());
        assertEquals("reverse2", this.target.getTemplateoutput());
        assertEquals("report2", this.target.getResultoutput());
        assertEquals(Context.TestNamePrefix.RESULT_DIR, this.target.getJunitReportPrefix());
        assertEquals("file2", this.target.getDownloadoutput());
        assertEquals("test2.json", this.target.getAspectFile());
        assertEquals("some_environment2.properties", this.target.getEnvironmentProperties());
        assertEquals("us", this.target.getEnvVar("locale"));
        assertEquals(Locale.JAPAN, this.target.getLocale());
        assertEquals(new File("locale_text2.properties"), this.target.getLocaleConf());
        assertEquals(ReportFormat.JUNIT, this.target.getReportFormat());
        assertEquals(Set.of("test.script", "test2.script", "test3.script", "test4.script"), this.target.getScripts());
    }

    @Test
    public void parse_default() throws Exception {
        this.target.parse(new String[]{});
        assertEquals(Long.valueOf(-1), this.target.getImplicitlyWait());
        assertEquals(Long.valueOf(-1), this.target.getPageLoadTimeout());
        assertEquals("Chrome", this.target.getDriver());
        assertNull(this.target.getDriverPath());
        assertEquals(0, this.target.getDriverConfig().size());
        assertEquals("UTF-8", this.target.getDatasourceEncoding());
        assertEquals("input", this.target.getDatasourceDirectory());
        assertEquals("screenshot", this.target.getScreenshotoutput());
        assertEquals("template", this.target.getTemplateoutput());
        assertEquals("result", this.target.getResultoutput());
        assertEquals("download", this.target.getDownloadoutput());
        assertNull(this.target.getAspectFile());
        assertNull(this.target.getEnvironmentProperties());
        assertEquals(0, this.target.getEnvVar().size());
        assertNull(this.target.getLocale());
        assertNull(this.target.getLocaleConf());
        assertEquals(ReportFormat.JUNIT, this.target.getReportFormat());
        assertEquals(0, this.target.getScripts().size());
    }

}