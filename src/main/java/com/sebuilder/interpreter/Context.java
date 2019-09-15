package com.sebuilder.interpreter;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public enum Context {

    INSTANCE;

    private final File baseDirectory = Paths.get(".").toAbsolutePath().normalize().toFile();
    private Map<String, ScriptParser> scriptParsers = new HashMap<>();
    private StepTypeFactory stepTypeFactory;
    private DataSourceFactory dataSourceFactory;
    private String dataSourceDirectory = "input";
    private String dataSourceEncoding = "UTF-8";
    private String resultOutputDirectory = "result";
    private String downloadDirectory = "download";
    private String screenShotOutputDirectory = "screenshot";
    private String templateOutputDirectory = "template";
    private String browser = "Chrome";
    private String defaultScript = "sebuilder";
    private Aspect aspect = new Aspect();

    public static Context getInstance() {
        return INSTANCE;
    }

    public static String getBrowser() {
        return getInstance().browser;
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

    public Context setBrowser(String browser) {
        this.browser = browser;
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

    public Context setAspect(Aspect aAspect) {
        this.aspect = aAspect;
        return this;
    }
}
