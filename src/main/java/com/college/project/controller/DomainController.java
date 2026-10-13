package com.college.project.controller;

import com.college.project.dto.ApiResponse;
import com.college.project.dto.DomainRequest;
import com.college.project.model.Domain;
import com.college.project.service.DomainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing technical domains and specializations.
 */
@RestController
@RequestMapping("/api/domains")
@RequiredArgsConstructor
public class DomainController {

    private final DomainService domainService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Domain>>> getAllDomains(@RequestParam(required = false, defaultValue = "false") boolean onlyActive) {
        List<Domain> domains = onlyActive ? domainService.getActiveDomains() : domainService.getAllDomains();
        return ResponseEntity.ok(ApiResponse.success("Domains retrieved successfully", domains));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Domain>> getDomainById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Domain details", domainService.getDomainById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Domain>> createDomain(@Valid @RequestBody DomainRequest request) {
        Domain created = domainService.createDomain(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Domain created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Domain>> updateDomain(@PathVariable Long id, @Valid @RequestBody DomainRequest request) {
        Domain updated = domainService.updateDomain(id, request);
        return ResponseEntity.ok(ApiResponse.success("Domain updated successfully", updated));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Domain>> toggleDomainStatus(@PathVariable Long id) {
        Domain toggled = domainService.toggleDomainStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Domain status toggled", toggled));
    }
}
