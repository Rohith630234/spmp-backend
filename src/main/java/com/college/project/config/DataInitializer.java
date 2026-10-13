package com.college.project.config;

import com.college.project.model.*;
import com.college.project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Startup Data Initializer that automatically seeds default Admin, Guides, Students,
 * Domains, Projects, Milestones, and Evaluations if the database is currently empty.
 *
 * Password for seeded accounts: Password@123
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DomainRepository domainRepository;
    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;
    private final EvaluationRepository evaluationRepository;
    private final ProjectFeedbackRepository feedbackRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedDomainsIfEmpty();
        seedUsersIfEmpty();
        seedProjectsAndMilestonesIfEmpty();
    }

    private void seedDomainsIfEmpty() {
        if (domainRepository.count() == 0) {
            log.info("Seeding initial technical domains...");
            domainRepository.saveAll(List.of(
                    Domain.builder().name("Artificial Intelligence & Machine Learning").description("Deep learning, computer vision, and NLP systems").build(),
                    Domain.builder().name("Full-Stack Web Development").description("Modern web architectures using Spring Boot and React").build(),
                    Domain.builder().name("Cloud Computing & DevOps").description("Kubernetes, Docker, CI/CD, and serverless computing").build(),
                    Domain.builder().name("Cyber Security & Forensics").description("Network penetration testing and cryptographic protocols").build(),
                    Domain.builder().name("Internet of Things (IoT) & Embedded").description("Smart sensors, microcontrollers, and real-time telemetry").build(),
                    Domain.builder().name("Mobile Application Development").description("Cross-platform mobile apps for Android and iOS").build()
            ));
            log.info("Domains seeded successfully.");
        }
    }

    private void seedUsersIfEmpty() {
        if (userRepository.count() == 0) {
            log.info("Seeding initial portal users...");
            String defaultPassword = passwordEncoder.encode("Password@123");

            // 1. Admin
            User admin = User.builder()
                    .fullName("Dr. Ramesh Sharma")
                    .email("admin@college.edu")
                    .password(defaultPassword)
                    .role(Role.ROLE_ADMIN)
                    .department("Computer Science & Engineering")
                    .rollOrEmpId("EMP-ADM-001")
                    .phone("9876543210")
                    .avatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=Ramesh")
                    .isActive(true)
                    .build();

            // 2. Guide 1
            User guide1 = User.builder()
                    .fullName("Prof. Arvind Kulkarni")
                    .email("guide.arvind@college.edu")
                    .password(defaultPassword)
                    .role(Role.ROLE_GUIDE)
                    .department("Computer Science & Engineering")
                    .rollOrEmpId("EMP-FAC-101")
                    .phone("9811122233")
                    .avatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=Arvind")
                    .isActive(true)
                    .build();

            // 3. Guide 2
            User guide2 = User.builder()
                    .fullName("Prof. Meera Nambiar")
                    .email("guide.meera@college.edu")
                    .password(defaultPassword)
                    .role(Role.ROLE_GUIDE)
                    .department("Computer Science & Engineering")
                    .rollOrEmpId("EMP-FAC-102")
                    .phone("9822233344")
                    .avatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=Meera")
                    .isActive(true)
                    .build();

            // 4. Student 1
            User student1 = User.builder()
                    .fullName("Aditya Joshi")
                    .email("student.aditya@college.edu")
                    .password(defaultPassword)
                    .role(Role.ROLE_STUDENT)
                    .department("Computer Science & Engineering")
                    .rollOrEmpId("CS2022-045")
                    .phone("9711223344")
                    .avatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=Aditya")
                    .isActive(true)
                    .build();

            // 5. Student 2
            User student2 = User.builder()
                    .fullName("Priya Nair")
                    .email("student.priya@college.edu")
                    .password(defaultPassword)
                    .role(Role.ROLE_STUDENT)
                    .department("Computer Science & Engineering")
                    .rollOrEmpId("CS2022-089")
                    .phone("9722334455")
                    .avatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=Priya")
                    .isActive(true)
                    .build();

            userRepository.saveAll(List.of(admin, guide1, guide2, student1, student2));
            log.info("Default users seeded successfully (Password: Password@123).");
        }
    }

    private void seedProjectsAndMilestonesIfEmpty() {
        if (projectRepository.count() == 0) {
            log.info("Seeding initial projects, milestones, evaluations, and discussion logs...");

            List<Domain> domains = domainRepository.findAll();
            User student1 = userRepository.findByEmail("student.aditya@college.edu").orElse(null);
            User student2 = userRepository.findByEmail("student.priya@college.edu").orElse(null);
            User guide1 = userRepository.findByEmail("guide.arvind@college.edu").orElse(null);
            User guide2 = userRepository.findByEmail("guide.meera@college.edu").orElse(null);

            if (student1 == null || student2 == null || guide1 == null || guide2 == null || domains.isEmpty()) {
                return;
            }

            Domain aiDomain = domains.get(0);
            Domain webDomain = domains.size() > 1 ? domains.get(1) : aiDomain;
            Domain iotDomain = domains.size() > 4 ? domains.get(4) : aiDomain;

            // Project 1
            Project p1 = Project.builder()
                    .title("AI-Powered Brain Tumor Detection using MRI Images")
                    .description("Deep convolutional neural network architecture (ResNet-50) for classifying MRI scans into glioma, meningioma, and pituitary tumors with Grad-CAM visualization.")
                    .domain(aiDomain)
                    .student(student1)
                    .guide(guide1)
                    .academicYear("2025-2026")
                    .status(ProjectStatus.IN_PROGRESS)
                    .githubUrl("https://github.com/aditya-joshi/brain-tumor-detection")
                    .liveDemoUrl("https://demo-mri-tumor.vercel.app")
                    .build();
            p1 = projectRepository.save(p1);

            // Milestones for P1
            Milestone m1 = Milestone.builder().project(p1).title("Literature Survey & Dataset Acquisition").dueDate(LocalDate.now().minusDays(30)).status(MilestoneStatus.COMPLETED).completionDate(LocalDate.now().minusDays(32)).build();
            Milestone m2 = Milestone.builder().project(p1).title("Data Preprocessing & Augmentation").dueDate(LocalDate.now().minusDays(10)).status(MilestoneStatus.COMPLETED).completionDate(LocalDate.now().minusDays(12)).build();
            Milestone m3 = Milestone.builder().project(p1).title("Model Training & Hyperparameter Tuning").dueDate(LocalDate.now().plusDays(25)).status(MilestoneStatus.IN_PROGRESS).build();
            Milestone m4 = Milestone.builder().project(p1).title("Web UI Integration & Grad-CAM Heatmaps").dueDate(LocalDate.now().plusDays(55)).status(MilestoneStatus.PENDING).build();
            milestoneRepository.saveAll(List.of(m1, m2, m3, m4));

            // Evaluations for P1
            Evaluation eval1 = Evaluation.builder()
                    .project(p1)
                    .evaluator(guide1)
                    .reviewStage(ReviewStage.REVIEW_1)
                    .technicalScore(27.5)
                    .documentationScore(26.0)
                    .presentationScore(36.0)
                    .totalScore(89.5)
                    .remarks("Excellent depth in literature review. Encouraged to benchmark against Vision Transformers.")
                    .build();
            evaluationRepository.save(eval1);

            // Feedback for P1
            ProjectFeedback fb1 = ProjectFeedback.builder()
                    .project(p1)
                    .author(guide1)
                    .feedbackText("Aditya, please verify if your ResNet-50 weights are pretrained on ImageNet.")
                    .build();
            ProjectFeedback fb2 = ProjectFeedback.builder()
                    .project(p1)
                    .author(student1)
                    .feedbackText("Prof. Arvind, yes, we utilized transfer learning with ImageNet weights and fine-tuned the top 4 layers.")
                    .build();
            feedbackRepository.saveAll(List.of(fb1, fb2));

            // Project 2
            Project p2 = Project.builder()
                    .title("Smart Campus Energy Monitoring with IoT Sensors")
                    .description("An end-to-end telemetry system utilizing ESP32 microcontrollers and MQTT broker to track lab electricity consumption, detect anomalies, and alert staff.")
                    .domain(iotDomain)
                    .student(student2)
                    .guide(guide2)
                    .academicYear("2025-2026")
                    .status(ProjectStatus.APPROVED)
                    .githubUrl("https://github.com/priya-nair/smart-campus-energy")
                    .build();
            p2 = projectRepository.save(p2);

            Milestone m5 = Milestone.builder().project(p2).title("Hardware Sourcing & Schematics").dueDate(LocalDate.now().minusDays(15)).status(MilestoneStatus.COMPLETED).completionDate(LocalDate.now().minusDays(17)).build();
            Milestone m6 = Milestone.builder().project(p2).title("MQTT Broker & Cloud Gateway Setup").dueDate(LocalDate.now().plusDays(30)).status(MilestoneStatus.PENDING).build();
            milestoneRepository.saveAll(List.of(m5, m6));

            // Project 3
            Project p3 = Project.builder()
                    .title("Microservices-Based College Placement Portal")
                    .description("Spring Boot and React portal for student eligibility filtering, interview slot scheduling, and corporate hiring analytics.")
                    .domain(webDomain)
                    .student(student1)
                    .guide(guide2)
                    .academicYear("2025-2026")
                    .status(ProjectStatus.COMPLETED)
                    .githubUrl("https://github.com/aditya-joshi/placement-microservices")
                    .liveDemoUrl("https://placement-portal-demo.onrender.com")
                    .build();
            p3 = projectRepository.save(p3);

            Milestone m7 = Milestone.builder().project(p3).title("System Requirements Specification (SRS)").dueDate(LocalDate.now().minusDays(45)).status(MilestoneStatus.COMPLETED).completionDate(LocalDate.now().minusDays(48)).build();
            Milestone m8 = Milestone.builder().project(p3).title("Final System Testing & Deployment").dueDate(LocalDate.now().minusDays(10)).status(MilestoneStatus.COMPLETED).completionDate(LocalDate.now().minusDays(11)).build();
            milestoneRepository.saveAll(List.of(m7, m8));

            Evaluation eval2 = Evaluation.builder()
                    .project(p3)
                    .evaluator(guide2)
                    .reviewStage(ReviewStage.FINAL_VIVA)
                    .technicalScore(29.0)
                    .documentationScore(28.5)
                    .presentationScore(38.5)
                    .totalScore(96.0)
                    .remarks("Outstanding demonstration! Architecture is clean and well-decoupled.")
                    .build();
            evaluationRepository.save(eval2);

            log.info("Sample projects, milestones, and evaluations seeded successfully.");
        }
    }
}
