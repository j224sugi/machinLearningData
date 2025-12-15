package com.example.node;

import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtType;

public class MethodMetrics extends NodeMetrics {

    private CtExecutable declaration;
    private ClassMetrics classMetrics;

    public MethodMetrics(CtExecutable declaration, ClassMetrics classMetrics) {
        this.declaration = declaration;
        this.classMetrics = classMetrics;
    }

    public CtExecutable getDeclaration() {
        return declaration;
    }

    public void setDeclaration(CtExecutable declaration) {
        this.declaration = declaration;
    }

    public CtType getClassParent() {
        return classMetrics.getDeclaration();
    }

    public ClassMetrics getClassMetrics() {
        return classMetrics;
    }
}
