package com.example.Stepway.dto;

import com.example.Stepway.Domain.LearningGoal.Status;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Getter @Setter
public class LearningGoalRequest {
    @NotBlank @Size(max = 120)
    private String title;
    @Size(max = 2000)
    private String description;
    @Size(max = 60)
    private String skill;
    @Size(max = 500)
    private String evidenceUrl;
    private LocalDate dueDate;
    @NotNull
    private Status status;
}
