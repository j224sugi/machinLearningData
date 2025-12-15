package com.example.calculate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.example.node.ClassMetrics;
import com.example.node.MethodMetrics;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

public class NumProtMembersInParent implements IAttribute {

    List<String> nameOfClasses;
    List<CtMethod> ParentMethods;
    List<CtTypeReference> superClasses;
    List<CtFieldReference> ParentFields;
    List<CtMethod> ParentProtMethods;
    List<CtFieldReference> ParentProtFields;

    public NumProtMembersInParent(List<String> nameOfClasses) {
        this.nameOfClasses = nameOfClasses;
        this.ParentMethods = new ArrayList<>();
        this.ParentFields = new ArrayList<>();
        this.ParentProtMethods = new ArrayList<>();
        this.ParentProtFields = new ArrayList<>();
        this.superClasses = new ArrayList<>();

    }

    @Override
    public String getName() {
        return "NprotM";
    }

    @Override
    public void calculate(MethodMetrics node) {
        //do not do anything
    }

    @Override
    public void calculate(ClassMetrics node) {
        int protMembers = 0;
        ParentMethods.clear();
        ParentFields.clear();
        ParentProtMethods.clear();
        ParentProtFields.clear();
        superClasses.clear();
        superClasses = (List<CtTypeReference>) node.getAttribute("superClasses");
        if (!superClasses.isEmpty()) {
            for (CtTypeReference superClass : superClasses) {
                //if (nameOfClasses.contains(superClass.getQualifiedName())) {
                    ListProtMethods(superClass);
                    ListProtFields(superClass);
                //}
            }
            protMembers = ParentProtMethods.size() + ParentProtFields.size();
        }
        node.setMetric(getName(), protMembers);
        node.setAttribute("A",ParentProtMethods);
        node.setAttribute("superClassMethods", ParentMethods);

    }

    public void ListProtFields(CtTypeReference superClass) {
        if (superClass.getDeclaredFields() != null) {
            for (CtFieldReference field : superClass.getDeclaredFields()) {
                if (!ParentFields.contains(field)) {
                    if (field.getModifiers().contains(ModifierKind.PROTECTED)) {
                        ParentProtFields.add(field);
                    }

                }
            }
        }
    }

    public void ListProtMethods(CtTypeReference superClass) {
        if (!superClass.getDeclaration().getMethods().isEmpty()) {
            Set<CtMethod> methods = superClass.getDeclaration().getMethods();
            for (CtMethod method : methods) {
                if (!isOverriden(method, ParentMethods)) {
                    ParentMethods.add(method);
                    if (method.isProtected()) {
                        ParentProtMethods.add(method);
                    }
                }
            }
        }
    }

    public boolean isOverriden(CtMethod newMethod, List<CtMethod> lowLayerMethods) {
        for (CtMethod method : lowLayerMethods) {
            if (method.isOverriding(newMethod)) {
                return true;
            }
        }
        return false;
    }

}
