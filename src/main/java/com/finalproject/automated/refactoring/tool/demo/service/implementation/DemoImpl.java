package com.finalproject.automated.refactoring.tool.demo.service.implementation;

import com.finalproject.automated.refactoring.tool.demo.service.AutomatedRefactoring;
import com.finalproject.automated.refactoring.tool.demo.service.Demo;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DemoImpl implements Demo {

    @Autowired
    private AutomatedRefactoring automatedRefactoring;

    @Override
    public void demo(@NonNull List<String> paths) {
        Long startTime = System.nanoTime();

        automatedRefactoring.automatedRefactoring(paths);

        Long endTime = System.nanoTime();
        printRunTime(startTime, endTime);
    }

    private void printRunTime(Long startTime, Long endTime) {
        double runTime = (double) (endTime - startTime);
        runTime /= 1000000000;

        System.out.println();
        System.out.println("Time consume --> " + runTime + " seconds");
        System.out.println();
    }
}
