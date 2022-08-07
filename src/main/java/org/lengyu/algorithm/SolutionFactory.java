package org.lengyu.algorithm;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;

public class SolutionFactory {
    private String classNamePattern;
    private String testFileNamePattern;
    private String idPlaceHolder = DEFAULT_ID_PLACE_HOLDER;
    public static final String DEFAULT_ID_PLACE_HOLDER = "{}";

    public SolutionFactory() {
        this.classNamePattern=DEFAULT_ID_PLACE_HOLDER;
        this.testFileNamePattern=DEFAULT_ID_PLACE_HOLDER;
    }

    public SolutionFactory(String classNamePattern, String testFileNamePattern) {
        this.classNamePattern = classNamePattern;
        this.testFileNamePattern = testFileNamePattern;
    }

    public String getIdPlaceHolder() {
        return idPlaceHolder;
    }

    public void setIdPlaceHolder(String idPlaceHolder) {
        this.idPlaceHolder = idPlaceHolder;
    }

    public String getClassNamePattern() {
        return classNamePattern;
    }

    public void setClassNamePattern(String classNamePattern) {
        this.classNamePattern = classNamePattern;
    }

    public String getTestFileNamePattern() {return testFileNamePattern;}

    public void setTestFileNamePattern(String testFileNamePattern) {this.testFileNamePattern = testFileNamePattern;}

    public String getClassName(String id) {
        return classNamePattern.replace(idPlaceHolder, id);
    }

    public String getFileName(String id) {
        return testFileNamePattern.replace(idPlaceHolder, id);
    }

    public Object getSolution(String id){
        Object p = null;
        try {
            p = Class.forName(getClassName(id)).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            LogUtil.ERR_CONSOLE_LOGGER.error(LogUtil.INITIAL_ERR_LOG, e);
        }
        return p;
    }

    public File getFile(String id) {
        return new File(getFileName(id));
    }

    @Override
    public String toString() {
        return "["+classNamePattern+","+testFileNamePattern+"]";
    }
}
