package com.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.example.calculate.AccessToData;
import com.example.calculate.BaseClassUsageRation;
import com.example.calculate.ForeignDataProvider;
import com.example.calculate.IAttribute;
import com.example.calculate.NumOvererideMethod;
import com.example.calculate.NumProtMembersInParent;
import com.example.calculate.SuperClass;
import com.example.node.ClassMetrics;
import com.example.node.MethodMetrics;

import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.CtScanner;

public class Visitor extends CtScanner {

    List<String> nameOfClasses = new ArrayList<>();
    List<IAttribute> metricForMethod = new ArrayList<>();
    List<IAttribute> metricForClass = new ArrayList<>();
    Map<CtType, ClassMetrics> classesMetrics = new IdentityHashMap<>();

    public Visitor() {
        metricForMethod.add(new AccessToData());
        metricForMethod.add(new ForeignDataProvider());

        metricForClass.add(new AccessToData());
        metricForClass.add(new NumProtMembersInParent(nameOfClasses));
        metricForClass.add(new NumOvererideMethod());
        metricForClass.add(new BaseClassUsageRation());
        metricForClass.add(new ForeignDataProvider());

    }

    @Override
    public <T extends Object> void visitCtClass(CtClass<T> ctClass) {
        doCalulateMetrics(ctClass);
        super.visitCtClass(ctClass);
    }

    @Override
    public <T> void visitCtInterface(CtInterface<T> intrface) {
        doCalulateMetrics(intrface);
        super.visitCtInterface(intrface);
    }

    @Override
    public <T extends Enum<?>> void visitCtEnum(CtEnum<T> ctEnum) {
        doCalulateMetrics(ctEnum);
        super.visitCtEnum(ctEnum);
    }

    public void doCalulateMetrics(CtType ctType) {
        ClassMetrics classMetrics = new ClassMetrics(ctType);
        IAttribute superClass = new SuperClass();
        superClass.calculate(classMetrics);
        List<CtExecutable<?>> methods = new ArrayList<>();
        ctType.getMethods().forEach(a -> methods.add((CtExecutable) a));
        if (ctType instanceof CtClass clazz) {
            clazz.getConstructors().forEach(a -> methods.add((CtExecutable) a));
            clazz.getAnonymousExecutables().forEach(a -> methods.add((CtExecutable) a));
        }
        for (CtExecutable method : methods) {
            MethodMetrics methodMetrics = new MethodMetrics(method, classMetrics);
            for (IAttribute metric : metricForMethod) {
                metric.calculate(methodMetrics);
            }
            classMetrics.getMethodsMetrics().add(methodMetrics);
        }
        for (IAttribute metric : metricForClass) {
            metric.calculate(classMetrics);
        }
        classesMetrics.put(ctType, classMetrics);

    }

