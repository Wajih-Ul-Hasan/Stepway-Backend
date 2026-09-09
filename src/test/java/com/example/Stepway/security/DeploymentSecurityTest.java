package com.example.Stepway.security;

import com.example.Stepway.Configuration.CorsConfig;
import com.example.Stepway.Controller.UserController;
import com.example.Stepway.Service.impl.MyUserDetailService;
import com.example.Stepway.Service.impl.UserServiceImpl;
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

@WebMvcTest(UserController.class)
@Import({SecurityConfigure.class, CorsConfig.class, JwtRequestFilter.class})
@TestPropertySource(properties = "app.cors.allowed-origins=https://demo.example.com")
class DeploymentSecurityTest {
    @Autowired MockMvc mvc;
    @MockBean UserServiceImpl users;
    @MockBean MyUserDetailService details;
    @MockBean JwtService jwt;

    private String registration(String role) {
        return "{\"firstName\":\"Demo\",\"lastName\":\"User\",\"email\":\"demo@example.com\","
                + "\"password\":\"demo-password\",\"role\":\"" + role + "\"}";
    }

    @Test void anonymousCannotRegisterAdmin() throws Exception {
        mvc.perform(post("/api/user").contentType("application/json").content(registration("ROLE_ADMIN")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(users);
    }

    @Test void anonymousCanRegisterStudent() throws Exception {
        mvc.perform(post("/api/user").contentType("application/json").content(registration("ROLE_STUDENT")))
                .andExpect(status().isOk());
        verify(users).createUser(any());
    }

    @Test void adminCanRegisterTeacher() throws Exception {
        mvc.perform(post("/api/user").with(user("admin").roles("ADMIN"))
                .contentType("application/json").content(registration("ROLE_TEACHER")))
                .andExpect(status().isOk());
        verify(users).createUser(any());
    }

    @Test void studentCannotModifyUsers() throws Exception {
        mvc.perform(delete("/api/deleteUser/1").with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(users);
    }

    @Test void allowsConfiguredOriginPreflight() throws Exception {
        mvc.perform(options("/api/user").header("Origin", "https://demo.example.com")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type,authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://demo.example.com"));
    }

    @Test void rejectsUnconfiguredOrigin() throws Exception {
        mvc.perform(options("/api/user").header("Origin", "https://untrusted.example.com")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
