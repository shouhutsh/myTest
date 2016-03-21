package com.aemoney.web.webservice;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ae-mp02 on 2016/3/16.
 */
public class CreateWebService {

    Map<String, String> projects;

    String FileType = ".java";
    String ExportPath = ".";

    String INTERFACE = "interface";
    String IMPL = "impl";

    String PathSplitFlag = "\\";

    @Before
    public void setUp() {
        ExportPath = "D:\\code\\develop\\webservice\\src\\main\\java";

        projects = new HashMap<String, String>();
        projects.put("eaccount", "D:\\code\\develop\\DQCitizenWallet\\eaccount\\eaccount_common\\trunk\\rs-common\\src");
        projects.put("countcore", "D:\\code\\develop\\DQCitizenWallet\\countcore\\countcore_common\\trunk\\rs-common\\src");
        projects.put("settlecore", "D:\\code\\develop\\DQCitizenWallet\\settlecore\\settlecore_common\\trunk\\rs-common\\src");
    }

    @Test
    public void create() throws Exception {
        for (Map.Entry<String, String> e : projects.entrySet()) {
            Map<String, List<String>> javaPackages = findFile(e.getValue(), FileType);
            for (Map.Entry<String, List<String>> p : javaPackages.entrySet()) {
                String packageName = "ws." + p.getKey();
                for (String javaPath : p.getValue()) {
                    Map<String, BufferedWriter> createFiles = initFile(packageName, javaPath);
                    parseAndWrite(p.getKey(), javaPath, createFiles);
                    closeFile(createFiles);
                }
            }
        }
    }

    private void closeFile(Map<String, BufferedWriter> createFiles) throws IOException {
        for (Map.Entry<String, BufferedWriter> e : createFiles.entrySet()) {
            BufferedWriter writer = e.getValue();
            writer.write("\n}");
            writer.flush();
            writer.close();
        }
    }

    private void parseAndWrite(String packageName, String javaPath, Map<String, BufferedWriter> createFiles) throws Exception {
        final String IfMethodTemplate = "\n%s %s(%s)%s;\n";
        final String SetTemplate = "\n%PACKAGE.%CLASS %VAR;\n@Autowired\npublic void set%CLASS(%PACKAGE.%CLASS %VAR){\nthis.%VAR = %VAR;\n}\n";

        Pattern p = Pattern.compile("([^ \t]+) ([^ (]+)\\((.*?)\\)(.*?);");
        String className = getJavaName(javaPath);
        String classVar = getVariableName(className);

        Map<String, Integer> functionNames = new HashMap<String, Integer>();
        createFiles.get(IMPL).write(SetTemplate.replaceAll("%PACKAGE", packageName).replaceAll("%CLASS", className).replaceAll("%VAR", classVar));
        for (String line : Files.readAllLines(Paths.get(javaPath), StandardCharsets.UTF_8)) {
            Matcher m = p.matcher(line);
            if (m.find()) {
                if(m.group(1).contains("Grid")) continue;
                String returnType = getReturnType(line);
                String functionName = getFunctionName(m.group(2), functionNames);
                createFiles.get(INTERFACE).write(String.format(IfMethodTemplate, returnType, functionName, m.group(3), m.group(4)));
                createFiles.get(IMPL).write(String.format(getIpMethodTemplate(returnType),
                        returnType, functionName, m.group(3), m.group(4), "void".equals(m.group(1)) ? "" : "return", classVar, m.group(2), getParameter(m.group(3))));
            }
        }
    }

    private String getReturnType(String line) throws Exception {
        Pattern p = Pattern.compile("([^ \t]+) [^ (]+\\(");
        Matcher m = p.matcher(cleanPointBracket(line));
        if(m.find()){
            String returnType = m.group(1);
            if(returnType.contains("Map")){
                returnType = "ws.util.WSMap";
            }
            return returnType;
        }
        throw new Exception();
    }

    private String getIpMethodTemplate(String returnType){
        if(returnType.contains("WSMap")){
            return "\npublic %s %s(%s)%s{\n%s new ws.util.WSMap(%s.%s(%s));\n}\n";
        }else{
            return "\npublic %s %s(%s)%s{\n%s %s.%s(%s);\n}\n";
        }
    }

