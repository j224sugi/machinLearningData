package com.example.node;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

public class MethodMetrics extends NodeMetrics {

    private CtMethod declaration;
    private ClassMetrics classMetrics;

    public MethodMetrics(CtMethod declaration, ClassMetrics classMetrics) {
        this.declaration = declaration;
        this.classMetrics = classMetrics;
    }

    public CtMethod getDeclaration() {
        return declaration;
    }

    public void setDeclaration(CtMethod declaration) {
        this.declaration = declaration;
    }

    public CtClass getClassParent() {
        return classMetrics.getDeclaration();
    }

    public ClassMetrics getClassMetrics() {
        return classMetrics;
    }
}
