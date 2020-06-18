package com.finalproject.automated.refactoring.tool.demo.service.implementation;

import com.finalproject.automated.refactoring.tool.demo.service.AutomatedRefactoring;
import com.finalproject.automated.refactoring.tool.duplicate.code.detection.model.CloneCandidate;
import com.finalproject.automated.refactoring.tool.duplicate.code.detection.model.ClonePair;
import com.finalproject.automated.refactoring.tool.duplicate.code.detection.service.DuplicateCodeDetection;
import com.finalproject.automated.refactoring.tool.files.detection.model.FileModel;
import com.finalproject.automated.refactoring.tool.files.detection.service.FilesDetection;
import com.finalproject.automated.refactoring.tool.methods.detection.service.MethodsDetection;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import com.finalproject.automated.refactoring.tool.model.StatementModel;
import com.finalproject.automated.refactoring.tool.refactoring.service.Refactoring;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class AutomatedRefactoringImpl implements AutomatedRefactoring {

    @Autowired
    private FilesDetection filesDetection;

    @Autowired
    private MethodsDetection methodsDetection;

    @Autowired
    private DuplicateCodeDetection duplicateCodeDetection;

    @Autowired
    private Refactoring refactoring;

    @Value("${files.mime.type}")
    private String mimeType;

    @Override
    public Map<String, List<MethodModel>> detect(@NonNull String path) {
        return detect(Collections.singletonList(path))
                .get(path);
    }

    @Override
    public Map<String, Map<String, List<MethodModel>>> detect(@NonNull List<String> paths) {
        return filesDetection.detect(paths, mimeType)
                .entrySet()
                .parallelStream()
                .collect(Collectors.toMap(Map.Entry::getKey, this::detectMethods));
    }

    @Override
    public void automatedRefactoring(@NonNull List<String> paths) {
        Map<String, Map<String, List<ClonePair>>> clonePairs = doDetection(paths);

        if (userInputRefactor()) {
            doRefactor(clonePairs);
        }
    }

    public Map<String, Map<String, List<ClonePair>>> doDetection(List<String> paths) {
        Map<String, Map<String, List<MethodModel>>> methods;
        Map<String, Map<String, List<ClonePair>>> clonePairs = new HashMap<>();

        System.out.println("\nLoading.....");
        methods = filesDetection.detect(paths, mimeType)
                                .entrySet()
                                .parallelStream()
                                .collect(Collectors.toMap(Map.Entry::getKey, this::detectMethods));

        AtomicInteger sumMethod = new AtomicInteger();

        methods.entrySet()
               .stream()
               .forEach(method ->
               {
                   clonePairs.put(method.getKey(), detectDuplicateCode(method.getValue()));
                   method.getValue().entrySet().stream().forEach( file ->  {
                       sumMethod.addAndGet(file.getValue().size());
                       //System.out.println("Sum method file: " + file.getValue().size());
                   });
               }
               );

        printDetectionResult(sumMethod, clonePairs);
        return clonePairs;
    }

    private Map<String, List<MethodModel>> detectMethods(Map.Entry<String, List<FileModel>> entry) {
        Map<String, List<MethodModel>> methods = methodsDetection.detect(entry.getValue());
        return methods;
    }

    private Map<String, List<ClonePair>> detectDuplicateCode(Map<String, List<MethodModel>> entry) {
        Map<String, List<ClonePair>> clonePairs = duplicateCodeDetection.detect(entry, 3L);
        return clonePairs;
    }

    private void printDetectionResult(AtomicInteger sumMethod, Map<String, Map<String, List<ClonePair>>> clonePairs) {
        AtomicInteger sumPair = new AtomicInteger();
        AtomicInteger sumFile = new AtomicInteger();

        clonePairs.entrySet()
                .stream()
                .forEach(clonePairBase ->
                {
                    System.out.println("\nBase Project: " + clonePairBase.getKey());
                    clonePairBase.getValue().entrySet().stream()
                            .forEach(clonePairFile ->
                            {
                                System.out.println("File: " + clonePairFile.getKey());
                                System.out.println("Sum Clone Pair File: " + clonePairFile.getValue().size());
                                System.out.println("List Clone Pair");
                                printListClone(clonePairFile.getValue(), clonePairFile.getKey());
                                System.out.println();
                                sumPair.addAndGet(clonePairFile.getValue().size());
                            });
                    sumFile.addAndGet(clonePairBase.getValue().size());
                });
        System.out.println("Sum of Java Files : " + sumFile);
        System.out.println("Sum of Methods: " + sumMethod);
        System.out.println("Sum of Clone Pairs: " + sumPair);
    }

    private void printListClone(List<ClonePair> clonePairList, String path) {
        int index = 1;
        for (ClonePair clone : clonePairList) {
            System.out.println("\nClone Pair " + index + " :");
            printListCandidate(clone.getCloneCandidates(), path);
            index++;
        }
    }

    private void printListCandidate(List<CloneCandidate> cloneCandidateList, String path) {
        int index = 1;
        for (CloneCandidate candidate : cloneCandidateList) {
            if (path.equals(candidate.getPath()))
                System.out.println("Clone Candidate " + index + " :");
            else
                System.out.println("Clone Candidate " + index + " : (" + candidate.getPath() + ")");
            printStatements(candidate.getStatements());
            System.out.println();
            index++;
        }
    }

    private void printStatements(List<StatementModel> statementModels) {
        for (StatementModel statementModel : statementModels) {
            System.out.println(statementModel.getStatement());
        }
    }

    private boolean userInputRefactor() {
        do {
            System.out.print("\nDo you want to refactor? (y/n) ");
            Scanner in = new Scanner(System.in);
            String answer = in.nextLine();

            if (answer.equals("y")) return true;
            else if (answer.equals("n")) return false;
        } while (true);
    }

    private void doRefactor(Map<String, Map<String, List<ClonePair>>> clonePairs) {
        System.out.println("\nRefactoring In Progress.....");
        clonePairs.entrySet()
                .stream()
                .forEach(clonePairBase ->
                        refactoring.refactoringDuplicate(clonePairBase.getValue()));
        System.out.println("\nDone");
    }
}
