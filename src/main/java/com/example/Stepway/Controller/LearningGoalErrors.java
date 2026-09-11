package com.example.Stepway.Controller;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import java.util.Collections;
import java.util.Map;

@RestControllerAdvice(assignableTypes = LearningGoalController.class)
public class LearningGoalErrors {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> status(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Collections.singletonMap("message",
                ex.getReason() == null ? ex.getStatus().getReasonPhrase() : ex.getReason()));
    }
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, String>> invalid(Exception ex) {
        return ResponseEntity.badRequest().body(Collections.singletonMap("message",
                "Check the title, field lengths, status and date format."));
    }
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> conflict(Exception ex) {
        return ResponseEntity.status(409).body(Collections.singletonMap("message",
                "This goal changed during your update. Refresh and try again."));
    }
}
