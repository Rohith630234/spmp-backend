package com.college.project.service;

import com.college.project.dto.DomainRequest;
import com.college.project.exception.BadRequestException;
import com.college.project.exception.ResourceNotFoundException;
import com.college.project.model.Domain;
import com.college.project.repository.DomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing technical domains/categories.
 */
@Service
@RequiredArgsConstructor
public class DomainService {

    private final DomainRepository domainRepository;

    @Transactional(readOnly = true)
    public List<Domain> getAllDomains() {
        return domainRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Domain> getActiveDomains() {
        return domainRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public Domain getDomainById(Long id) {
        return domainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Domain", "id", id));
    }

    @Transactional
    public Domain createDomain(DomainRequest request) {
        if (domainRepository.existsByName(request.getName().trim())) {
            throw new BadRequestException("A domain with name '" + request.getName() + "' already exists.");
        }

        Domain domain = Domain.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .isActive(request.isActive())
                .build();

        return domainRepository.save(domain);
    }

    @Transactional
    public Domain updateDomain(Long id, DomainRequest request) {
        Domain domain = getDomainById(id);

        if (!domain.getName().equalsIgnoreCase(request.getName().trim())
                && domainRepository.existsByName(request.getName().trim())) {
            throw new BadRequestException("A domain with name '" + request.getName() + "' already exists.");
        }

        domain.setName(request.getName().trim());
        domain.setDescription(request.getDescription());
        domain.setActive(request.isActive());

        return domainRepository.save(domain);
    }

    @Transactional
    public Domain toggleDomainStatus(Long id) {
        Domain domain = getDomainById(id);
        domain.setActive(!domain.isActive());
        return domainRepository.save(domain);
    }
}
