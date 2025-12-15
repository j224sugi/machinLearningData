package com.example;

import java.util.ArrayList;
import java.util.List;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.CtScanner;

public class ASTVisitor extends CtScanner {

    List<CtElement> allElement = new ArrayList<>();
    List<CtExecutableReference> methods = new ArrayList<>();

    @Override
    public <T extends Object> void visitCtInvocation(CtInvocation<T> invocation) {
        try {
            System.out.println(invocation.getExecutable().getSimpleName() + " : " + invocation.getExecutable().getDeclaringType().getSimpleName() + " " + invocation.getPosition().getLine());
        } catch (Exception e) {
            System.out.println("error : " + e.getMessage());
        }
    }
}
