package com.sebuilder.interpreter;

import java.io.File;

public class Context {
    private static Context ourInstance = new Context();

    public static Context getInstance() {
        return ourInstance;
    }

    /**
     *
     */
    private String dataSourceEncording = "UTF-8";
    /**
     *
     */
    private String dataSourceDirectory = "input";
    /**
     *
     */
    private String screenShotOutputDirectory = "screenshot";
    /**
     *
     */
    private String resultOutputDirectory = "result";

    private Context() {
    }

    /**
     *
     * @return
     */
    public String getDataSourceEncording() {
        return dataSourceEncording;
    }

    /**
     *
     * @param dataSourceEncording
     */
    public void setDataSourceEncording(String dataSourceEncording) {
        this.dataSourceEncording = dataSourceEncording;
    }

    /**
     *
     * @return
     */
    public String getDataSourceDirectory() {
        return dataSourceDirectory;
    }

    /**
     *
     * @param dataSourceDirectory
     */
    public void setDataSourceDirectory(String dataSourceDirectory) {
        this.dataSourceDirectory = dataSourceDirectory;
    }

    /**
     *
     * @return
     */
    public File getScreenShotOutputDirectory() {
        return new File(this.getResultOutputDirectory(),screenShotOutputDirectory);
    }

    /**
     *
     * @param screenShotOutput
     */
    public void setScreenShotOutputDirectory(String screenShotOutput) {
        this.screenShotOutputDirectory = screenShotOutput;
    }

    public File getResultOutputDirectory() {
        return new File(resultOutputDirectory);
    }

    public void setResultOutputDirectory(String aResultOutputDirectory) {
        this.resultOutputDirectory = aResultOutputDirectory;
    }

}
