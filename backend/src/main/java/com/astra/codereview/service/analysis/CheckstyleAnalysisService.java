package com.astra.codereview.service.analysis;

import com.astra.codereview.dto.RawFinding;
import com.astra.codereview.entity.Severity;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Runs Checkstyle in-process (no CLI, no separate JVM) against a list of
 * .java source files and returns normalized findings. Uses the bundled
 * astra_checks.xml ruleset so it works on any uploaded project without
 * extra configuration files.
 */
@Service
public class CheckstyleAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(CheckstyleAnalysisService.class);

    public List<RawFinding> analyze(List<File> javaFiles) {
        List<RawFinding> findings = new ArrayList<>();
        if (javaFiles.isEmpty()) {
            return findings;
        }

        try {
            Configuration config = ConfigurationLoader.loadConfiguration(
                    new ClassPathResource("checkstyle/astra_checks.xml").getURL().toString(),
                    new PropertiesExpander(new Properties())
            );

            Checker checker = new Checker();
            checker.setModuleClassLoader(Checker.class.getClassLoader());
            checker.configure(config);

            AuditListener collector = new AuditListener() {
                @Override public void auditStarted(AuditEvent event) { }
                @Override public void auditFinished(AuditEvent event) { }
                @Override public void fileStarted(AuditEvent event) { }
                @Override public void fileFinished(AuditEvent event) { }

                @Override
                public void addError(AuditEvent event) {
                    findings.add(new RawFinding(
                            mapSeverity(event.getSeverityLevel()),
                            shortRuleName(event.getSourceName()),
                            event.getMessage(),
                            suggestionFor(event.getSourceName()),
                            new File(event.getFileName()).getName(),
                            event.getLine() == 0 ? null : event.getLine(),
                            "CHECKSTYLE"
                    ));
                }

                @Override
                public void addException(AuditEvent event, Throwable throwable) {
                    log.warn("Checkstyle module raised an exception for {}: {}", event.getFileName(), throwable.getMessage());
                }
            };

            checker.addListener(collector);
            checker.process(javaFiles);
            checker.destroy();
        } catch (Exception e) {
            log.error("Checkstyle analysis failed", e);
            findings.add(new RawFinding(Severity.INFO, "Checkstyle skipped",
                    "Checkstyle could not complete: " + e.getMessage(),
                    "Verify the uploaded files are valid, parseable Java source.",
                    null, null, "CHECKSTYLE"));
        }

        return findings;
    }

    private Severity mapSeverity(SeverityLevel level) {
        return switch (level) {
            case ERROR -> Severity.HIGH;
            case WARNING -> Severity.MEDIUM;
            case INFO -> Severity.LOW;
            default -> Severity.INFO;
        };
    }

    private String shortRuleName(String sourceName) {
        // sourceName looks like "com.puppycrawl.tools.checkstyle.checks.naming.MethodNameCheck"
        int lastDot = sourceName.lastIndexOf('.');
        String simple = lastDot >= 0 ? sourceName.substring(lastDot + 1) : sourceName;
        return simple.replace("Check", "");
    }

    private String suggestionFor(String sourceName) {
        String rule = shortRuleName(sourceName);
        return "Address the '" + rule + "' style/convention violation reported by Checkstyle.";
    }
}
