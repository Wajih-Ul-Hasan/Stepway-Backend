package com.example.Stepway;

import com.example.Stepway.Domain.LearningGoal;
import com.example.Stepway.Repository.LearningGoalRepository;
import com.example.Stepway.Service.impl.LearningGoalService;
import com.example.Stepway.dto.LearningGoalRequest;
import com.example.Stepway.dto.LearningGoalResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import javax.persistence.EntityManager;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {"spring.liquibase.enabled=false",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "spring.datasource.password=test"})
@Import(LearningGoalService.class)
class LearningGoalPersistenceTest {
    @Autowired LearningGoalService service;
    @Autowired LearningGoalRepository repository;
    @Autowired EntityManager entities;

    @Test void persistsGoalsAndScopesQueriesToOwner() {
        LearningGoalRequest request = new LearningGoalRequest();
        request.setTitle("Document architecture decisions");
        request.setStatus(LearningGoal.Status.PLANNED);
        LearningGoalResponse created = service.create(7L, request);
        entities.flush(); entities.clear();
        assertEquals(1, service.list(7L).size());
        assertTrue(service.list(8L).isEmpty());
        assertFalse(repository.findByIdAndOwnerId(created.getId(), 8L).isPresent());
        request.setStatus(LearningGoal.Status.COMPLETED);
        service.update(7L, created.getId(), request);
        entities.flush(); entities.clear();
        assertNotNull(service.list(7L).get(0).getCompletedAt());
        service.delete(7L, created.getId());
        entities.flush();
        assertTrue(service.list(7L).isEmpty());
    }
}