    public void printCSV(String arg, int isFirst) throws IOException {
        try {
            if (isFirst == 0) {
                FileWriter fwClass = new FileWriter(arg + "spoonClass.csv", false);
                PrintWriter pwClass = new PrintWriter(new BufferedWriter(fwClass));
                FileWriter fwMethod = new FileWriter(arg + "spoonMethod.csv", false);
                PrintWriter pwMethod = new PrintWriter(new BufferedWriter(fwMethod));

                String[] metricOfClass = {"NprotM", "BOvR", "ATFD", "ATLD", "LAA", "BUR", "FDP"};
                String[] metricOfMethod = {"ATFD", "ATLD", "LAA", "FDP"};

                pwMethod.print("file");
                pwMethod.print(",");
                pwMethod.print("class");
                pwMethod.print(",");
                pwMethod.print("method");
                for (String metric : metricOfMethod) {
                    pwMethod.print(",");
                    pwMethod.print(metric);
                }
                pwMethod.print(",");
                pwMethod.print("line");
                pwMethod.println();

                pwClass.print("file");
                pwClass.print(",");
                pwClass.print("class");
                for (String metric : metricOfClass) {
                    pwClass.print(",");
                    pwClass.print(metric);
                }
                pwClass.print(",");
                pwClass.print("line");
                pwClass.println();
                printMetrics(pwClass, pwMethod);
                pwClass.close();
                pwMethod.close();
            } else {
                FileWriter fwClass = new FileWriter(arg + "spoonClass.csv", true);
                PrintWriter pwClass = new PrintWriter(new BufferedWriter(fwClass));
                FileWriter fwMethod = new FileWriter(arg + "spoonMethod.csv", true);
                PrintWriter pwMethod = new PrintWriter(new BufferedWriter(fwMethod));
                printMetrics(pwClass, pwMethod);
                pwClass.close();
                pwMethod.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printMetrics(PrintWriter pwClass, PrintWriter pwMethod) {
        for (CtType clazz : classesMetrics.keySet()) {
            String fileName = getFileName(clazz);
            String className = getClassName(clazz);

            ClassMetrics classMetrics = classesMetrics.get(clazz);
            for (MethodMetrics methodMetrics : classMetrics.getMethodsMetrics()) {
                boolean isInitializer = false;
                CtExecutable method = methodMetrics.getDeclaration();
                String methodName = method.getSimpleName();
                if ("<init>".equals(methodName)) {
                    methodName = clazz.getSimpleName();
                    // if (isDigits(methodName)) {
                    // continue;
                    // }
                } else if ("".equals(methodName)) {
                    methodName = "(initializer " + getStartLine(method) + ")";
                    isInitializer = true;
                }

                printFileClass(pwMethod, fileName, className);
                pwMethod.print(",");
                if (!isInitializer) {
                    List<String> parameters = new ArrayList<>();
                    List<CtParameter> methodParameter = method.getParameters();
                    for (CtParameter param : methodParameter) {
                        parameters.add(param.getType().getQualifiedName());
                    }
                    if (!parameters.isEmpty()) {
                        pwMethod.print(
                                "\"" + methodName + "/" + parameters.size() + parameters + "\"");
                    } else {
                        pwMethod.print("\"" + methodName + "/" + parameters.size() + "\"");
                    }
                } else {
                    pwMethod.print(methodName);
                }

                printMethodMetrics(pwMethod, methodMetrics);
                pwMethod.print(",");
                pwMethod.print(getStartLine(method));
                pwMethod.println();
            }

            printFileClass(pwClass, fileName, className);
            printClassMetrics(pwClass, classMetrics);
            pwClass.print(",");
            pwClass.print(getStartLine(clazz));
            pwClass.println();
        }
    }

    public void printFileClass(PrintWriter pw, String file, String classname) {
        pw.print(file);
        pw.print(",");
        pw.print(classname);
    }

    public void printMethodMetrics(PrintWriter pwMethod, MethodMetrics methodMetrics) {
        String[] metricOfMethod = {"ATFD", "ATLD", "LAA", "FDP"};
        for (String metric : metricOfMethod) {
            pwMethod.print(",");
            pwMethod.print(methodMetrics.getMetric(metric));
        }
    }

    public void printClassMetrics(PrintWriter pwClass, ClassMetrics classMetrics) {
        String[] metricOfClass = {"NprotM", "BOvR", "ATFD", "ATLD", "LAA", "BUR", "FDP"};
        for (String metric : metricOfClass) {
            pwClass.print(",");
            pwClass.print(classMetrics.getMetric(metric));
        }
    }

    public String getFileName(CtType clazz) {
        if (clazz.getPosition() != null) {
            SourcePosition sourcePosition = clazz.getPosition();
            if (sourcePosition.getFile() != null && sourcePosition.isValidPosition()) {
                return sourcePosition.getFile().getPath();
            }
        }
        return "null";
    }

    public String getClassName(CtType clazz) {
        if (clazz.isAnonymous()) {
            CtType parent = clazz.getParent(CtType.class);
            if (parent == null || parent == clazz) {
                return "$Anonymous$" + getStartLine(clazz);
            }
            return getClassName(parent) + "$" + getStartLine(clazz);
        } else if (clazz.isTopLevel()) {
            return clazz.getQualifiedName();
        } else {
            CtType parent = clazz.getParent(CtType.class);
            if (parent == null || parent == clazz) {
                return "$innerClass$";
            }
            return getClassName(parent)+"$"+clazz.getSimpleName();
            
        }
    }

    public int getStartLine(CtElement element) {
        SourcePosition pos = element.getPosition();
        if (pos != null && pos.isValidPosition()) {
            return pos.getLine();
        }
        return 0;
    }

    public static boolean isDigits(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }

        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
