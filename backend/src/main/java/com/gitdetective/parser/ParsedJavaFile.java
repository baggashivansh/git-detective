package com.gitdetective.parser;

import java.util.List;

public record ParsedJavaFile(
        String packageName, List<ImportInfo> imports, List<String> exports, List<TypeInfo> types) {

    public record ImportInfo(String name, boolean staticImport, boolean asterisk) {}

    public record TypeInfo(
            String name,
            String fullyQualifiedName,
            String kind,
            String visibility,
            String superclassName,
            List<String> implementedInterfaces,
            List<String> annotations,
            List<MethodInfo> methods,
            List<FieldInfo> fields,
            Integer startLine,
            Integer endLine) {}

    public record MethodInfo(
            String name,
            String signature,
            String returnType,
            String visibility,
            boolean constructor,
            int parameterCount,
            List<String> annotations,
            Integer startLine) {}

    public record FieldInfo(
            String name, String typeName, String visibility, List<String> annotations) {}
}
