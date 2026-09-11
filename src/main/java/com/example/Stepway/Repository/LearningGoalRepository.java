package com.example.Stepway.Repository;

import com.example.Stepway.Domain.LearningGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LearningGoalRepository extends JpaRepository<LearningGoal, Long> {
    List<LearningGoal> findByOwnerIdOrderByUpdatedAtDesc(Long ownerId);
    Optional<LearningGoal> findByIdAndOwnerId(Long id, Long ownerId);
}
