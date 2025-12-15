package com.example.node;

import java.util.ArrayList;
import java.util.List;

import spoon.reflect.declaration.CtType;

public class ClassMetrics extends NodeMetrics{
    CtType declaration;
    List<MethodMetrics> methodsMetrics;

    public ClassMetrics(CtType declaration){
        this.declaration=declaration;
        this.methodsMetrics=new ArrayList<>();
    }
    public CtType getDeclaration(){
        return declaration;
    }
    public void setDeclaration(CtType declaration){
        this.declaration=declaration;
    }
    public List<MethodMetrics> getMethodsMetrics(){
        return methodsMetrics;
    }
    public void setMethodsMetrics(List<MethodMetrics> methodsMetrics){
        this.methodsMetrics=methodsMetrics;
    }

}
