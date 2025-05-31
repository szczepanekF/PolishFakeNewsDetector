package com.pfnd.BusinessLogicService.controller;


import com.pfnd.BusinessLogicService.model.dto.FactCheckRequestDto;
import com.pfnd.BusinessLogicService.model.messages.Response;
import com.pfnd.BusinessLogicService.repository.EvalutationHistoryRepository;
import com.pfnd.BusinessLogicService.service.BusinessLogicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
public class BusinessLogicController {

    private final BusinessLogicService businessLogicService;

    @PostMapping("/check")
    public ResponseEntity<Response<?>> submitTextForEvaluation(@RequestBody FactCheckRequestDto request) {
        return null;
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<Response<?>> getStatus(@PathVariable UUID id) {
        return null;
    }

    @GetMapping("/result/{id}")
    public ResponseEntity<Response<?>> getResult(@PathVariable UUID id) {
        return null;
    }

    @GetMapping("/history")
    public ResponseEntity<Response<?>> getUserHistory(@RequestHeader("Authorization") String token) {

        //TODO get userId from token using UserService decoding token api
        return null;
    }

    // TODO
}
