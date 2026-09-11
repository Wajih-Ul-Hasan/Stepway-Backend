package com.example.Stepway.Controller;

import com.example.Stepway.Service.impl.LearningGoalService;
import com.example.Stepway.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/me/goals")
public class LearningGoalController {
    private final LearningGoalService service;
    public LearningGoalController(LearningGoalService service) { this.service = service; }

    @GetMapping
    public List<LearningGoalResponse> list(@AuthenticationPrincipal CustomUserDetails user) {
        return service.list(owner(user));
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public LearningGoalResponse create(@AuthenticationPrincipal CustomUserDetails user,
                                      @Valid @RequestBody LearningGoalRequest input) {
        return service.create(owner(user), input);
    }
    @PutMapping("/{id}")
    public LearningGoalResponse update(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long id,
                                      @Valid @RequestBody LearningGoalRequest input) {
        return service.update(owner(user), id, input);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long id) {
        service.delete(owner(user), id);
    }
    private Long owner(CustomUserDetails user) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return user.getUserId();
    }
}
