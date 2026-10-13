package com.college.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Evaluation Entity representing academic scoring across Review 1, Review 2, and Final Viva.
 */
@Entity
@Table(name = "evaluations",
       uniqueConstraints = @UniqueConstraint(name = "uk_project_review_stage", columnNames = {"project_id", "review_stage"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_stage", nullable = false, length = 20)
    private ReviewStage reviewStage;

    @Column(name = "technical_score", nullable = false)
    private Double technicalScore;

    @Column(name = "documentation_score", nullable = false)
    private Double documentationScore;

    @Column(name = "presentation_score", nullable = false)
    private Double presentationScore;

    @Column(name = "total_score", nullable = false)
    private Double totalScore;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "evaluated_at", nullable = false, updatable = false)
    private LocalDateTime evaluatedAt;

    @PrePersist
    protected void onCreate() {
        this.evaluatedAt = LocalDateTime.now();
        if (this.totalScore == null) {
            this.totalScore = (this.technicalScore != null ? this.technicalScore : 0.0)
                    + (this.documentationScore != null ? this.documentationScore : 0.0)
                    + (this.presentationScore != null ? this.presentationScore : 0.0);
        }
    }
}
