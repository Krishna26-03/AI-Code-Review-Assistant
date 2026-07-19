package com.astra.codereview.service;

import com.astra.codereview.entity.Review;
import com.astra.codereview.entity.ReviewFinding;
import com.astra.codereview.entity.Severity;
import com.astra.codereview.util.FileStorageUtil;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfReportService {

    private final FileStorageUtil fileStorageUtil;

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 20, Font.BOLD);
    private static final Font HEADING_FONT = new Font(Font.HELVETICA, 13, Font.BOLD);
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY);

    public File generate(Review review) throws Exception {
        String fileName = "review-" + review.getId() + "-report.pdf";
        File outFile = fileStorageUtil.reportPath(fileName).toFile();

        Document document = new Document(PageSize.A4, 48, 48, 54, 54);
        PdfWriter.getInstance(document, new FileOutputStream(outFile));
        document.open();

        addHeader(document, review);
        addSummary(document, review);
        addFindingsTable(document, review);

        document.close();
        return outFile;
    }

    private void addHeader(Document document, Review review) throws DocumentException {
        Paragraph title = new Paragraph("AI Code Review Report", TITLE_FONT);
        title.setSpacingAfter(4);
        document.add(title);

        Paragraph project = new Paragraph(
                "Project: " + review.getProject().getProjectName(), HEADING_FONT);
        project.setSpacingAfter(2);
        document.add(project);

        String generatedAt = review.getCreatedAt() != null
                ? review.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm"))
                : "";
        Paragraph meta = new Paragraph("Review #" + review.getId() + "  •  Generated " + generatedAt, SMALL_FONT);
        meta.setSpacingAfter(16);
        document.add(meta);
    }

    private void addSummary(Document document, Review review) throws DocumentException {
        double score = review.getReviewScore() == null ? 0 : review.getReviewScore();
        Paragraph scoreLine = new Paragraph(
                String.format("Overall Score: %.1f / 100", score),
                new Font(Font.HELVETICA, 16, Font.BOLD, scoreColor(score)));
        scoreLine.setSpacingAfter(10);
        document.add(scoreLine);

        Paragraph summaryHeading = new Paragraph("Summary", HEADING_FONT);
        summaryHeading.setSpacingAfter(4);
        document.add(summaryHeading);

        Paragraph summaryBody = new Paragraph(
                review.getSummary() == null ? "No summary available." : review.getSummary(), BODY_FONT);
        summaryBody.setSpacingAfter(18);
        document.add(summaryBody);
    }

    private void addFindingsTable(Document document, Review review) throws DocumentException {
        Paragraph findingsHeading = new Paragraph(
                "Findings (" + review.getFindings().size() + ")", HEADING_FONT);
        findingsHeading.setSpacingAfter(6);
        document.add(findingsHeading);

        if (review.getFindings().isEmpty()) {
            document.add(new Paragraph("No findings reported.", BODY_FONT));
            return;
        }

        PdfPTable table = new PdfPTable(new float[]{9, 10, 24, 10, 8, 39});
        table.setWidthPercentage(100);

        addHeaderCell(table, "Severity");
        addHeaderCell(table, "Tool");
        addHeaderCell(table, "Issue");
        addHeaderCell(table, "File");
        addHeaderCell(table, "Line");
        addHeaderCell(table, "Suggestion");

        List<ReviewFinding> sorted = review.getFindings().stream()
                .sorted(Comparator.comparingInt(f -> severityRank(f.getSeverity())))
                .toList();

        for (ReviewFinding f : sorted) {
            addBodyCell(table, f.getSeverity().name(), scoreColor(severityToApproxScore(f.getSeverity())));
            addBodyCell(table, nullToDash(f.getSourceTool()), null);
            addBodyCell(table, nullToDash(f.getIssue()), null);
            addBodyCell(table, nullToDash(f.getFileName()), null);
            addBodyCell(table, f.getLineNumber() == null ? "-" : String.valueOf(f.getLineNumber()), null);
            addBodyCell(table, nullToDash(f.getSuggestion()), null);
        }

        document.add(table);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE)));
        cell.setBackgroundColor(new Color(40, 40, 60));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Color textColor) {
        Font font = new Font(Font.HELVETICA, 8, Font.NORMAL, textColor == null ? Color.DARK_GRAY : textColor);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(4);
        table.addCell(cell);
    }

    private String nullToDash(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private int severityRank(Severity s) {
        return switch (s) {
            case CRITICAL -> 0;
            case HIGH -> 1;
            case MEDIUM -> 2;
            case LOW -> 3;
            case INFO -> 4;
        };
    }

    private double severityToApproxScore(Severity s) {
        return switch (s) {
            case CRITICAL -> 10;
            case HIGH -> 40;
            case MEDIUM -> 65;
            case LOW -> 85;
            case INFO -> 95;
        };
    }

    private Color scoreColor(double score) {
        if (score >= 80) return new Color(34, 139, 34);
        if (score >= 50) return new Color(204, 140, 0);
        return new Color(178, 34, 34);
    }
}
