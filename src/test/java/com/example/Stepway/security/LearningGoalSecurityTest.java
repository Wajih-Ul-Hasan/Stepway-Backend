package com.example.Stepway.security;

import com.example.Stepway.Configuration.CorsConfig;
import com.example.Stepway.Controller.LearningGoalController;
import com.example.Stepway.Controller.LearningGoalErrors;
import com.example.Stepway.Domain.User;
import com.example.Stepway.Service.impl.LearningGoalService;
import com.example.Stepway.Service.impl.MyUserDetailService;
import com.example.Stepway.dto.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LearningGoalController.class)
@Import({SecurityConfigure.class, CorsConfig.class, JwtRequestFilter.class, LearningGoalErrors.class})
@TestPropertySource(properties = "app.cors.allowed-origins=https://demo.example.com")
class LearningGoalSecurityTest {
    @Autowired MockMvc mvc;
    @MockBean LearningGoalService service;
    @MockBean MyUserDetailService details;
    @MockBean JwtService jwt;

    private CustomUserDetails student() {
        User user = new User(); user.setId(7L); user.setEmail("student@example.com");
        return new CustomUserDetails(user);
    }

    @Test void anonymousCannotReadGoals() throws Exception {
        mvc.perform(get("/api/me/goals")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test void userCanCreateOwnGoalAndCannotSupplyOwner() throws Exception {
        mvc.perform(post("/api/me/goals").with(user(student())).contentType("application/json")
                .content("{\"title\":\"Build a portfolio\",\"status\":\"PLANNED\",\"ownerId\":99}"))
                .andExpect(status().isCreated());
        verify(service).create(eq(7L), any());
    }

    @Test void rejectsBlankTitleAndInvalidStatus() throws Exception {
        mvc.perform(post("/api/me/goals").with(user(student())).contentType("application/json")
                .content("{\"title\":\" \",\"status\":\"PLANNED\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/me/goals").with(user(student())).contentType("application/json")
                .content("{\"title\":\"Goal\",\"status\":\"INVALID\"}")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
