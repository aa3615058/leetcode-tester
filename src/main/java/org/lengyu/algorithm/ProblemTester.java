package org.lengyu.algorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.Logger;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;

public class ProblemTester {
    private SolutionFactory fac;
    private TestCaseParser parser;
    private boolean plusMode;
    public static final long DEFAULT_TIME_OUT = 8;
    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
    private long timeout = DEFAULT_TIME_OUT;
    private TimeUnit timeUnit = DEFAULT_TIME_UNIT;

    private Logger dataLogger;
    private Logger infoLogger;
    private Logger errLogger;

    public ProblemTester() {
        this(new SolutionFactory());
    }

    public ProblemTester(String classNamePattern, String testCasesFilePattern) {
        this(new SolutionFactory(classNamePattern, testCasesFilePattern));
    }

    public ProblemTester(SolutionFactory fac) {
        this.fac = fac;
        this.plusMode = false;
        this.dataLogger = LogUtil.DATA_CONSOLE_LOGGER;
        this.infoLogger = LogUtil.INFO_CONSOLE_LOGGER;
        this.errLogger = LogUtil.ERR_CONSOLE_LOGGER;
        this.timeout = DEFAULT_TIME_OUT;
        this.timeUnit = DEFAULT_TIME_UNIT;
        this.parser = TestCaseParser.getInstance();
    }

    public SolutionFactory getSolutionFactory() {
        return fac;
    }

    public void setSolutionFactory(SolutionFactory fac) {
        this.fac = fac;
    }

