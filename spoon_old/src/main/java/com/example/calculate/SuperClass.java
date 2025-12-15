package com.example.calculate;

import java.util.ArrayList;
import java.util.List;

import com.example.node.ClassMetrics;
import com.example.node.MethodMetrics;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtTypeReference;

public class SuperClass implements IAttribute {

    List<CtTypeReference> superClasses = new ArrayList<>();

    @Override
    public String getName() {
        return "superClasses";
    }

    @Override
    public void calculate(ClassMetrics node) {
        superClasses.clear();
        CtClass clazz = node.getDeclaration();
        CtTypeReference current = clazz.getSuperclass();
        while (current != null) {
            if (current.getDeclaration() == null) {
                break;
            }
            superClasses.add(current);
            current = current.getSuperclass();
        }
        node.setAttribute(getName(), superClasses);

    }

    @Override
    public void calculate(MethodMetrics node) {
        //do not anything
    }

}
