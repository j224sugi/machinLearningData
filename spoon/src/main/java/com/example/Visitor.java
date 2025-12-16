package com.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        classMetrics.getDeclaration().getMethods().forEach(a -> methods.add((CtExecutable) a));
        if (classMetrics.getDeclaration() instanceof CtClass clazz) {
            clazz.getConstructors().forEach(a -> methods.add((CtExecutable) a));
            clazz.getAnonymousExecutables().forEach(a -> methods.add((CtExecutable) a));
            //methods.add((CtExecutable)clazz.getAnonymousExecutables());
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

    public void printCSV(String arg, int flag) throws IOException {
        try {
            if (flag == 0) {
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
        String[] metricOfClass = {"NprotM", "BOvR", "ATFD", "ATLD", "LAA", "BUR", "FDP"};
        String[] metricOfMethod = {"ATFD", "ATLD", "LAA", "FDP"};
        for (CtType clazz : classesMetrics.keySet()) {
            if(clazz.isAnonymous()){
                System.out.println(clazz.getQualifiedName());
            }
            List<MethodMetrics> initializers = new ArrayList<>();
            ClassMetrics classMetrics = classesMetrics.get(clazz);
            for (MethodMetrics methodMetrics : classMetrics.getMethodsMetrics()) {
                CtExecutable method = methodMetrics.getDeclaration();
                String methodName = "";
                if ("<init>".equals(method.getSimpleName())) {
                    methodName = clazz.getSimpleName();
                    if (isDigits(methodName)) {
                        continue;
                    }
                } else if ("".equals(method.getSimpleName())) {
                    initializers.add(methodMetrics);
                    continue;
                } else {
                    methodName = method.getSimpleName();
                }
                pwMethod.print(clazz.getPosition().getFile().getPath());
                pwMethod.print(",");
                pwMethod.print(clazz.getQualifiedName());
                pwMethod.print(",");
                List<String> parameters = new ArrayList<>();
                List<CtParameter> methodParameter = method.getParameters();
                for (CtParameter param : methodParameter) {
                    parameters.add(param.getType().getQualifiedName());
                }
                if (!parameters.isEmpty()) {
                    pwMethod.print("\"" + methodName + "/" + parameters.size() + parameters + "\"");
                } else {
                    pwMethod.print("\"" + methodName + "/" + parameters.size() + "\"");
                }
                for (String metric : metricOfMethod) {
                    pwMethod.print(",");
                    pwMethod.print(methodMetrics.getMetric(metric));
                }
                pwMethod.print(",");
                SourcePosition pos = method.getPosition();
                if (pos != null && pos.isValidPosition()) {
                    pwMethod.print(pos.getLine());
                }
                pwMethod.println();
            }
            if (!initializers.isEmpty()) {
                Map<Integer, MethodMetrics> map = new TreeMap<>();
                for (MethodMetrics initialize : initializers) {
                    CtExecutable method = initialize.getDeclaration();
                    SourcePosition pos = method.getPosition();
                    if (pos != null && pos.isValidPosition()) {
                        map.put(method.getPosition().getLine(), initialize);
                    }
                }
                int i = 1;
                for (Integer line : map.keySet()) {
                    MethodMetrics methodMetrics = map.get(line);
                    pwMethod.print(clazz.getPosition().getFile().getPath());
                    pwMethod.print(",");
                    pwMethod.print(clazz.getQualifiedName());
                    pwMethod.print(",");
                    pwMethod.print("(initializer " + i + ")");
                    i = i + 1;
                    for (String metric : metricOfMethod) {
                        pwMethod.print(",");
                        pwMethod.print(methodMetrics.getMetric(metric));
                    }
                    pwMethod.print(",");
                    pwMethod.print(line);
                    pwMethod.println();
                }
            }
            int classLine = 0;
            if (clazz.getPosition() != null) {
                SourcePosition position = clazz.getPosition();
                if (position.getFile() != null && position.isValidPosition()) {
                    classLine = position.getLine();
                    pwClass.print(position.getFile().getPath());
                }
            }
            pwClass.print(",");
            pwClass.print(clazz.getQualifiedName());
            for (String metric : metricOfClass) {
                pwClass.print(",");
                pwClass.print(classMetrics.getMetric(metric));
            }
            pwClass.print(",");
            pwClass.print(classLine);
            pwClass.println();
        }
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

/*
 * public void excuteMetrics() { for (CtClass clazz : classesMetrics.keySet()) { //if
 * (Paths.get(clazz.getPosition().getFile().getAbsolutePath()).equals(Paths.get(
 * "C:/Users/syuuj/HikariCP/src/main/java/com/zaxxer/hikari/util/ConcurrentBag.java"))) {
 * ClassMetrics classMetrics = classesMetrics.get(clazz); IAttribute superClass = new SuperClass();
 * superClass.calculate(classMetrics); Set<CtMethod> methods = clazz.getMethods(); for (CtMethod
 * method : methods) { MethodMetrics methodMetrics = new MethodMetrics(method, classMetrics); for
 * (IAttribute metric : metricForMethod) { metric.calculate(methodMetrics); }
 * classMetrics.getMethodsMetrics().add(methodMetrics); } for (IAttribute metric : metricForClass) {
 * metric.calculate(classMetrics); } //} } }
 */
