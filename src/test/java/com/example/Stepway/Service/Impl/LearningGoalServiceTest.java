package com.example.Stepway.Service.Impl;

import com.example.Stepway.Domain.LearningGoal;
import com.example.Stepway.Repository.LearningGoalRepository;
import com.example.Stepway.Service.impl.LearningGoalService;
import com.example.Stepway.dto.LearningGoalRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LearningGoalServiceTest {
    private final LearningGoalRepository repository = mock(LearningGoalRepository.class);
    private final LearningGoalService service = new LearningGoalService(repository);

    private LearningGoalRequest input(LearningGoal.Status status) {
        LearningGoalRequest input = new LearningGoalRequest();
        input.setTitle(" Build an API ");
        input.setStatus(status);
        input.setSkill("Java");
        input.setEvidenceUrl("https://github.com/example/project");
        return input;
    }

    @Test void createUsesAuthenticatedOwnerAndTrimsInput() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("Build an API", service.create(7L, input(LearningGoal.Status.PLANNED)).getTitle());
        org.mockito.ArgumentCaptor<LearningGoal> record = org.mockito.ArgumentCaptor.forClass(LearningGoal.class);
        verify(repository).save(record.capture());
        assertEquals(Long.valueOf(7), record.getValue().getOwnerId());
        assertNotNull(record.getValue().getCreatedAt());
        assertNull(record.getValue().getCompletedAt());
    }

    @Test void foreignGoalCannotBeReadForUpdateOrDeleted() {
        when(repository.findByIdAndOwnerId(9L, 7L)).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.update(7L, 9L, input(LearningGoal.Status.COMPLETED))).getStatus().value());
        assertThrows(ResponseStatusException.class, () -> service.delete(7L, 9L));
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any(LearningGoal.class));
    }

    @Test void completionRecordsTimestampAndReopeningClearsIt() {
        LearningGoal goal = new LearningGoal();
        when(repository.findByIdAndOwnerId(9L, 7L)).thenReturn(Optional.of(goal));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        service.update(7L, 9L, input(LearningGoal.Status.COMPLETED));
        assertNotNull(goal.getCompletedAt());
        java.time.Instant first = goal.getCompletedAt();
        service.update(7L, 9L, input(LearningGoal.Status.COMPLETED));
        assertEquals(first, goal.getCompletedAt());
        service.update(7L, 9L, input(LearningGoal.Status.IN_PROGRESS));
        assertNull(goal.getCompletedAt());
    }

    @Test void rejectsExecutableAndCredentialBearingEvidenceLinks() {
        for (String url : new String[]{"javascript:alert(1)", "file:///etc/passwd", "https://user:pass@example.com"}) {
            LearningGoalRequest request = input(LearningGoal.Status.PLANNED);
            request.setEvidenceUrl(url);
            assertEquals(400, assertThrows(ResponseStatusException.class,
                    () -> service.create(7L, request)).getStatus().value());
        }
        verify(repository, never()).save(any());
    }
}
