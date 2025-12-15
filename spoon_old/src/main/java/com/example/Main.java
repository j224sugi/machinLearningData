package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtType;

public class Main {

    public static void main(String[] args) throws IOException {
        String projectFilesText = args[0];
        String outPutFile = args[1];

        String allFilesString = getAllFiles(projectFilesText);
        allFilesString = allFilesString.replace("\r\n", "\n");
        String[] modulesFiles = allFilesString.split("\n\n");
        int flag = 0;
        for (String moduleFiles : modulesFiles) {
            Visitor visitor = new Visitor();
            List<String> ListModuleFiles = new ArrayList<>(Arrays.asList(moduleFiles.split("\n")));
            ListModuleFiles.removeIf(f -> {
                f = f.trim();
                return f.isEmpty() || !Files.exists(Paths.get(f));
            });
            
            Launcher launcher = new Launcher();
            for (String file : ListModuleFiles) {
                launcher.addInputResource(file);
            }
            launcher.getEnvironment().setCommentEnabled(false);
            launcher.getEnvironment().setAutoImports(true);
            launcher.getEnvironment().setLevel("DEBUG");

            CtModel model = launcher.buildModel();
            for (CtType<?> clazz : model.getAllTypes()) {
                clazz.accept(visitor);
            }
            visitor.printCSV(outPutFile, flag);
            flag = 1;

        }

    }

    public static List<String> getdiffFiles(String path) {
        try {
            File File = new File(path);   //後ほどlogを読み取ったファイルに置き換える
            List<String> FileList;
            try (BufferedReader Reader = new BufferedReader(new FileReader(File))) {
                FileList = new ArrayList<>();
                String str;
                while ((str = Reader.readLine()) != null) {
                    FileList.add(str);                                          //１つ目の要素はgitのhash値　それ以降の要素が変更のあったファイル名
                }
            }
            return FileList;
        } catch (IOException e) {
            System.out.println(e);
        }
        return null;
    }

    public static String getAllFiles(String path) {
        try {
            File file = new File(path);
            String allString;
            try (BufferedReader Reader = new BufferedReader(new FileReader(file))) {
                int str;
                allString = "";
                while ((str = Reader.read()) != -1) {
                    allString = allString + (char) str;
                }
            }
            return allString;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}

/*  
        try {
            File allfile = new File("c:\\Users\\sugii syuji\\spoonTEXT\\demo\\first.txt");
            BufferedReader allreader = new BufferedReader(new FileReader(allfile));
            String str;
            while ((str = allreader.readLine()) != null) {
                launcher.addInputResource(str);
            }
            allreader.close();
        } catch (IOException e) {
            System.out.println(e);
        }
private static List<String> addJarSourceFile(Path path) throws IOException {
        List<String> JarFile;
        try (Stream<Path> paths = Files.walk(path)) {
            JarFile = paths.filter(p -> p.toString().endsWith(".jar")).map(p -> p.toString()).toList();
        }
        return JarFile;
    }

}
   
    //JarFile=filterConflictingJars(JarFile);
    private static List<String> filterConflictingJars(List<String> jars) {
        List<String> safe = new ArrayList<>();
        for (String jar : jars) {
            try (JarFile jf = new JarFile(jar)) {
                boolean hasConflict = jf.stream().anyMatch(entry -> {
                    String name = entry.getName();
                    return name.startsWith("org/w3c/dom")
                            || name.startsWith("javax/xml/")
                            || name.startsWith("org/xml/sax/");
                });
                if (!hasConflict) {
                    safe.add(jar);
                } else {
                    System.out.println("Conflict : " + jar);
                }
            } catch (Exception e) {
            }
        }
        return safe;
    }*/
