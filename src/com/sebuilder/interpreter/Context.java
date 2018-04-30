package com.sebuilder.interpreter;

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
    private String screenShotDirectory = "screenshot";

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
    public String getScreenShotDirectory() {
        return screenShotDirectory;
    }

    /**
     *
     * @param screenShotOutput
     */
    public void setScreenShotOutput(String screenShotOutput) {
        this.screenShotDirectory = screenShotOutput;
    }
}
