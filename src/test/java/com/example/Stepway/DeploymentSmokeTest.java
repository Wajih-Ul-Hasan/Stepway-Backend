package com.example.Stepway;

import com.example.Stepway.Configuration.DemoBootstrap;
import com.example.Stepway.Repository.RoleRepository;
import com.example.Stepway.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "RUN_DATABASE_TESTS", matches = "true")
class DeploymentSmokeTest {
    @Autowired MockMvc mvc;
    @Autowired RoleRepository roles;
    @Autowired UserRepository users;
    @Autowired DemoBootstrap bootstrap;

    @Test void freshDatabaseBootstrapsAndAdminCanLogin() throws Exception {
        assertTrue(roles.findByName("ROLE_ADMIN").isPresent());
        long count = users.count();
        String hash = users.findByEmail(System.getenv("DEMO_ADMIN_EMAIL")).getPassword();
        bootstrap.run(null);
        assertEquals(count, users.count());
        assertEquals(hash, users.findByEmail(System.getenv("DEMO_ADMIN_EMAIL")).getPassword());
        mvc.perform(get("/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(post("/api/login").contentType("application/json").content(
                "{\"email\":\"" + System.getenv("DEMO_ADMIN_EMAIL") + "\",\"password\":\""
                        + System.getenv("DEMO_ADMIN_PASSWORD") + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isString());
        // Native queries must work on Linux's case-sensitive MySQL tables.
        assertNotNull(users.countUsersWithRoleStudent());
        assertNotNull(users.countUsersWithRoleTeacher());
        assertEquals("Demo", users.getLoginName(users.findByEmail(System.getenv("DEMO_ADMIN_EMAIL")).getId()));
    }
}
