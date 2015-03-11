package ru.ifmo.ctddev.kachalskiy.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class Implementor implements Impler {

    private StringBuilder sb;
    private List<Method> allMethods;
    public Implementor() {
        sb = new StringBuilder();
        allMethods = new ArrayList<>();
    }
    public static void main(String args[]) throws ImplerException {
        try {
            Implementor implementor = new Implementor();
            implementor.implement(Class.forName(args[0]), new File(args[1]));
        } catch (Exception e) {
        	System.err.println(e.toString());
        }
    }
    public void implement(Class<?> token, File root) throws ImplerException {
        if (token.isPrimitive()) {
            throw new ImplerException();
        }
        try {
            String path = root.getCanonicalPath() + "/" + token.getPackage().getName().replaceAll("\\.", "/");
            File file = new File(path);
            file.mkdirs();
            try (FileWriter fileWriter = new FileWriter(path + "/" + token.getSimpleName() + "Impl.java")) {
                printClass(token);

                fileWriter.append(sb.toString());
                sb.delete(0, sb.length());
                allMethods.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Input/Output error");
        }
    }
    
    private void printClass(Class<?> token) throws ImplerException {
        sb.append("package " + token.getPackage().getName() + ";\n\n");
        sb.append("public class " + token.getSimpleName() + "Impl ");
        sb.append("implements " + token.getCanonicalName());
        sb.append(" {\n");
        printMethods(token);
        sb.append("\n}");
    }
    private void printMethods(Class<?> token) {
        collectAllMethods(token);
        for (int i = 0; i < allMethods.size(); i++) {
            printMethod(allMethods.get(i));
        }
    }
    private void printMethod(Method method) {
        int modifiers = method.getModifiers();
        if (!Modifier.isAbstract(modifiers)) {
            return;
        }
        sb.append("\n" + "    " + "@Override");
        modifiers ^= Modifier.ABSTRACT;
        
        if (Modifier.isTransient(modifiers)) {
            modifiers ^= Modifier.TRANSIENT;
        }
        sb.append("\n");
        sb.append("    ");
        sb.append(Modifier.toString(modifiers) + " " +
                method.getReturnType().getCanonicalName() + " " +
                method.getName() +
                "(" + getArgs(method) + ")" +
                " { return " + getReturnValue(method) + "; }\n");
    }
    private String getReturnValue(Method method) {
        Class<?> returnValue = method.getReturnType();
        if (!returnValue.isPrimitive()) {
            return "null";
        } else {
            if ("char".equals(returnValue.getSimpleName())) {
                return "'a'";
            }
            if ("boolean".equals(returnValue.getSimpleName())) {
                return "true";
            }
            if (!"void".equals(returnValue.getSimpleName())) {
                return "0";
            }
            return "";
        }
    }
    private String getArgs(Method method) {
        StringBuilder ans = new StringBuilder();
        Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            if (i == parameters.length - 1) {
                ans.append(parameters[i].getCanonicalName() + " args" + i);
            } else {
                ans.append(parameters[i].getCanonicalName() + " args" + i + ", ");
            }
        }
        return ans.toString();
    }
    private void collectAllMethods(Class<?> token) {

        if (token == null) {
            return;
        }

        addAllMethods(token);

        Class[] interfaces = token.getInterfaces();
        for (Class i : interfaces) {
            collectAllMethods(i);
        }

        collectAllMethods(token.getSuperclass());

    }

    private void addAllMethods(Class<?> token) {

        Method[] methods = token.getDeclaredMethods();

        for (Method method : methods) {

            boolean flag = false;
            for (int i = 0; i < allMethods.size(); i++) {
                if (equalMethod(method, allMethods.get(i))) {
                    flag = true;
                    break;
                }
            }
            if (flag == false) {
                allMethods.add(method);
            }
        }
    }

    private boolean equalMethod(Method a, Method b) {

        if (!a.getName().equals(b.getName())) {
            return false;
        }
        Class[] paramTypesA = a.getParameterTypes();
        Class[] paramTypesB = b.getParameterTypes();
        if (paramTypesA.length != paramTypesB.length) {
            return false;
        }
        for (int i = 0; i < paramTypesA.length; i++) {
            if (!(paramTypesA[i].getCanonicalName()).equals(paramTypesB[i].getCanonicalName())) {
                return false;
            }
        }
        return true;
    }
}