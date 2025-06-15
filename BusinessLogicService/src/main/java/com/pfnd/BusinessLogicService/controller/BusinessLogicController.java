package com.pfnd.BusinessLogicService.controller;


import com.pfnd.BusinessLogicService.model.dto.*;
import com.pfnd.BusinessLogicService.model.messages.Response;
import com.pfnd.BusinessLogicService.service.BusinessLogicService;
import com.pfnd.BusinessLogicService.service.JwtTokenDecoder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @Operation(summary = "Get evaluation status",
            description = "Retrieve current status of fact-check evaluation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved evaluation status"),
            @ApiResponse(responseCode = "404", description = "Evaluation record not found"),
            @ApiResponse(responseCode = "500", description = "Error during status retrieval")
    })
    public ResponseEntity<Response<?>> getStatus(
            @PathVariable @Parameter(description = "Evaluation ID") long id) {

        log.info("Retrieving status for evaluation ID: {}", id);

        try {
            FactCheckResultDto status = businessLogicService.getEvaluationStatus(id);
            return ResponseEntity.ok(new Response<>(status));
        } catch (RuntimeException e) {
            log.error("Error retrieving status for evaluation ID {}: {}", id, e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404)
                        .body(new Response<>("Evaluation record not found"));
            }
            return ResponseEntity.status(500)
                    .body(new Response<>("Error retrieving status: " + e.getMessage()));
        }
    }

    @GetMapping("/result/{id}")
    @Operation(summary = "Get evaluation result",
            description = "Retrieve complete fact-check result including sources and analysis")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved evaluation result"),
            @ApiResponse(responseCode = "404", description = "Evaluation record not found"),
            @ApiResponse(responseCode = "202", description = "Evaluation still in progress"),
            @ApiResponse(responseCode = "500", description = "Error during result retrieval")
    })
    public ResponseEntity<Response<?>> getResult(
            @PathVariable @Parameter(description = "Evaluation ID") int id) {

        log.info("Retrieving result for evaluation ID: {}", id);

        try {
            FactCheckResultDto result = businessLogicService.getEvaluationResult(id);
            return ResponseEntity.ok(new Response<>(result));
        } catch (RuntimeException e) {
            log.error("Error retrieving result for evaluation ID {}: {}", id, e.getMessage());

            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404)
                        .body(new Response<>("Evaluation record not found"));
            } else if (e.getMessage().contains("not completed")) {
                return ResponseEntity.status(202)
                        .body(new Response<>("Evaluation still in progress"));
            }

            return ResponseEntity.status(500)
                    .body(new Response<>("Error retrieving result: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    @Operation(summary = "Get user evaluation history",
            description = "Retrieve all fact-check evaluations for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user history"),
            @ApiResponse(responseCode = "400", description = "Error during token decoding or invalid user ID"),
            @ApiResponse(responseCode = "500", description = "Error during history retrieval")
    })
    public ResponseEntity<Response<?>> getUserHistory(
            @RequestHeader("Authorization")
            @Parameter(description = "JWT Bearer token") String token) {

        log.info("Retrieving user history");

        try {
            Claims claims = decoder.decode(token);
            int userId = Integer.parseInt(claims.get("userId").toString());

            List<FactCheckResultDto> history = businessLogicService.getUserHistory(userId);

            log.info("Successfully retrieved {} history records for user: {}",
                    history.size(), userId);

            return ResponseEntity.ok(new Response<>(history));

        } catch (JwtException e) {
            log.error("JWT token decoding error: {}", e.getMessage());
            return ResponseEntity.status(400)
                    .body(new Response<>("Invalid or expired token: " + e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Error retrieving user history: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(new Response<>("Error retrieving history: " + e.getMessage()));
        }
    }
}
