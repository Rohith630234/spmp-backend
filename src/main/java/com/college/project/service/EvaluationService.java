package com.college.project.service;

import com.college.project.dto.EvaluationRequest;
import com.college.project.dto.EvaluationResponse;
import com.college.project.dto.ScorecardResponse;
import com.college.project.exception.BadRequestException;
import com.college.project.exception.ResourceNotFoundException;
import com.college.project.model.*;
import com.college.project.repository.EvaluationRepository;
import com.college.project.repository.ProjectRepository;
import com.college.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service managing academic evaluation scoring, rubrics, and grade calculation.
 */
@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public EvaluationResponse submitEvaluation(Long projectId, EvaluationRequest request, String evaluatorEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        User evaluator = userRepository.findByEmail(evaluatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", evaluatorEmail));

        if (evaluator.getRole() != Role.ROLE_GUIDE && evaluator.getRole() != Role.ROLE_ADMIN) {
            throw new AccessDeniedException("Only faculty guides and administrators can evaluate projects.");
        }

        double totalScore = request.getTechnicalScore() + request.getDocumentationScore() + request.getPresentationScore();

        // Check if an evaluation already exists for this stage (to update or create)
        Optional<Evaluation> existingOpt = evaluationRepository.findByProjectIdAndReviewStage(projectId, request.getReviewStage());

        Evaluation evaluation;
        if (existingOpt.isPresent()) {
            evaluation = existingOpt.get();
            evaluation.setEvaluator(evaluator);
            evaluation.setTechnicalScore(request.getTechnicalScore());
            evaluation.setDocumentationScore(request.getDocumentationScore());
            evaluation.setPresentationScore(request.getPresentationScore());
            evaluation.setTotalScore(totalScore);
            evaluation.setRemarks(request.getRemarks());
        } else {
            evaluation = Evaluation.builder()
                    .project(project)
                    .evaluator(evaluator)
                    .reviewStage(request.getReviewStage())
                    .technicalScore(request.getTechnicalScore())
                    .documentationScore(request.getDocumentationScore())
                    .presentationScore(request.getPresentationScore())
                    .totalScore(totalScore)
                    .remarks(request.getRemarks())
                    .build();
        }

        Evaluation saved = evaluationRepository.save(evaluation);

        // If Final Viva evaluated, update project status to COMPLETED
        if (request.getReviewStage() == ReviewStage.FINAL_VIVA) {
            project.setStatus(ProjectStatus.COMPLETED);
            projectRepository.save(project);
        }

        return mapToEvaluationResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EvaluationResponse> getEvaluationsByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return evaluationRepository.findByProjectId(projectId).stream()
                .map(this::mapToEvaluationResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ScorecardResponse getProjectScorecard(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        List<Evaluation> evals = evaluationRepository.findByProjectId(projectId);

        Double r1 = null;
        Double r2 = null;
        Double viva = null;
        double sum = 0.0;
        int count = 0;

        for (Evaluation eval : evals) {
            if (eval.getReviewStage() == ReviewStage.REVIEW_1) {
                r1 = eval.getTotalScore();
            } else if (eval.getReviewStage() == ReviewStage.REVIEW_2) {
                r2 = eval.getTotalScore();
            } else if (eval.getReviewStage() == ReviewStage.FINAL_VIVA) {
                viva = eval.getTotalScore();
            }
            sum += eval.getTotalScore();
            count++;
        }

        Double avg = count > 0 ? Math.round((sum / count) * 10.0) / 10.0 : null;
        String grade = "Pending Evaluation";

        if (avg != null) {
            if (avg >= 90) grade = "A+ (Outstanding / Distinction)";
            else if (avg >= 75) grade = "A (First Class with Distinction)";
            else if (avg >= 60) grade = "B (First Class)";
            else if (avg >= 50) grade = "C (Second Class)";
            else grade = "Needs Improvement / Resubmission";
        }

        List<EvaluationResponse> detailDtos = evals.stream()
                .map(this::mapToEvaluationResponse)
                .collect(Collectors.toList());

        return ScorecardResponse.builder()
                .projectId(project.getId())
                .projectTitle(project.getTitle())
                .studentId(project.getStudent().getId())
                .studentName(project.getStudent().getFullName())
                .studentRollNo(project.getStudent().getRollOrEmpId())
                .guideName(project.getGuide() != null ? project.getGuide().getFullName() : "Not Assigned")
                .review1Total(r1)
                .review2Total(r2)
                .finalVivaTotal(viva)
                .overallAverage(avg)
                .academicGrade(grade)
                .evaluationDetails(detailDtos)
                .build();
    }

    private EvaluationResponse mapToEvaluationResponse(Evaluation eval) {
        return EvaluationResponse.builder()
                .id(eval.getId())
                .projectId(eval.getProject().getId())
                .projectTitle(eval.getProject().getTitle())
                .evaluatorId(eval.getEvaluator().getId())
                .evaluatorName(eval.getEvaluator().getFullName())
                .reviewStage(eval.getReviewStage())
                .technicalScore(eval.getTechnicalScore())
                .documentationScore(eval.getDocumentationScore())
                .presentationScore(eval.getPresentationScore())
                .totalScore(eval.getTotalScore())
                .remarks(eval.getRemarks())
                .evaluatedAt(eval.getEvaluatedAt())
                .build();
    }
}
