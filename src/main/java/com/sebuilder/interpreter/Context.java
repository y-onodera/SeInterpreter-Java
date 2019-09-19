package com.sebuilder.interpreter;

import com.google.common.collect.Lists;
import com.sebuilder.interpreter.step.type.SaveScreenshot;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public enum Context {

    INSTANCE;

    private final File baseDirectory = Paths.get(".").toAbsolutePath().normalize().toFile();
    private String browser = "Chrome";
    public final HashMap<String, String> getDriverConfig = new HashMap();
    private WebDriverFactory wdf;
    private Long implicitlyWaitTime = Long.valueOf(-1);
    private Long pageLoadWaitTime = Long.valueOf(-1);
    private DataSourceFactory dataSourceFactory;
    private String dataSourceDirectory = "input";
    private String dataSourceEncoding = "UTF-8";
    private String resultOutputDirectory = "result";
    private String downloadDirectory = "download";
    private String screenShotOutputDirectory = "screenshot";
    private String templateOutputDirectory = "template";
    private String defaultScript = "sebuilder";
    private Map<String, ScriptParser> scriptParsers = new HashMap<>();
    private StepTypeFactory stepTypeFactory;
    private Aspect aspect = new Aspect().builder()
            .interceptor()
            .addFailure(Lists.newArrayList(new SaveScreenshot().toStep().put("file", "failure.png").build()))
            .build()
            .build();

    public static Context getInstance() {
        return INSTANCE;
    }

    public static Long getImplicitlyWaitTime() {
        return getInstance().implicitlyWaitTime;
    }

    public static Long getPageLoadWaitTime() {
        return getInstance().pageLoadWaitTime;
    }

    public static String getBrowser() {
        return getInstance().browser;
    }

    public static Map<String, String> getDriverConfig() {
        return getInstance().getDriverConfig;
    }

    public static WebDriverFactory getWebDriverFactory() {
        return getInstance().wdf;
    }

    public static ScriptParser getScriptParser() {
        return getInstance().getScriptParser(getDefaultScript());
    }

    public static ScriptParser getScriptParser(String scriptType) {
        return getInstance().scriptParsers.get(scriptType);
    }

    public static StepTypeFactory getStepTypeFactory() {
        return getInstance().stepTypeFactory;
    }

    public static DataSourceFactory getDataSourceFactory() {
        return getInstance().dataSourceFactory;
    }

    public static String getDefaultScript() {
        return getInstance().defaultScript;
    }

    public static File getBaseDirectory() {
        return getInstance().baseDirectory;
    }

    public static File getDataSourceDirectory() {
        return new File(getInstance().dataSourceDirectory);
    }

    public static String getDataSourceEncoding() {
        return getInstance().dataSourceEncoding;
    }

    public static File getResultOutputDirectory() {
        return new File(getInstance().resultOutputDirectory);
    }

    public static String getDownloadDirectory() {
        return getInstance().downloadDirectory;
    }

    public static String getScreenShotOutputDirectory() {
        return getInstance().screenShotOutputDirectory;
    }

    public static String getTemplateOutputDirectory() {
        return getInstance().templateOutputDirectory;
    }

    public static Aspect getAspect() {
        return getInstance().aspect;
    }

    public Context setImplicitlyWaitTime(Long aLong) {
        this.implicitlyWaitTime = aLong;
        return this;
    }

    public Context setPageLoadWaitTime(Long aLong) {
        this.pageLoadWaitTime = aLong;
        return this;
    }

    public Context setBrowser(String browserName, String driverPath) {
        this.setBrowser(browserName);
        this.wdf.setDriverPath(driverPath);
        return this;
    }

    public Context setBrowser(String browser) {
        try {
            setWebDriverFactory((WebDriverFactory) Class.forName("com.sebuilder.interpreter.browser." + browser).getDeclaredConstructor().newInstance());
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            throw new AssertionError("Unknown WebDriverFactory: " + "com.sebuilder.interpreter.browser." + browser, e);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new AssertionError("Could not instantiate WebDriverFactory " + "com.sebuilder.interpreter.browser." + browser, e);
        }
        return this;
    }

    public Context setWebDriverFactory(WebDriverFactory webDriverFactory) {
        this.wdf = webDriverFactory;
        this.browser = webDriverFactory.targetBrowser();
        return this;
    }

    public Context addScriptParser(ScriptParser parser) {
        this.scriptParsers.put(parser.type(), parser);
        return this;
    }

    public Context setDefaultScriptParser(ScriptParser parser) {
        this.defaultScript = parser.type();
        return this.addScriptParser(parser);
    }

    public Context setStepTypeFactory(StepTypeFactory stepTypeFactory) {
        this.stepTypeFactory = stepTypeFactory;
        return this;
    }

    public Context setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
        return this;
    }

    public Context setDataSourceDirectory(String dataSourceDirectory) {
        this.dataSourceDirectory = dataSourceDirectory;
        return this;
    }

    public Context setDataSourceEncoding(String dataSourceEncoding) {
        this.dataSourceEncoding = dataSourceEncoding;
        return this;
    }

    public Context setResultOutputDirectory(String aResultOutputDirectory) {
        this.resultOutputDirectory = aResultOutputDirectory;
        return this;
    }

    public Context setDownloadDirectory(String downloadDirectory) {
        this.downloadDirectory = downloadDirectory;
        return this;
    }

    public Context setScreenShotOutputDirectory(String screenShotOutput) {
        this.screenShotOutputDirectory = screenShotOutput;
        return this;
    }

    public Context setTemplateOutputDirectory(String aTemplateOutputDirectory) {
        this.templateOutputDirectory = aTemplateOutputDirectory;
        return this;
    }

    public Context setAspect(String aspectFileName) throws IOException {
        this.aspect = this.getScriptParser().loadAspect(new File(this.getBaseDirectory(), aspectFileName));
        return this;
    }

    public Context setAspect(Aspect aAspect) {
        this.aspect = aAspect;
        return this;
    }
}
