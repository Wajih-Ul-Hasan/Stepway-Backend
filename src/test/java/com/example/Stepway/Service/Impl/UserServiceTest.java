package com.example.Stepway.Service.Impl;

import com.example.Stepway.Domain.Role;
import com.example.Stepway.Domain.User;
import com.example.Stepway.Repository.RoleRepository;
import com.example.Stepway.Repository.UserRepository;
import com.example.Stepway.Service.impl.UserServiceImpl;
import com.example.Stepway.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import javax.persistence.EntityManager;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Test
    void registrationHashesPasswordAndAssignsExistingRole() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        Role role = new Role();
        role.setName("ROLE_STUDENT");
        when(roles.findByName("ROLE_STUDENT")).thenReturn(Optional.of(role));
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserServiceImpl service = new UserServiceImpl(users, new ModelMapper(), roles, mock(EntityManager.class));
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        ReflectionTestUtils.setField(service, "passwordEncoder", encoder);
        UserDto input = UserDto.builder().firstName("Demo").lastName("Student")
                .email("student@example.com").password("a-demo-password").Role("ROLE_STUDENT").build();
        service.createUser(input);
        org.mockito.ArgumentCaptor<User> saved = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertTrue(encoder.matches(input.getPassword(), saved.getValue().getPassword()));
        assertTrue(saved.getValue().getRole().contains(role));
    }
}