    private String getFunctionName(String function, Map<String, Integer> functionNames){
        Integer i = functionNames.get(function);
        if(null == i){
            functionNames.put(function, new Integer(0));
            return function;
        }else{
            ++i;
            return function + i;
        }
    }

    private String getParameter(String classParameters) {
        StringBuffer sb = new StringBuffer();
        String parameters = cleanPointBracket(classParameters.trim());
        if (parameters.isEmpty()) return "";

        for (String cp : parameters.split(",")) {
            cp = cp.trim();
            sb.append("," + cp.substring(cp.lastIndexOf(" ")));
        }
        return sb.substring(1);
    }

    private String cleanPointBracket(String str) {
        StringBuffer sb = new StringBuffer();
        int count = 0;
        for (char c : str.toCharArray()) {
            if ('<' == c) ++count;
            if (count == 0) sb.append(c);
            if ('>' == c) --count;
        }
        return sb.toString();
    }

    private String getJavaName(String pathName) {
        return pathName.substring(pathName.lastIndexOf(PathSplitFlag) + 1, pathName.lastIndexOf("."));
    }

    private String getVariableName(String className) {
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    private String getImports(String javaPath) throws IOException {
        StringBuffer sb = new StringBuffer();
        for (String line : Files.readAllLines(Paths.get(javaPath), StandardCharsets.UTF_8)) {
            if (line.startsWith("import ") && !sb.toString().contains(line.trim())) {
                sb.append(line.trim() + "\n");
            } else if (line.trim().startsWith("public ")) {
                break;
            }
        }
        return sb.toString();
    }

    private String getClassName(String key) {
        return Character.toUpperCase(key.charAt(0)) + key.substring(1);
    }

    private void createDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) dir.mkdirs();
    }

    private Map<String, BufferedWriter> initFile(String packageName, String javaPath) throws IOException {
        final String InitIfTemplate = "package %s;\n%s\nimport javax.jws.WebService;\n@WebService\npublic interface %s {\n";
        final String InitIpTemplate = "package %s;\n%s\nimport javax.jws.WebService;\nimport org.springframework.beans.factory.annotation.Autowired;\n@SuppressWarnings(\"ALL\")\n@WebService(endpointInterface = \"%s.%s\")\npublic class %s implements %s {\n";

        Map<String, BufferedWriter> createFiles = new HashMap<String, BufferedWriter>();

        String imports = getImports(javaPath);
        String interName = getClassName(getJavaName(javaPath));
        String implName = interName + "Impl";

        String createPath = ExportPath + PathSplitFlag + packageName.replaceAll("\\.", "\\\\");
        createDir(createPath);

        BufferedWriter inter = new BufferedWriter(new FileWriter(new File(createPath + PathSplitFlag + interName + FileType)));
        inter.write(String.format(InitIfTemplate, packageName, imports, interName));

        BufferedWriter impl = new BufferedWriter(new FileWriter(new File(createPath + PathSplitFlag + implName + FileType)));
        impl.write(String.format(InitIpTemplate, packageName, imports, packageName, interName, implName, interName));

        createFiles.put(INTERFACE, inter);
        createFiles.put(IMPL, impl);
        return createFiles;
    }

    private Map<String, List<String>> findFile(String path, String type) throws IOException {
        Map<String, List<String>> finds = new HashMap<String, List<String>>();
        File dir = new File(path);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files.length == 0) {
                return new HashMap<String, List<String>>();
            } else {
                for (File f : files) {
                    if (f.isDirectory()) {
                        finds.putAll(findFile(f.getAbsolutePath(), type));
                    } else {
                        if (f.getName().toLowerCase().endsWith(type.toLowerCase())) {
                            String p = getPackageName(f.getPath());
                            if (finds.get(p) == null) {
                                finds.put(p, new ArrayList<String>());
                            }
                            finds.get(p).add(f.getPath());
                        }
                    }
                }
            }
        }
        return finds;
    }

    private String getPackageName(String filePath) throws IOException {
        for (String line : Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            if (line.startsWith("package ")) {
                return line.replace("package ", "").replace(";", "");
            }
        }
        return "";
    }
}
