package com.astra.codereview.service.analysis;

import com.astra.codereview.dto.RawFinding;
import com.astra.codereview.entity.Severity;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.rule.RulePriority;
import net.sourceforge.pmd.reporting.RuleViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs PMD in-process against the uploaded project's source root using a
 * curated set of standard rule categories (best practices, error-prone,
 * design, and a couple of high-signal performance checks).
 */
@Service
public class PmdAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(PmdAnalysisService.class);

    private static final List<String> RULESETS = List.of(
            "category/java/bestpractices.xml",
            "category/java/errorprone.xml",
            "category/java/design.xml",
            "category/java/performance.xml"
    );

    public List<RawFinding> analyze(Path sourceRoot) {
        List<RawFinding> findings = new ArrayList<>();

        PMDConfiguration config = new PMDConfiguration();
        config.addInputPath(sourceRoot);
        RULESETS.forEach(config::addRuleSet);
        config.setIgnoreIncrementalAnalysis(true);
        config.setThreads(2);

        try (PmdAnalysis pmd = PmdAnalysis.create(config)) {
            var report = pmd.performAnalysisAndCollectReport();

            for (RuleViolation violation : report.getViolations()) {
                findings.add(new RawFinding(
                        mapSeverity(violation.getRule().getPriority()),
                        violation.getRule().getName(),
                        violation.getDescription(),
                        "Follow PMD's '" + violation.getRule().getName() + "' guidance: " + ruleUrlHint(violation.getRule().getName()),
                        Path.of(violation.getFileId().getAbsolutePath()).getFileName().toString(),
                        violation.getBeginLine(),
                        "PMD"
                ));
            }

            report.getProcessingErrors().forEach(err ->
                    log.warn("PMD processing error on {}: {}", err.getFileId().getFileName(), err.getMsg()));

        } catch (Exception e) {
            log.error("PMD analysis failed", e);
            findings.add(new RawFinding(Severity.INFO, "PMD skipped",
                    "PMD could not complete: " + e.getMessage(),
                    "Verify the uploaded files are valid, parseable Java source.",
                    null, null, "PMD"));
        }

        return findings;
    }

    private Severity mapSeverity(RulePriority priority) {
        // PMD priority 1 = highest severity, 5 = lowest
        return switch (priority.getPriority()) {
            case 1 -> Severity.CRITICAL;
            case 2 -> Severity.HIGH;
            case 3 -> Severity.MEDIUM;
            case 4 -> Severity.LOW;
            default -> Severity.INFO;
        };
    }

    private String ruleUrlHint(String ruleName) {
        return "see PMD Java rule docs for \"" + ruleName + "\" for the recommended fix pattern.";
    }
}
