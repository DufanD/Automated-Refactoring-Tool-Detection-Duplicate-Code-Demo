package com.finalproject.automated.refactoring.tool.demo.service;

import com.finalproject.automated.refactoring.tool.model.MethodModel;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

public interface AutomatedRefactoring {

    Map<String, List<MethodModel>> detect(@lombok.NonNull String path);

    Map<String, Map<String, List<MethodModel>>> detect(@NonNull List<String> paths);

    void automatedRefactoring(@NonNull List<String> paths);
}
