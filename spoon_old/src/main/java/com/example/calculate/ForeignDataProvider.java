package com.example.calculate;

import java.util.ArrayList;
import java.util.List;

import com.example.node.ClassMetrics;
import com.example.node.MethodMetrics;

public class ForeignDataProvider implements IAttribute {

    @Override
    public String getName() {
        return "FDP";
    }

    @Override
    public void calculate(ClassMetrics node) {
        List<String> Provider = new ArrayList<>();
        List<MethodMetrics> methodsMetrics = node.getMethodsMetrics();
        float total = 0;
        for (MethodMetrics methodMetrics : methodsMetrics) {
            total += calculateFDP(methodMetrics, Provider);
        }
        node.setMetric(getName(), total);
    }

    @Override
    public void calculate(MethodMetrics node) {
        List<String> Provider = new ArrayList<>();
        node.setMetric(getName(), calculateFDP(node, Provider));
    }

    public float calculateFDP(MethodMetrics node, List<String> FDP) {
        List<String> ListOfATFD = (List<String>) node.getAttribute("ListOfATFD");
        float newClass = 0;
        if (ListOfATFD != null && !ListOfATFD.isEmpty()) {
            for (String className : ListOfATFD) {
                if (!FDP.contains(className)) {
                    FDP.add(className);
                    newClass += 1;
                }
            }
            return newClass;
        } else {
            return 0;
        }
    }

}
