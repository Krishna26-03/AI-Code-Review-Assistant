package com.astra.codereview.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review_findings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Column(nullable = false, length = 300)
    private String issue;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String suggestion;

    @Column(name = "file_name", length = 300)
    private String fileName;

    @Column(name = "line_number")
    private Integer lineNumber;

    // Which engine produced this: CHECKSTYLE / PMD / SPOTBUGS / AI
    @Column(name = "source_tool", length = 30)
    private String sourceTool;
}
