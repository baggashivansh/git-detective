package com.gitdetective.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithAccessModifiers;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JavaSourceParser {

    public ParsedJavaFile parse(Path file) {
        long started = System.currentTimeMillis();
        try {
            CompilationUnit unit = StaticJavaParser.parse(file);
            String packageName =
                    unit.getPackageDeclaration()
                            .map(declaration -> declaration.getNameAsString())
                            .orElse("");

            List<ParsedJavaFile.ImportInfo> imports =
                    unit.getImports().stream()
                            .map(
                                    importDecl ->
                                            new ParsedJavaFile.ImportInfo(
                                                    importDecl.getNameAsString(),
                                                    importDecl.isStatic(),
                                                    importDecl.isAsterisk()))
                            .toList();

            List<ParsedJavaFile.TypeInfo> types = new ArrayList<>();
            for (TypeDeclaration<?> type : unit.getTypes()) {
                types.add(toTypeInfo(packageName, type));
            }

            List<String> exports =
                    types.stream()
                            .filter(type -> "PUBLIC".equalsIgnoreCase(type.visibility()))
                            .map(ParsedJavaFile.TypeInfo::fullyQualifiedName)
                            .toList();

            log.debug(
                    "Java parser finish file={} types={} durationMs={}",
                    file,
                    types.size(),
                    System.currentTimeMillis() - started);
            return new ParsedJavaFile(packageName, imports, exports, types);
        } catch (Exception exception) {
            log.warn("Java parser failed file={} message={}", file, exception.getMessage());
            return new ParsedJavaFile("", List.of(), List.of(), List.of());
        }
    }

    private ParsedJavaFile.TypeInfo toTypeInfo(String packageName, TypeDeclaration<?> type) {
        String name = type.getNameAsString();
        String fqn = packageName.isBlank() ? name : packageName + "." + name;
        String kind = resolveKind(type);
        String visibility = resolveVisibility(type);
        String superclass = null;
        List<String> interfaces = List.of();

        if (type instanceof ClassOrInterfaceDeclaration classOrInterface) {
            if (classOrInterface.isInterface()) {
                interfaces =
                        classOrInterface.getExtendedTypes().stream().map(Object::toString).toList();
            } else {
                superclass =
                        classOrInterface.getExtendedTypes().stream()
                                .findFirst()
                                .map(Object::toString)
                                .orElse(null);
                interfaces =
                        classOrInterface.getImplementedTypes().stream()
                                .map(Object::toString)
                                .toList();
            }
        }

        List<ParsedJavaFile.MethodInfo> methods = new ArrayList<>();
        for (MethodDeclaration method : type.getMethods()) {
            methods.add(
                    new ParsedJavaFile.MethodInfo(
                            method.getNameAsString(),
                            method.getDeclarationAsString(false, false, true),
                            method.getType().asString(),
                            resolveVisibility(method),
                            false,
                            method.getParameters().size(),
                            annotationNames(method.getAnnotations()),
                            method.getBegin().map(position -> position.line).orElse(null)));
        }
        for (ConstructorDeclaration constructor : type.getConstructors()) {
            methods.add(
                    new ParsedJavaFile.MethodInfo(
                            constructor.getNameAsString(),
                            constructor.getDeclarationAsString(false, false, true),
                            name,
                            resolveVisibility(constructor),
                            true,
                            constructor.getParameters().size(),
                            annotationNames(constructor.getAnnotations()),
                            constructor.getBegin().map(position -> position.line).orElse(null)));
        }

        List<ParsedJavaFile.FieldInfo> fields = new ArrayList<>();
        for (FieldDeclaration field : type.getFields()) {
            String fieldVisibility = resolveVisibility(field);
            List<String> annotations = annotationNames(field.getAnnotations());
            field.getVariables()
                    .forEach(
                            variable ->
                                    fields.add(
                                            new ParsedJavaFile.FieldInfo(
                                                    variable.getNameAsString(),
                                                    variable.getType().asString(),
                                                    fieldVisibility,
                                                    annotations)));
        }

        return new ParsedJavaFile.TypeInfo(
                name,
                fqn,
                kind,
                visibility,
                superclass,
                interfaces,
                annotationNames(type.getAnnotations()),
                methods,
                fields,
                type.getBegin().map(position -> position.line).orElse(null),
                type.getEnd().map(position -> position.line).orElse(null));
    }

    private String resolveKind(TypeDeclaration<?> type) {
        if (type instanceof EnumDeclaration) {
            return "ENUM";
        }
        if (type instanceof ClassOrInterfaceDeclaration classOrInterface) {
            if (classOrInterface.isInterface()) {
                return "INTERFACE";
            }
            if (classOrInterface.isAnnotationDeclaration()) {
                return "ANNOTATION";
            }
            return "CLASS";
        }
        return "CLASS";
    }

    private String resolveVisibility(NodeWithAccessModifiers<?> node) {
        AccessSpecifier accessSpecifier = node.getAccessSpecifier();
        if (accessSpecifier == AccessSpecifier.NONE) {
            return "PACKAGE";
        }
        return accessSpecifier.name().toUpperCase(Locale.ROOT);
    }

    private List<String> annotationNames(List<AnnotationExpr> annotations) {
        return annotations.stream().map(AnnotationExpr::getNameAsString).toList();
    }
}
