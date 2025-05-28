package top.cjf_rb.aut_interface.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import top.cjf_rb.aut_interface.annotation.AutoDubboService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
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

        // 构建字段和构造方法
        FieldSpec delegateField = FieldSpec.builder(delegateType, "delegate")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(delegateType, "delegate")
                .addStatement("this.delegate = java.util.Objects.requireNonNull(delegate)")
                .build();

        // 构建方法
        List<MethodSpec> methods = new ArrayList<>();
        for (Element e : classElement.getEnclosedElements()) {
            if (e.getKind() == ElementKind.METHOD && e.getModifiers().contains(Modifier.PUBLIC)) {
                ExecutableElement method = (ExecutableElement) e;
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.get(method.getReturnType()))
                        .addParameters(method.getParameters().stream()
                                .map(p -> ParameterSpec.builder(TypeName.get(p.asType()), p.getSimpleName().toString())
                                        .build())
                                .collect(Collectors.toList()));

                if (method.getReturnType().getKind() != TypeKind.VOID) {
                    methodBuilder.addStatement("return delegate.$N($L)", method.getSimpleName(),
                            getParameterNames(method));
                } else {
                    methodBuilder.addStatement("delegate.$N($L)", method.getSimpleName(), getParameterNames(method));
                }
                methods.add(methodBuilder.build());
            }
        }

        // 构建 DubboService 注解
        AnnotationSpec.Builder dubboServiceBuilder = AnnotationSpec.builder(
                ClassName.get("org.apache.dubbo.config.annotation", "DubboService"));

        // 提取源类上的 DubboService 注解
        AnnotationMirror dubboServiceAnno = classElement.getAnnotationMirrors().stream()
                .filter(am -> "org.apache.dubbo.config.annotation.DubboService"
                        .equals(am.getAnnotationType().toString()))
                .findFirst()
                .orElse(null);

        if (dubboServiceAnno != null) {
            for (var entry : dubboServiceAnno.getElementValues().entrySet()) {
                ExecutableElement keyElement = entry.getKey();
                String key = keyElement.getSimpleName().toString();
                Object value = entry.getValue().getValue();

                // 处理不同类型的值
                if (value instanceof String) {
                    dubboServiceBuilder.addMember(key, "$S", value);
                } else if (value instanceof Integer) {
                    dubboServiceBuilder.addMember(key, "$L", value);
                } else if (value instanceof Boolean) {
                    dubboServiceBuilder.addMember(key, "$L", value);
                } else if (value instanceof Enum) {
                    dubboServiceBuilder.addMember(key, "$T.$L", ((Enum<?>) value).getDeclaringClass(), value);
                } else if (value instanceof List<?> listValue) {
                    if (!listValue.isEmpty()) {
                        StringBuilder sb = new StringBuilder("{");
                        for (Object item : listValue) {
                            if (item instanceof String) {
                                sb.append("$S").append(", ");
                            } else if (item instanceof Integer) {
                                sb.append("$L").append(", ");
                            }
                        }
                        sb.setLength(sb.length() - 2); // 去掉最后一个逗号和空格
                        sb.append("}");
                        dubboServiceBuilder.addMember(key, sb.toString(), listValue.toArray());
                    }
                }
            }
        }

        return TypeSpec.classBuilder(implName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(dubboServiceBuilder.build()) // 使用构建的注解
                .addSuperinterface(interfaceType)
                .addField(delegateField)
                .addMethod(constructor)
                .addMethods(methods)
                .build();
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
