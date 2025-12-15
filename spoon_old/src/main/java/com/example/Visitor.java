package com.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.visitor.CtScanner;

public class Visitor extends CtScanner {

    List<String> nameOfClasses = new ArrayList<>();
    List<IAttribute> metricForMethod = new ArrayList<>();
    List<IAttribute> metricForClass = new ArrayList<>();
    Map<CtClass, ClassMetrics> classesMetrics = new IdentityHashMap<>();

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
        ClassMetrics classMetrics = new ClassMetrics(ctClass);
        IAttribute superClass = new SuperClass();
        superClass.calculate(classMetrics);
        Set<CtMethod> methods = classMetrics.getDeclaration().getMethods();
        for (CtMethod method : methods) {
            MethodMetrics methodMetrics = new MethodMetrics(method, classMetrics);
            for (IAttribute metric : metricForMethod) {
                metric.calculate(methodMetrics);
            }
            classMetrics.getMethodsMetrics().add(methodMetrics);
        }
        for (IAttribute metric : metricForClass) {
            metric.calculate(classMetrics);
        }
        classesMetrics.put(ctClass, classMetrics);
        super.visitCtClass(ctClass);
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
                pwMethod.println();

                pwClass.print("file");
                pwClass.print(",");
                pwClass.print("class");
                for (String metric : metricOfClass) {
                    pwClass.print(",");
                    pwClass.print(metric);
                }
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
        for (CtClass clazz : classesMetrics.keySet()) {
            ClassMetrics classMetrics = classesMetrics.get(clazz);
            for (MethodMetrics methodMetrics : classMetrics.getMethodsMetrics()) {
                pwMethod.print(clazz.getPosition().getFile().getPath());
                pwMethod.print(",");
                pwMethod.print(clazz.getQualifiedName());
                pwMethod.print(",");
                CtMethod method = methodMetrics.getDeclaration();
                List<String> parameters = new ArrayList<>();
                List<CtParameter> methodParameter = method.getParameters();
                for (CtParameter param : methodParameter) {
                    parameters.add(param.getType().getQualifiedName());
                }
                pwMethod.print("\"" + method.getSimpleName() + "/" + parameters.size() + parameters
                        + "\"");
                for (String metric : metricOfMethod) {
                    pwMethod.print(",");
                    pwMethod.print(methodMetrics.getMetric(metric));
                }
                pwMethod.println();
            }
            if (clazz.getPosition() != null) {
                SourcePosition position = clazz.getPosition();
                if (position.getFile() != null) {
                    pwClass.print(position.getFile().getPath());
                }
            }
            pwClass.print(",");
            pwClass.print(clazz.getQualifiedName());
            for (String metric : metricOfClass) {
                pwClass.print(",");
                pwClass.print(classMetrics.getMetric(metric));
            }
            pwClass.println();
        }
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
