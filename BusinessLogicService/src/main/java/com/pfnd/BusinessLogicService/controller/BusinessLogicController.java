package com.pfnd.BusinessLogicService.controller;


import com.pfnd.BusinessLogicService.model.dto.EvaluationHistoryDto;
import com.pfnd.BusinessLogicService.model.dto.FactCheckRequestDto;
import com.pfnd.BusinessLogicService.model.messages.Response;
import com.pfnd.BusinessLogicService.service.BusinessLogicService;
import com.pfnd.BusinessLogicService.service.JwtTokenDecoder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
@Slf4j
public class BusinessLogicController {

    private final BusinessLogicService businessLogicService;
    private final JwtTokenDecoder decoder;

    @PostMapping("/evaluate")
    @Operation(summary = "Start evaluating text", description = "Create evaluation history entity and send text " +
            "analysis request to the evaluation module")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully started evaluation and created history record"),
            @ApiResponse(responseCode = "400", description = "Error during decoding JWT token"),
            @ApiResponse(responseCode = "500", description = "Error during request processing")
    })
    public ResponseEntity<Response<?>> submitTextForEvaluation(@RequestBody FactCheckRequestDto request, @RequestHeader(name = "Authorization") String token) {
        EvaluationHistoryDto record;
        try {
            Claims claims = decoder.decode(token);
            int userId = Integer.parseInt(claims.get("userId").toString());
            record = businessLogicService.initiateFactCheck(request, userId);
        } catch (JwtException e) {
            return ResponseEntity.status(400).body(new Response<EvaluationHistoryDto>(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(new Response<EvaluationHistoryDto>(e.getMessage()));
        }
        return ResponseEntity.status(201).body(new Response<>(record));
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<Response<?>> getStatus(@PathVariable long id) {

        return ResponseEntity.ok(new Response<>(businessLogicService.getEvaluationStatus(id)));
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
