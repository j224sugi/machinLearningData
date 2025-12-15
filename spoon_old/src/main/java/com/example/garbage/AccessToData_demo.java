package com.example.garbage;

import java.util.ArrayList;
import java.util.List;

import com.example.calculate.IAttribute;
import com.example.node.ClassMetrics;
import com.example.node.MethodMetrics;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.CtScanner;

public class AccessToData_demo extends CtScanner implements IAttribute {

    private List<String> ListOfATFD = new ArrayList<>();
    private List<String> ListOfATLD = new ArrayList<>();
    private List<String> ListOfError = new ArrayList<>();
    private String nameOfParentClass;

    @Override
    public String getName() {
        return "ATFD";
    }

    @Override
    public void calculate(ClassMetrics node) {
        float sumOfATFD = 0;
        for (MethodMetrics methodMetrics : node.getMethodsMetrics()) {
            sumOfATFD = sumOfATFD + methodMetrics.getMetric(getName());
        }
        node.setMetric(getName(), sumOfATFD);
    }

    @Override
    public void calculate(MethodMetrics node) {
        ListOfError = new ArrayList<>();
        ListOfATLD = new ArrayList<>();
        ListOfATFD = new ArrayList<>();

        nameOfParentClass = node.getDeclaration().getParent(CtType.class).getQualifiedName();
        node.getDeclaration().accept(this);
        node.setAttribute("ListOfATFD", ListOfATFD);
        node.setAttribute("ListOfATLD",ListOfATLD);
        node.setAttribute("ListOfError",ListOfError);
    }

    @Override
    public <T extends Object> void visitCtInvocation(CtInvocation<T> invocation) {
        try{
            if(invocation.getExecutable().getType()!=null){
            String nameOfClass=invocation.getExecutable().getDeclaringType().getQualifiedName();
            if(!nameOfClass.equals(nameOfParentClass)){
                ListOfATFD.add(invocation.getExecutable().getSimpleName()+" "+nameOfClass);
            }else{
                ListOfATLD.add(invocation.getExecutable().getSimpleName()+" "+nameOfClass);
            }
            }else{
                ListOfError.add(invocation.getExecutable().getSimpleName()+" error");
            }
        }catch(Exception e){
            ListOfError.add(invocation.getExecutable().getSimpleName()+" "+e.getMessage());
        }
        super.visitCtInvocation(invocation);
    }

}
