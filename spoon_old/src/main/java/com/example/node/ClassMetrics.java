package com.example.node;

import java.util.ArrayList;
import java.util.List;

import spoon.reflect.declaration.CtClass;

public class ClassMetrics extends NodeMetrics{
    CtClass declaration;
    List<MethodMetrics> methodsMetrics;

    public ClassMetrics(CtClass declaration){
        this.declaration=declaration;
        this.methodsMetrics=new ArrayList<>();
    }
    public CtClass getDeclaration(){
        return declaration;
    }
    public void setDeclaration(CtClass declaration){
        this.declaration=declaration;
    }
    public List<MethodMetrics> getMethodsMetrics(){
        return methodsMetrics;
    }
    public void setMethodsMetrics(List<MethodMetrics> methodsMetrics){
        this.methodsMetrics=methodsMetrics;
    }

}
