package top.cjf_rb;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.type.Type;
import lombok.Getter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Mojo(name = "generate-dubbo", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class DubboGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;

    /**
     * 基础扫描包名
     */
    @Parameter(property = "basePackage", required = true)
    private String basePackage;

    /**
     * 接口模块名称
     */
    @Parameter(property = "interfaceModuleName", required = true)
    private String interfaceModuleName;

    /**
     * 实现类输出目录
     */
    @Parameter(property = "implModulePath", required = true)
    private String implModulePath;

    /**
     * 接口名后缀
     */
    @Parameter(property = "serviceSuffix", defaultValue = "Service")
    private String serviceSuffix;

    /**
     * 实现类名后缀
     */
    @Parameter(property = "implSuffix", defaultValue = "ServiceImpl")
    private String implSuffix;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            getLog().info("Starting Dubbo interface generation for package: " + basePackage);

            Set<ClassInfo> serviceClasses = scanCurrentModule();

            getLog().info("Found " + serviceClasses.size() + " Dubbo service classes");
            // 接口类固定放到 top.cjf_rb.api 下
            String interfacePkg = basePackage + ".api";
            getLog().info("接口包名: " + interfacePkg);
            // 实现类放到 provider.service
            String implPkg = basePackage + ".provider.service";
            getLog().info("实现类包名: " + implPkg);

            for (ClassInfo serviceClass : serviceClasses) {
                getLog().info("处理类: " + serviceClass.getClassName());
                generateDubboFiles(serviceClass, interfacePkg, implPkg);
            }

            getLog().info("Dubbo interface generation completed");
        } catch (Exception e) {
            throw new MojoExecutionException("Dubbo generation failed", e);
        }
    }

    private Set<ClassInfo> scanCurrentModule() {
        Set<ClassInfo> serviceClasses = new HashSet<>();

        String sourceDir = project.getBuild().getSourceDirectory();
        File dir = new File(sourceDir);

        if (dir.exists()) {
            scanJavaFiles(dir, basePackage, serviceClasses);
        }

        return serviceClasses;
    }

    private void scanJavaFiles(File dir, String packageName, Set<ClassInfo> serviceClasses) {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                scanJavaFiles(file, packageName + "." + file.getName(), serviceClasses);
            } else if (file.getName().endsWith(".java")) {
                try {
                    CompilationUnit cu =
                            new JavaParser().parse(file).getResult().orElseThrow(() -> new RuntimeException("解析文件失败: "
                                    + file.getName()));


                    ClassOrInterfaceDeclaration classDecl = cu.findFirst(ClassOrInterfaceDeclaration.class)
                            .orElseThrow(() -> new RuntimeException("No class found in file: " + file.getName()));

                    String className = classDecl.getNameAsString();

                    // 如果类已经有完整的 package 声明，则使用它的全限定名
                    Optional<String> classPackage = cu.getPackageDeclaration().map(NodeWithName::getNameAsString);

                    String fullClassName = classPackage.map(pkg -> pkg + "." + className)
                            .orElse(packageName + "." + className);

                    // 跳过 impl 包下的类
                    if (fullClassName.contains("service.")) {
                        getLog().debug("跳过处理类（impl包）: " + fullClassName);
                        continue;
                    }

                    if (hasAutoDubboServiceAnnotation(cu)) {
                        ClassInfo classInfo = new ClassInfo(fullClassName, cu);
                        serviceClasses.add(classInfo);
                        getLog().info("✅ 发现服务类: " + fullClassName);
                    }
                } catch (Exception e) {
                    getLog().warn("⚠️ 解析文件失败: " + file.getAbsolutePath(), e);
                }
            }
        }
    }

    private boolean hasAutoDubboServiceAnnotation(CompilationUnit cu) {
        return cu.findAll(ClassOrInterfaceDeclaration.class)
                .stream()
                .anyMatch(c -> c.getAnnotations()
                        .stream()
                        .anyMatch(a -> a.getNameAsString().equals("AutoDubboService")));
    }

    private void generateDubboFiles(ClassInfo serviceClass, String interfacePkg, String implPkg) throws IOException {


        // 构造接口和实现类的名称
        String simpleName = extractSimpleName(serviceClass.getClassName());
        String interfaceName = simpleName + serviceSuffix;
        String implName = simpleName + implSuffix;


        // 生成代码
        String interfaceCode = generateInterfaceCode(serviceClass, interfacePkg, interfaceName);
        String implCode = generateImplCode(serviceClass, implPkg, implName, interfaceName);

        File basedir = project.getBasedir(); // 👈 获取项目根目录
        // 拼接 api 模块的源码路径
        File apiModulePath = new File(basedir.getParentFile(), interfaceModuleName + "/src/main/java");

        // 写入文件
        writeFile(apiModulePath.getAbsolutePath(), interfacePkg, interfaceName, interfaceCode);
        writeFile(implModulePath, implPkg, implName, implCode);
    }

    private String extractSimpleName(String fullyQualifiedName) {
        int lastDotIndex = fullyQualifiedName.lastIndexOf('.');
        return (lastDotIndex >= 0)
                ? fullyQualifiedName.substring(lastDotIndex + 1)
                : fullyQualifiedName;
    }

    private String generateInterfaceCode(ClassInfo serviceClass, String pkg, String interfaceName) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("public interface ").append(interfaceName).append(" {\n");

        for (MethodDeclaration method : serviceClass.getMethods()) {
            sb.append("    ")
                    .append(typeToString(method.getType()))
                    .append(" ")
                    .append(method.getName())
                    .append("(");

            List<com.github.javaparser.ast.body.Parameter> params = method.getParameters();
            for (int i = 0; i < params.size(); i++) {
                sb.append(typeToString(params.get(i).getType())).append(" arg").append(i);
                if (i < params.size() - 1) sb.append(", ");
            }
            sb.append(");\n\n");
        }
        sb.append("}");
        return sb.toString();
    }

    private String generateImplCode(ClassInfo serviceClass, String pkg, String implName, String interfaceName) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import org.apache.dubbo.config.annotation.DubboService;\n");
        sb.append("import org.springframework.stereotype.Service;\n\n");

        sb.append("import ").append(basePackage).append(".").append(interfaceModuleName).append(".").append(interfaceName).append(";\n");

        sb.append("@Service\n");
        sb.append("@DubboService\n");
        sb.append("public class ").append(implName).append(" implements ")
                .append(interfaceName).append(" {\n\n");

        sb.append("    private final ").append(serviceClass.getClassName()).append(" delegate;\n\n");

        sb.append("    public ").append(implName).append("(")
                .append(serviceClass.getClassName()).append(" delegate) {\n")
                .append("        this.delegate = delegate;\n")
                .append("    }\n\n");

        for (MethodDeclaration method : serviceClass.getMethods()) {
            sb.append("    @Override\n")
                    .append("    public ")
                    .append(typeToString(method.getType()))
                    .append(" ")
                    .append(method.getName())
                    .append("(");

            List<com.github.javaparser.ast.body.Parameter> params = method.getParameters();
            for (int i = 0; i < params.size(); i++) {
                sb.append(typeToString(params.get(i).getType())).append(" arg").append(i);
                if (i < params.size() - 1) sb.append(", ");
            }
            sb.append(") {\n");

            sb.append("        ");
            if (!method.getType().toString().equals("void")) {
                sb.append("return ");
            }
            sb.append("delegate.").append(method.getName()).append("(");
            for (int i = 0; i < params.size(); i++) {
                sb.append("arg").append(i);
                if (i < params.size() - 1) sb.append(", ");
            }
            sb.append(");\n");

            sb.append("    }\n\n");
        }
        sb.append("}");
        return sb.toString();
    }

    // 新增方法：提取包名
    private String extractPackageName(String fullyQualifiedName) {
        int lastDotIndex = fullyQualifiedName.lastIndexOf('.');
        return (lastDotIndex >= 0)
                ? fullyQualifiedName.substring(0, lastDotIndex)
                : "";
    }

    private void writeFile(String basePath, String pkg, String className, String content) throws IOException {
        String dirPath = basePath + "/" + pkg.replace('.', '/');
        new File(dirPath).mkdirs();

        File file = new File(dirPath, className + ".java");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
            getLog().info("Generated: " + file.getAbsolutePath());
        }
    }

    private String typeToString(Type type) {
        return type.toString();
    }

    // 新增 ClassInfo 类来存储类信息
    private static class ClassInfo {
        @Getter
        private final String className;
        private final CompilationUnit compilationUnit;

        public ClassInfo(String className, CompilationUnit compilationUnit) {
            this.className = className;
            this.compilationUnit = compilationUnit;
        }

        public List<MethodDeclaration> getMethods() {
            return compilationUnit.findAll(MethodDeclaration.class);
        }
    }
}