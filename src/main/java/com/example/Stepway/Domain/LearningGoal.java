package com.example.Stepway.Domain;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/** Personal planning records; completion is self-reported, not a certification. */
@Entity
@Table(name = "learning_goal", indexes = @Index(name = "idx_goal_owner", columnList = "owner_id"))
@Getter @Setter
public class LearningGoal {
    public enum Status { PLANNED, IN_PROGRESS, COMPLETED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    @Column(nullable = false, length = 120)
    private String title;
    @Column(length = 2000)
    private String description;
    @Column(length = 60)
    private String skill;
    @Column(length = 500)
    private String evidenceUrl;
    private LocalDate dueDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Status status = Status.PLANNED;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;
    private Instant completedAt;
    @Version
    private Long version;
}
