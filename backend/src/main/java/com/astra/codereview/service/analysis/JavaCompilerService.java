package com.astra.codereview.service.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Compiles the .java files found under a project's source root to .class
 * files using the JDK's in-process compiler (no external process spawned).
 * SpotBugs needs bytecode to run, so this is a prerequisite step for it.
 *
 * Uploaded code with unresolved third-party dependencies will legitimately
 * fail to compile here — that's surfaced back to the caller so the review
 * can note "SpotBugs skipped: could not compile" instead of silently
 * pretending bytecode analysis ran.
 */
@Service
public class JavaCompilerService {

    private static final Logger log = LoggerFactory.getLogger(JavaCompilerService.class);

    public CompileResult compile(Path sourceRoot) throws IOException {
        List<File> javaFiles = Files.walk(sourceRoot)
                .filter(p -> p.toString().endsWith(".java"))
                .map(Path::toFile)
                .collect(Collectors.toList());

        if (javaFiles.isEmpty()) {
            return new CompileResult(null, false, "No .java files found to compile");
        }

        Path classesOut = Files.createTempDirectory("astra-classes-");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return new CompileResult(null, false, "No system Java compiler available (JDK required, not just a JRE)");
        }

        StringWriter diagnosticsOut = new StringWriter();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.getDefault(), null)) {
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(classesOut.toFile()));

            Iterable<? extends JavaFileObject> compilationUnits =
                    fileManager.getJavaFileObjectsFromFiles(javaFiles);

            JavaCompiler.CompilationTask task = compiler.getTask(
                    diagnosticsOut, fileManager, null,
                    List.of("-nowarn", "-Xlint:none"),
                    null, compilationUnits);

            boolean success = task.call();
            if (!success) {
                log.warn("javac reported errors while compiling for SpotBugs: {}", diagnosticsOut);
                return new CompileResult(classesOut, false, diagnosticsOut.toString());
            }
            return new CompileResult(classesOut, true, null);
        }
    }

    public record CompileResult(Path classesDir, boolean success, String diagnostics) {}
}
