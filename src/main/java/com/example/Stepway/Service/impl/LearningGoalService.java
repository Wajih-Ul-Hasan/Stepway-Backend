package com.example.Stepway.Service.impl;

import com.example.Stepway.Domain.LearningGoal;
import com.example.Stepway.Repository.LearningGoalRepository;
import com.example.Stepway.dto.LearningGoalRequest;
import com.example.Stepway.dto.LearningGoalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LearningGoalService {
    private final LearningGoalRepository repository;
    public LearningGoalService(LearningGoalRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<LearningGoalResponse> list(Long ownerId) {
        return repository.findByOwnerIdOrderByUpdatedAtDesc(ownerId).stream()
                .map(LearningGoalResponse::new).collect(Collectors.toList());
    }

    public LearningGoalResponse create(Long ownerId, LearningGoalRequest input) {
        LearningGoal goal = new LearningGoal();
        goal.setOwnerId(ownerId);
        goal.setCreatedAt(Instant.now());
        apply(goal, input);
        return new LearningGoalResponse(repository.save(goal));
    }

    public LearningGoalResponse update(Long ownerId, Long id, LearningGoalRequest input) {
        LearningGoal goal = owned(ownerId, id);
        apply(goal, input);
        return new LearningGoalResponse(repository.save(goal));
    }

    public void delete(Long ownerId, Long id) { repository.delete(owned(ownerId, id)); }

    private LearningGoal owned(Long ownerId, Long id) {
        // Same response for absent and foreign records; never disclose another user's goals.
        return repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Goal not found"));
    }

    private void apply(LearningGoal goal, LearningGoalRequest input) {
        String evidence = clean(input.getEvidenceUrl());
        if (!evidence.isEmpty()) {
            try {
                URI url = new URI(evidence);
                if (!("https".equalsIgnoreCase(url.getScheme()) || "http".equalsIgnoreCase(url.getScheme()))
                        || url.getHost() == null || url.getUserInfo() != null) {
                    throw new IllegalArgumentException();
                }
            } catch (Exception ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Evidence must be an HTTP or HTTPS URL");
            }
        }
        goal.setTitle(input.getTitle().trim());
        goal.setDescription(clean(input.getDescription()));
        goal.setSkill(clean(input.getSkill()));
        goal.setEvidenceUrl(evidence);
        goal.setDueDate(input.getDueDate());
        if (input.getStatus() == LearningGoal.Status.COMPLETED) {
            if (goal.getCompletedAt() == null) goal.setCompletedAt(Instant.now());
        } else {
            goal.setCompletedAt(null);
        }
        goal.setStatus(input.getStatus());
        goal.setUpdatedAt(Instant.now());
    }
    private String clean(String value) { return value == null ? "" : value.trim(); }
}
