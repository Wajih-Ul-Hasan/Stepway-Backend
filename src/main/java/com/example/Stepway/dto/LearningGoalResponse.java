package com.example.Stepway.dto;

import com.example.Stepway.Domain.LearningGoal;
import lombok.Getter;
import java.time.Instant;
import java.time.LocalDate;

@Getter
public class LearningGoalResponse {
    private final Long id;
    private final String title, description, skill, evidenceUrl;
    private final LocalDate dueDate;
    private final LearningGoal.Status status;
    private final Instant createdAt, updatedAt, completedAt;

    public LearningGoalResponse(LearningGoal goal) {
        id = goal.getId(); title = goal.getTitle(); description = goal.getDescription();
        skill = goal.getSkill(); evidenceUrl = goal.getEvidenceUrl(); dueDate = goal.getDueDate();
        status = goal.getStatus(); createdAt = goal.getCreatedAt();
        updatedAt = goal.getUpdatedAt(); completedAt = goal.getCompletedAt();
    }
}
