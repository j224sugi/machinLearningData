package com.example.calculate;

import java.util.ArrayList;
import java.util.List;

import com.example.node.ClassMetrics;
import com.example.node.MethodMetrics;

import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

public class AccessToData extends CtScanner implements IAttribute {

    private List<String> ListOfATFD = new ArrayList<>();
    private List<String> ListOfATLD = new ArrayList<>();
    private List<String> ListOfError = new ArrayList<>();
    private List<String> ListOfErrorATFD = new ArrayList<>();
    private String nameOfParentClass;
    private List<String> nameOfSuperClasses;
    private List<CtInvocation> ListOfLocalInvocation;
    private List<CtFieldAccess> ListOfLocalField;

    @Override
    public String getName() {
        return "ATFD";
    }

    @Override
    public void calculate(ClassMetrics node) {
        float sumOfATFD = 0;
        float sumOfATLD = 0;
        for (MethodMetrics methodMetrics : node.getMethodsMetrics()) {
            sumOfATFD = sumOfATFD + methodMetrics.getMetric(getName());
            sumOfATLD = sumOfATLD + methodMetrics.getMetric("ATLD");
        }
        node.setMetric(getName(), sumOfATFD);
        node.setMetric("ATLD", sumOfATLD);
        if (sumOfATFD > 0 || sumOfATLD > 0) {
            node.setMetric("LAA", sumOfATLD / (sumOfATFD + sumOfATLD));
        } else {
            node.setMetric("LAA", -1);
        }
    }

    @Override
    public void calculate(MethodMetrics node) {
        ListOfLocalField = new ArrayList<>();
        ListOfLocalInvocation = new ArrayList<>();
        ListOfError = new ArrayList<>();
        ListOfErrorATFD = new ArrayList<>();
        ListOfATLD = new ArrayList<>();
        ListOfATFD = new ArrayList<>();
        nameOfSuperClasses = new ArrayList<>();

        nameOfParentClass = node.getDeclaration().getParent(CtType.class).getQualifiedName();
        nameOfSuperClasses = ((List<CtTypeReference>) node.getClassMetrics().getAttribute("superClasses")).stream().map(a -> a.getQualifiedName()).toList();
        node.getDeclaration().accept(this);
        node.setAttribute("ListOfATFD", ListOfATFD);
        node.setAttribute("ListOfATLD", ListOfATLD);
        node.setAttribute("ListOfError", ListOfError);
        node.setAttribute("ListOfLocalInvocation", ListOfLocalInvocation);
        node.setAttribute("ListOfLocalField", ListOfLocalField);
        node.setMetric(getName(), ListOfATFD.size() + ListOfErrorATFD.size());
        node.setMetric("ATLD", ListOfATLD.size());
        float denominator = ListOfATFD.size() + ListOfATLD.size();
        if (denominator <= 0) {
            node.setMetric("LAA", -1);
        } else {
            node.setMetric("LAA", ListOfATLD.size() / denominator);
        }
    }

    @Override
    public <T extends Object> void visitCtInvocation(CtInvocation<T> invocation) {
        try {
            if (invocation.getExecutable().getDeclaringType() != null) {
                String nameOfClass = invocation.getExecutable().getDeclaringType().getQualifiedName();
                String nameOfMethod = invocation.getExecutable().getSimpleName();
                if (!nameOfClass.equals(nameOfParentClass)) {//自クラスでない場合
                    if (nameOfMethod.startsWith("get") || nameOfMethod.startsWith("set")) {
                        ListOfATFD.add(nameOfClass);
                    }
                    if (nameOfSuperClasses.contains(nameOfClass)) {
                        ListOfLocalInvocation.add(invocation);
                    }
                } else {
                    if (nameOfMethod.startsWith("get") || nameOfMethod.startsWith("set")) {
                        ListOfATLD.add(nameOfClass);
                    }
                    //ListOfLocalInvocation.add(invocation);　BURは親クラスのprotectedにおける使用数であるため，含めない，
                }
            } else {
                String methodName = invocation.getExecutable().getSimpleName();
                ListOfError.add(methodName + " error");
                if (methodName.startsWith("get") || methodName.startsWith("set")) {
                    ListOfErrorATFD.add(methodName);
                    ListOfATFD.add(methodName);
                }
                if (nameOfSuperClasses.contains(methodName)) {
                    ListOfLocalInvocation.add(invocation);
                }

            }
        } catch (Exception e) {
            ListOfError.add(invocation.getExecutable().getSimpleName() + " " + e.getMessage());
        }
        super.visitCtInvocation(invocation);
    }

    @Override
    public <T extends Object> void visitCtFieldRead(CtFieldRead<T> fieldRead) {
        if (isField(fieldRead)) {
            if (fieldRead.getVariable() != null && fieldRead.getVariable().getDeclaringType() != null) {
                String nameOfClass = fieldRead.getVariable().getDeclaringType().getQualifiedName();
                if (isForeignClass(nameOfClass)) {      //自クラス・親クラスではない場合
                    ListOfATFD.add(nameOfClass);
                } else {
                    ListOfATLD.add(nameOfClass);
                    if (!nameOfClass.equals(nameOfParentClass)) {
                        ListOfLocalField.add(fieldRead);
                    }
                }
            } else {
                ListOfATFD.add("null");
            }
        }
    }

    @Override
    public <T extends Object> void visitCtFieldWrite(spoon.reflect.code.CtFieldWrite<T> fieldWrite) {
        if (fieldWrite.getVariable() != null && fieldWrite.getVariable().getDeclaringType() != null) {
            String nameOfClass = fieldWrite.getVariable().getDeclaringType().getQualifiedName();
            if (isForeignClass(nameOfClass)) {
                ListOfATFD.add(nameOfClass);
            } else {
                ListOfATLD.add(nameOfClass);
                if (!nameOfClass.equals(nameOfParentClass)) {
                    ListOfLocalField.add(fieldWrite);
                }
            }
        } else {
            ListOfATFD.add("null");
        }
    }

    private boolean isForeignClass(String nameOfClass) {
        if (nameOfParentClass.equals(nameOfClass) || nameOfSuperClasses.contains(nameOfClass)) {
            return false;
        }
        return true;
    }

    public boolean isField(CtFieldRead fieldRead) {
        return !isInvocation(fieldRead) && isLastField(fieldRead);
    }

    public boolean isInvocation(CtFieldRead fieldRead) {        //instance.method()かどうか判別
        CtElement parent = fieldRead.getParent();
        if (parent != null && parent instanceof CtInvocation parentMethod) {
            if (parentMethod.getTarget() != null && parentMethod.getTarget() instanceof CtFieldRead target) {
                if (target.getVariable().getQualifiedName().equals(fieldRead.getVariable().getQualifiedName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isLastField(CtFieldRead fieldRead) {         //instance.fieldの時fieldのみを通す
        CtElement parent = fieldRead.getParent();
        if (parent != null && parent instanceof CtFieldAccess parentField) {
            if (fieldRead.getType().getQualifiedName().equals(parentField.getVariable().getDeclaringType().getQualifiedName())) {
                return false;
            }
        }
        return true;
    }

}