    public long getTimeout() {
        return timeout;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeout(long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    public boolean isPlusMode() {
        return plusMode;
    }

    public void openPlusMode() {
        this.plusMode = true;
    }

    public void closePlusMode() {
        this.plusMode = false;
    }

    @Override
    public String toString() {
        return "["+fac+timeout+timeUnit+"]";
    }

    public void test(String id) {
        test(fac.getSolution(id),fac.getFile(id));
    }

    public void test(String className, String testCasesFileName) {
        try{
            test(Class.forName(className),new File(testCasesFileName));
        } catch (IllegalArgumentException | ClassNotFoundException e) {
            errLogger.error(LogUtil.INITIAL_ERR_LOG,e);
        }
    }

    public void test(String className, String methodName, String testCasesFileName) {
        try {
            File testFile = new File(testCasesFileName);
            Class<?> classA = Class.forName(className);
            Object objectA = classA.getConstructors()[0].newInstance();
            Method[] methods = classA.getMethods();
            Method methodA = null;
            for(Method m : methods) {
                if(m.getName().equals(methodName)) {
                    methodA = m;
                }
            }
            if(methodA != null) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Scanner sc = new Scanner(testFile);
                testMethod(methodA, sc, objectA, executor);
                sc.close();
                executor.shutdown();
            }else {
                errLogger.error("There is no method " + methodName + " in class " + className);
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | SecurityException | ClassNotFoundException | FileNotFoundException e) {
            errLogger.error(LogUtil.INITIAL_ERR_LOG,e);
        }
    }

    public void test(Object solution, String testCasesFileName) {
        test(solution,new File(testCasesFileName));
    }

    public void test(Object solution, File testFile) {
        try{
        Class<?> classA = solution.getClass();
        Method[] methods = classA.getDeclaredMethods();
        Class<?>[] innerClasses = classA.getClasses();
        
        //leetcode测试模式
        //mod=0: 一个Solution类中有且仅有一个public方法。输入测试用例是该方法的参数。
        //mod=1: 一个Solution类中有一个public内部类。输入测试用例是调用该类方法的名称，以及参数。
        int mod = innerClasses.length;
        
        if(mod == 0) {
            int methodsLen = methods.length;
            for(int i = 0; i < methods.length; i++) {
                if((methods[i].getModifiers()&1)!=1) {
                    methods[i] = null;
                    methodsLen--;
                }
            }
            if(methodsLen == 0) {
                errLogger.error("In Class "+ classA.getName() +" there is None public method OR inner class.");
                return;
            }else if(methodsLen > 1) {
                //多于一个public方法，按注解筛选
                int methodsLen0 = methodsLen;
                for(int i = 0, j = 0; i < methods.length && j < methodsLen0; i++) {
                    if(methods[i] != null) {
                        j++;
                        if(!methods[i].isAnnotationPresent(Answer.class)) {
                            methods[i] = null;
                            methodsLen--;
                        }
                    } 
                }
                if(methodsLen == 0) {
                    errLogger.error("In Class " + classA.getName() + " there are "+ methodsLen0 +" public methods. But no one annoted by " + "@Answer" + ".");
                    return;
                }
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            for(int i = 0, j = 0; i < methods.length && j < methodsLen; i++) {
                if(methods[i] == null) continue;
                else j++;

                Method methodA = methods[i];
                Scanner sc = new Scanner(testFile);
                testMethod(methodA, sc, solution, executor);
                sc.close();
            }
            executor.shutdown();
        }
        else if(mod > 0) {
            //粗略实现，无法应对如下情况
            //1.方法存在重载
            //2.构造函数存在重载

            //多于一个public内部类，按注解筛选
            int innerClassesLen = mod;
            if(mod > 1) {
                for(int i = 0; i < innerClasses.length; i++) {
                    if(!innerClasses[i].isAnnotationPresent(Answer.class)) {
                        innerClasses[i] = null;
                        innerClassesLen--;
                    }
                }
                if(innerClassesLen == 0) {
                    errLogger.error("In Class " + classA.getName() + " there are "+ mod +" public inner classes. But no one annoted by " + "@Answer" + ".");
                    return;                    
                }
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            for(Class<?> classB : innerClasses) {
                if(classB == null) continue;
                if(plusMode) {
                    infoLogger.info(classB.getName());
                }
                
                Scanner sc = new Scanner(testFile);
                testInnerClass(classB, sc, solution, executor);
                sc.close();
            }
            executor.shutdown();
        }
        }catch(FileNotFoundException e) {
            errLogger.error(LogUtil.INITIAL_ERR_LOG,e);
        }
    }

    public void test(Class<?> clazz, String testCasesFileName) {   
        try {
            test(clazz.getConstructors()[0].newInstance(),new File(testCasesFileName));
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | SecurityException e) {
            errLogger.error(LogUtil.INITIAL_ERR_LOG,e);
        }

    }

    public void test(Class<?> clazz, File testFile) {
        try {
            test(clazz.getConstructors()[0].newInstance(),testFile);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | SecurityException e) {
            errLogger.error(LogUtil.INITIAL_ERR_LOG,e);
        }
    }

    private void testMethod(Method methodA, Scanner sc, Object objectA, ExecutorService executor) {
        Parameter[] paraMeters = methodA.getParameters();
        Type[] parasTypes = getMethodParaTypes(methodA);
        Object[] paras = new Object[parasTypes.length];
        long totalTime = 0;
        if(plusMode) {
            infoLogger.info(methodA.toGenericString());
        }
        try {
            while(sc.hasNextLine()) {
                for(int k = 0; k < parasTypes.length; k++) {
                    String line = sc.nextLine();
                    if(line!=null && line.length()>0) {
                        paras[k] = parser.parsePara(line,parasTypes[k]);
                    }
                }
                Future<Long> future = executor.submit(()->{
                    long time = 0;
                    long anchorA = System.nanoTime();
                    Object result = methodA.invoke(objectA, paras);
                    long anchorB = System.nanoTime();
                    time += anchorB - anchorA;
                    dataLogger.info(resultToString(result));
                    return time;
                });

                //运行超时
                totalTime += future.get(timeUnit.toNanos(timeout)-totalTime, TimeUnit.NANOSECONDS);
            }
            if(plusMode) {
                infoLogger.info("time: " + totalTime/1000000.0d + " ms" + "\n");
            }
        } catch(TimeoutException e) {
            errLogger.error(LogUtil.TIMEOUT_ERR_LOG + " " + methodA);
        } catch(Exception e) {
            Throwable e0 = e;
            if(e0.getClass() == java.util.concurrent.ExecutionException.class) e0 = e0.getCause();
            if(e0.getClass() == InvocationTargetException.class) e0 = e0.getCause();
            errLogger.error(LogUtil.LAST_TESTCASES_ERR_LOG + TestCasesToString(paras), e0);
        }
    }

    private void testInnerClass(Class<?> clazz, Scanner sc, Object objectA, ExecutorService executor) {
        long totalTime = 0;
        try{
            HashMap<String, Method> methodMap = new HashMap<>();
            for(Method m : clazz.getMethods()) {
                methodMap.put(m.getName(), m);
            }
            while(sc.hasNext()) {
                Future<Long> future = executor.submit(()->{
                    JsonArray jCommands = Json.parse(sc.nextLine()).asArray();
                    JsonArray jParasList = Json.parse(sc.nextLine()).asArray();
                    try{
                    long time = 0L;
                    Constructor<?> constructor = clazz.getConstructors()[0];
                    Object objectB = null;
                    Object[] results = new Object[jCommands.size()];

                    for(int i = 0; i < jCommands.size(); i++) {
                        String command = jCommands.get(i).asString();
                        if(clazz.getSimpleName().startsWith(command)) {
                            Type[] parasTypes = getMethodParaTypes(constructor);
                            Object[] paras = new Object[parasTypes.length-1];
                            JsonArray jParas = jParasList.get(i).asArray();
                            for(int j = 0; j < paras.length; j++) {
                                paras[j] = parser.parsePara(jParas.get(j),parasTypes[j]);
                            }
                            long anchorA = System.nanoTime();
                            if(paras.length > 1) {
                                objectB = constructor.newInstance(objectA, paras);
                            }else objectB = constructor.newInstance(objectA);
                            long anchorB = System.nanoTime();
                            time += anchorB - anchorA;
                            continue;
                            //objectB = constructor.newInstance(objectA);
                        }
                        Method methodA = methodMap.get(command);
                        Object result = null;
                        if(methodA != null) {
                            Type[] parasTypes = getMethodParaTypes(methodA);
                            Object[] paras = new Object[parasTypes.length];
                            JsonArray jParas = jParasList.get(i).asArray();
                            for(int j = 0; j < parasTypes.length; j++) {
                                paras[j] = parser.parsePara(jParas.get(j),parasTypes[j]);
                            }
                            long anchorA = System.nanoTime();
                            if(paras.length > 0) result = methodA.invoke(objectB, paras);
                            else result = methodA.invoke(objectB);
                            long anchorB = System.nanoTime();
                            time += anchorB - anchorA;
                        }
                        results[i] = result;
                    }
                    dataLogger.info(resultToString(results));
                    return time;
                    }catch(Exception e) {
                        errLogger.error(LogUtil.LAST_TESTCASES_ERR_LOG + "\n" + jCommands + "\n" + jParasList);
                        throw e;
                    }
                    }
                );

                //运行超时
                totalTime += future.get(timeUnit.toNanos(timeout)-totalTime, TimeUnit.NANOSECONDS);
            }
            if(plusMode) {
                infoLogger.info("time: " + totalTime/1000000.0d + " ms" + "\n");
            }
        }catch(TimeoutException e) {
            errLogger.error(LogUtil.TIMEOUT_ERR_LOG + clazz.getName());
        }catch(Exception e) {
            Throwable e0 = e;
            if(e0.getClass() == java.util.concurrent.ExecutionException.class) e0 = e0.getCause();
            if(e0.getClass() == InvocationTargetException.class) e0 = e0.getCause();
            errLogger.error("",e0);
        }
    }

    private Type[] getMethodParaTypes(Executable method) {
        Parameter[] paraMeters = method.getParameters();
        Type[] parasType = new Type[paraMeters.length];
        for(int i = 0; i < parasType.length; i++) {
            parasType[i] = paraMeters[i].getParameterizedType();
        }
        return parasType;
    }

    private String resultToString(Object result) {
        String s;
        if(result == null) {
            s = "null";
        }else if(result.getClass().isArray()){
            Class<?> eClass = result.getClass().getComponentType();
            if(eClass.isArray()) s = Arrays.deepToString((Object[])result);
            else {
                if (eClass == byte.class)
                    s = Arrays.toString((byte[])result);
                else if (eClass == short.class)
                    s = Arrays.toString((short[])result);
                else if (eClass == int.class)
                    s = Arrays.toString((int[])result);
                else if (eClass == long.class)
                    s = Arrays.toString((long[])result);
                else if (eClass == char.class)
                    s = Arrays.toString((char[])result);
                else if (eClass == float.class)
                    s = Arrays.toString((float[])result);
                else if (eClass == double.class)
                    s = Arrays.toString((double[])result);
                else if (eClass == boolean.class)
                    s = Arrays.toString((boolean[])result);
                else
                    s = Arrays.toString((Object[])result);
            }
        }else s = result.toString();
        return s;
    }

    private String TestCasesToString(Object[] paras) {
        StringBuilder s0 = new StringBuilder();
        for(Object p: paras) {
            s0.append(resultToString(p));
        }
        return s0.toString();
    }
}