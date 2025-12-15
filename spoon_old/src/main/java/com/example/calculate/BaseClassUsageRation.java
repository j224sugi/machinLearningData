package com.example.calculate;

import java.util.ArrayList;
import java.util.List;

import com.example.node.ClassMetrics;
import com.example.node.MethodMetrics;

import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;

public class BaseClassUsageRation implements IAttribute {

    private List<CtMethod> ListOfUseProtectMethod = new ArrayList<>();
    private List<CtField> ListOfUseProtectField = new ArrayList<>();
    private List<CtMethod> NProtMofMethod = new ArrayList<>();

    @Override
    public String getName() {
        return "BUR";
    }

    @Override
    public void calculate(MethodMetrics node) {
        //do not anything
    }

    @Override
    public void calculate(ClassMetrics node) {
        ListOfUseProtectMethod.clear();
        ListOfUseProtectField.clear();
        NProtMofMethod=new ArrayList<>();
        if (node.getMetric("NprotM") == null || node.getMetric("NprotM") <= 0) {//NprotMが計算できていないもしくは、protectedがない場合
            node.setMetric(getName(), -1);
        } else {
            NProtMofMethod=(List<CtMethod>)node.getAttribute("A");
            List<MethodMetrics> methodsMetrics = node.getMethodsMetrics();
            for (MethodMetrics methodMetrics : methodsMetrics) {
                List<CtInvocation> LocalInvocations = (List<CtInvocation>) methodMetrics.getAttribute("ListOfLocalInvocation");
                List<CtFieldAccess> LocalFieldAccess = (List<CtFieldAccess>) methodMetrics.getAttribute("ListOfLocalField");
                for (CtInvocation invocation : LocalInvocations) {
                    addFirstUseProtectedMethod(invocation);
                }
                for (CtFieldAccess fieldAccess : LocalFieldAccess) {
                    addFirstUseProtectedField(fieldAccess);
                }
            }
            float NprotM = node.getMetric("NprotM");
            float ration = (ListOfUseProtectField.size() + ListOfUseProtectMethod.size()) / NprotM;
            node.setMetric(getName(), ration);
        }

    }

    public void addFirstUseProtectedMethod(CtInvocation invocation) {
        if (invocation.getExecutable() != null) {
            CtExecutable executable = invocation.getExecutable().getExecutableDeclaration();
            if (executable != null && executable instanceof CtMethod methodDeclaration) {
                if (methodDeclaration.isProtected() && !ListOfUseProtectMethod.contains(methodDeclaration) && NProtMofMethod.contains(methodDeclaration)) {
                    ListOfUseProtectMethod.add(methodDeclaration);
                }
            }
        }
    }

    public void addFirstUseProtectedField(CtFieldAccess fieldAccess) {
        if (fieldAccess.getVariable() != null) {
            CtField fieldDeclaration = fieldAccess.getVariable().getDeclaration();
            if (fieldDeclaration != null && fieldDeclaration.isProtected()) {
                if (!ListOfUseProtectField.contains(fieldDeclaration)) {
                    ListOfUseProtectField.add(fieldDeclaration);
                }
            }
        }
    }
}
