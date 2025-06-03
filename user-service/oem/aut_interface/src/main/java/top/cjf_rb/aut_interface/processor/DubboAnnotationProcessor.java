package top.cjf_rb.aut_interface.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import top.cjf_rb.aut_interface.annotation.AutoDubboService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// 处理器类实现
@AutoService(Processor.class)
@SupportedAnnotationTypes("top.cjf_rb.aut_interface.annotation.AutoDubboService")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class DubboAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element element : elements) {
                if (element.getKind() == ElementKind.CLASS) {
                    TypeElement classElement = (TypeElement) element;
                    generateInterfaceAndImpl(classElement);
                }
            }
        }
        return true;
    }

    private void generateInterfaceAndImpl(TypeElement classElement) {
        String className = classElement.getSimpleName().toString();
        AutoDubboService generateDubbo = classElement.getAnnotation(AutoDubboService.class);
        // 接口名称
        String interfaceName = generateDubbo.interfaceName().isEmpty() ? className : generateDubbo.interfaceName();

        // 实现类名称
        String implName = generateDubbo.implName().isEmpty() ? interfaceName + "Impl" : generateDubbo.implName();

        // 生成的包路径
        String packageName = processingEnv.getElementUtils().getPackageOf(classElement).toString();

        // 接口路径
        String interfacePackage = packageName + generateDubbo.interfacePackage();

        // 实现类路径
        String implPackage = interfacePackage + generateDubbo.implPackage();

        // 生成接口
        TypeSpec interfaceSpec = buildInterfaceSpec(classElement, interfaceName);
        writeFile(interfacePackage, interfaceSpec);

        // 生成实现类
        TypeSpec implSpec = buildImplSpec(classElement, interfaceName, implName, packageName, interfacePackage);
        writeFile(implPackage, implSpec);
    }

    private TypeSpec buildInterfaceSpec(TypeElement classElement, String interfaceName) {
        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(interfaceName)
                .addModifiers(Modifier.PUBLIC);

        classElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD && e.getModifiers().contains(Modifier.PUBLIC))
                .map(e -> (ExecutableElement) e)
                .forEach(method -> {
                    MethodSpec methodSpec = MethodSpec.methodBuilder(method.getSimpleName().toString())
                            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                            .returns(TypeName.get(method.getReturnType()))
                            .addParameters(method.getParameters().stream()
                                    .map(p -> ParameterSpec.builder(TypeName.get(p.asType()),
                                                    p.getSimpleName().toString())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build();
                    interfaceBuilder.addMethod(methodSpec);
                });

        return interfaceBuilder.build();
    }


    private TypeSpec buildImplSpec(TypeElement classElement, String interfaceName, String implName,
                                   String packageName, String interfacePackage) {
        ClassName interfaceType = ClassName.get(interfacePackage, interfaceName);
        ClassName delegateType = ClassName.get(packageName, classElement.getSimpleName().toString());

        // 分解字段和构造方法生成
        FieldSpec delegateField = buildDelegateField(delegateType);
        MethodSpec constructor = buildConstructor(delegateType);

        // 分解方法生成逻辑
        List<MethodSpec> methods = buildDelegateMethods(classElement);

        // 分解注解处理逻辑
        ClassName serviceAnnotationType = resolveServiceAnnotation(classElement);
        AnnotationSpec serviceAnnotation = buildServiceAnnotation(classElement, serviceAnnotationType);

        return TypeSpec.classBuilder(implName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(serviceAnnotation)
                .addSuperinterface(interfaceType)
                .addField(delegateField)
                .addMethod(constructor)
                .addMethods(methods)
                .build();
    }

    // 新增方法：构建委托字段
    private FieldSpec buildDelegateField(ClassName delegateType) {
        return FieldSpec.builder(delegateType, "delegate")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }

    // 新增方法：构建构造方法
    private MethodSpec buildConstructor(ClassName delegateType) {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(delegateType, "delegate")
                .addStatement("this.delegate = java.util.Objects.requireNonNull(delegate)")
                .build();
    }

    // 新增方法：构建委托方法
    private List<MethodSpec> buildDelegateMethods(TypeElement classElement) {
        return classElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD && e.getModifiers().contains(Modifier.PUBLIC))
                .map(e -> (ExecutableElement) e)
                .map(this::buildDelegateMethod)
                .collect(Collectors.toList());
    }

    // 新增方法：构建单个委托方法
    private MethodSpec buildDelegateMethod(ExecutableElement method) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(method.getReturnType()))
                .addParameters(buildMethodParameters(method));

        if (method.getReturnType().getKind() != TypeKind.VOID) {
            builder.addStatement("return delegate.$N($L)", method.getSimpleName(), getParameterNames(method));
        } else {
            builder.addStatement("delegate.$N($L)", method.getSimpleName(), getParameterNames(method));
        }
        return builder.build();
    }

    // 新增方法：构建方法参数
    private List<ParameterSpec> buildMethodParameters(ExecutableElement method) {
        return method.getParameters().stream()
                .map(p -> ParameterSpec.builder(TypeName.get(p.asType()), p.getSimpleName().toString()).build())
                .collect(Collectors.toList());
    }

    // 新增方法：解析服务注解类型
    private ClassName resolveServiceAnnotation(TypeElement classElement) {
        return classElement.getAnnotationMirrors().stream()
                .map(am -> am.getAnnotationType().toString())
                .filter(type -> type.equals("org.apache.dubbo.config.annotation.DubboService") ||
                        type.equals("org.springframework.stereotype.Service"))
                .map(this::parseClassName)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("找不到有效的服务注解"));
    }

    // 新增方法：构建服务注解
    private AnnotationSpec buildServiceAnnotation(TypeElement classElement, ClassName annotationType) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(annotationType);
        classElement.getAnnotationMirrors().stream()
                .filter(am -> am.getAnnotationType().toString().equals(annotationType.toString()))
                .findFirst()
                .ifPresent(anno -> copyAnnotationValues(anno, builder));
        return builder.build();
    }

    // 新增方法：拷贝注解值
    private void copyAnnotationValues(AnnotationMirror annotation, AnnotationSpec.Builder builder) {
        annotation.getElementValues().forEach((key, value) -> {
            String memberName = key.getSimpleName().toString();
            Object val = value.getValue();
            if (val == null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                        "注解字段值为 null: " + memberName);
                return;
            }

            switch (val) {
                case Collection<?> coll -> {
                    if (coll.isEmpty()) {
                        builder.addMember(memberName, "$T.emptyArray()", ClassName.get("com.util", "ArrayUtils"));
                    } else {
                        addArrayAnnotationMember(builder, memberName, List.of(coll.toArray()));
                    }
                }
                case Enum<?> enumVal -> builder.addMember(memberName, "$T.$L", enumVal.getDeclaringClass(), enumVal);
                case String s -> builder.addMember(memberName, "$S", s);
                case Number num -> builder.addMember(memberName, "$L", num);
                case Boolean b -> builder.addMember(memberName, "$L", b);
                case TypeMirror tm -> builder.addMember(memberName, "$T.class", TypeName.get(tm));
                case AnnotationMirror nestedAnno -> builder.addMember(memberName, "$L", AnnotationSpec.get(nestedAnno));
                case Object obj when obj.getClass().isArray() -> handleArray(builder, memberName, obj);
                default -> processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                        "不支持的注解值类型: " + val.getClass().getName());
            }
        });
    }

    // 处理数组
    private void handleArray(AnnotationSpec.Builder builder, String memberName, Object array) {
        // 创建一个CodeBlock.Builder对象
        CodeBlock.Builder codeBlock = CodeBlock.builder().add("{");

        // 获取数组的长度
        int length = Array.getLength(array);
        // 遍历数组
        for (int i = 0; i < length; i++) {
            // 获取数组中的元素
            Object element = Array.get(array, i);
            // 如果元素是AnnotationMirror类型
            if (element instanceof AnnotationMirror anno) {
                // 将AnnotationMirror转换为AnnotationSpec
                codeBlock.add("$L", AnnotationSpec.get(anno));
                // 如果元素是String类型
            } else if (element instanceof String) {
                // 将String元素添加到CodeBlock中
                codeBlock.add("$S", element);
                // 如果元素是其他类型
            } else {
                // 将元素添加到CodeBlock中
                codeBlock.add("$L", element);
            }
            // 如果不是最后一个元素，添加一个逗号
            if (i < length - 1) codeBlock.add(", ");
        }
        // 添加一个右括号
        codeBlock.add("}");

        // 将CodeBlock添加到AnnotationSpec中
        builder.addMember(memberName, "$L", codeBlock.build());
    }

    // 新增通用集合处理方法
    private void addArrayAnnotationMember(AnnotationSpec.Builder builder, String memberName, Collection<?> collection) {
        CodeBlock elements = collection.stream()
                .map(item -> {
                    if (item instanceof String) return CodeBlock.of("$S", item);
                    if (item instanceof AnnotationMirror)
                        return CodeBlock.of("$L", AnnotationSpec.get((AnnotationMirror) item));
                    return CodeBlock.of("$L", item);
                })
                .collect(CodeBlock.joining(", "));

        builder.addMember(memberName, "{$L}", elements);
    }

    // 新增方法：解析类名
    private ClassName parseClassName(String fullClassName) {
        String[] parts = fullClassName.split("\\.");
        String pkg = String.join(".", Arrays.asList(parts).subList(0, parts.length - 1));
        String name = parts[parts.length - 1];
        return ClassName.get(pkg, name);
    }

    private void writeFile(String packageName, TypeSpec typeSpec) {
        try {
            JavaFile.builder(packageName, typeSpec)
                    .build()
                    .writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "生成文件失败: " + e.getMessage());
        }
    }

    private String getParameterNames(ExecutableElement method) {
        return method.getParameters().stream()
                .map(p -> p.getSimpleName().toString())
                .collect(Collectors.joining(", "));
    }
}
