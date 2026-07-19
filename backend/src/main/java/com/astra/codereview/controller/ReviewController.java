package com.astra.codereview.controller;

import com.astra.codereview.dto.ReviewDto;
import com.astra.codereview.entity.Review;
import com.astra.codereview.security.AppUserPrincipal;
import com.astra.codereview.service.PdfReportService;
import com.astra.codereview.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final PdfReportService pdfReportService;

    @PostMapping("/project/{projectId}")
    public ResponseEntity<ReviewDto> runReview(@AuthenticationPrincipal AppUserPrincipal principal,
                                                @PathVariable Long projectId) {
        return ResponseEntity.ok(reviewService.runReview(projectId, principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> get(@AuthenticationPrincipal AppUserPrincipal principal,
                                          @PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getOwned(id, principal.getId()));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ReviewDto>> listForProject(@AuthenticationPrincipal AppUserPrincipal principal,
                                                           @PathVariable Long projectId) {
        return ResponseEntity.ok(reviewService.listForProject(projectId, principal.getId()));
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<FileSystemResource> downloadReport(@AuthenticationPrincipal AppUserPrincipal principal,
                                                              @PathVariable Long id) throws Exception {
        Review review = reviewService.getOwnedEntity(id, principal.getId());
        File pdf = pdfReportService.generate(review);

        FileSystemResource resource = new FileSystemResource(pdf);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdf.getName() + "\"")
                .body(resource);
    }
}
