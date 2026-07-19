package com.astra.codereview.service.analysis;

import com.astra.codereview.dto.RawFinding;
import com.astra.codereview.entity.Severity;
import edu.umd.cs.findbugs.*;
import edu.umd.cs.findbugs.config.UserPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs SpotBugs against compiled .class bytecode. Unlike Checkstyle/PMD
 * (which operate on source text), SpotBugs requires a successful compile
 * first — see JavaCompilerService. If the uploaded project can't be
 * compiled in isolation (missing third-party dependencies, partial
 * snippets, etc.) this engine is skipped and that's reported honestly
 * as a single INFO finding rather than failing the whole review.
 */
@Service
public class SpotBugsAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(SpotBugsAnalysisService.class);

    private final JavaCompilerService javaCompilerService;

    public SpotBugsAnalysisService(JavaCompilerService javaCompilerService) {
        this.javaCompilerService = javaCompilerService;
    }

    public List<RawFinding> analyze(Path sourceRoot) {
        List<RawFinding> findings = new ArrayList<>();

        JavaCompilerService.CompileResult compileResult;
        try {
            compileResult = javaCompilerService.compile(sourceRoot);
        } catch (Exception e) {
            log.error("Could not attempt compilation for SpotBugs", e);
            findings.add(skippedFinding("Could not read/compile source: " + e.getMessage()));
            return findings;
        }

        if (!compileResult.success()) {
            String reason = compileResult.diagnostics() == null
                    ? "No compilable .java files were found."
                    : truncate(compileResult.diagnostics(), 600);
            findings.add(skippedFinding(
                    "SpotBugs requires successfully compiled bytecode. Compilation did not succeed "
                            + "(likely missing third-party dependencies for an isolated upload): " + reason));
            return findings;
        }

        try {
            findings.addAll(runSpotBugs(compileResult.classesDir(), sourceRoot));
        } catch (Throwable t) {
            // SpotBugs engine setup is sensitive to classpath/plugin discovery; degrade gracefully
            // rather than failing the whole review if it can't initialize in this environment.
            t.printStackTrace();
            log.error("SpotBugs engine failed to run", t);
            findings.add(skippedFinding("SpotBugs engine error: " + t.getMessage()));
        }

        return findings;
    }

    private List<RawFinding> runSpotBugs(Path classesDir, Path sourceRoot) throws Exception {
        List<RawFinding> findings = new ArrayList<>();

        Project project = new Project();
        project.addFile(classesDir.toAbsolutePath().toString());

        BugCollectingReporter reporter = new BugCollectingReporter();
        reporter.setPriorityThreshold(Priorities.LOW_PRIORITY);

        try (FindBugs2 findBugs = new FindBugs2()) {
            findBugs.setProject(project);
            findBugs.setBugReporter(reporter);
            findBugs.setDetectorFactoryCollection(DetectorFactoryCollection.instance());
            findBugs.setUserPreferences(UserPreferences.createDefaultUserPreferences());
            findBugs.setNoClassOk(true);
            findBugs.execute();
        }

        for (BugInstance bug : reporter.getCollectedBugs()) {
            findings.add(new RawFinding(
                    mapSeverity(bug.getPriority()),
                    bug.getBugPattern() != null ? bug.getBugPattern().getShortDescription() : bug.getType(),
                    bug.getMessageWithoutPrefix(),
                    "Review the SpotBugs pattern '" + bug.getType() + "' documentation for the recommended remediation.",
                    bug.getPrimarySourceLineAnnotation() != null
                            ? bug.getPrimarySourceLineAnnotation().getSourceFile() : null,
                    bug.getPrimarySourceLineAnnotation() != null
                            ? bug.getPrimarySourceLineAnnotation().getStartLine() : null,
                    "SPOTBUGS"
            ));
        }

        return findings;
    }

    private Severity mapSeverity(int priority) {
        return switch (priority) {
            case Priorities.HIGH_PRIORITY -> Severity.HIGH;
            case Priorities.NORMAL_PRIORITY -> Severity.MEDIUM;
            case Priorities.LOW_PRIORITY -> Severity.LOW;
            default -> Severity.INFO;
        };
    }

    private RawFinding skippedFinding(String explanation) {
        return new RawFinding(Severity.INFO, "SpotBugs skipped", explanation,
                "Provide a compilable project (with dependencies) for full bytecode-level analysis.",
                null, null, "SPOTBUGS");
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    /** Minimal BugReporter that just accumulates BugInstances in memory. */
    private static class BugCollectingReporter extends AbstractBugReporter {
        private final List<BugInstance> collected = new ArrayList<>();

        List<BugInstance> getCollectedBugs() {
            return collected;
        }

        @Override
        protected void doReportBug(BugInstance bugInstance) {
            collected.add(bugInstance);
        }

        @Override
        public void reportQueuedErrors() {
            // no-op: analysis errors are surfaced via logs, not treated as findings
        }

        @Override
        public void observeClass(edu.umd.cs.findbugs.classfile.ClassDescriptor classDescriptor) {
            // no-op
        }

        @Override
        public void reportMissingClass(String classDescriptor) {
        }

        @Override
        public void reportAnalysisError(AnalysisError analysisError) {
        }

        @Override
        public void finish() {

        }

        @Override
        public BugCollection getBugCollection() {
            return null;
        }
    }
}
