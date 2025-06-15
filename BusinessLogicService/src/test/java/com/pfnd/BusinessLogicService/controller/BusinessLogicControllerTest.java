package com.pfnd.BusinessLogicService.controller;


import java.util.Arrays;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfnd.BusinessLogicService.model.dto.*;
import com.pfnd.BusinessLogicService.service.BusinessLogicService;
import com.pfnd.BusinessLogicService.service.JwtTokenDecoder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BusinessLogicController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BusinessLogicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BusinessLogicService service;

    @MockitoBean
    private JwtTokenDecoder decoder;
    @Autowired
    private ObjectMapper objectMapper;
    private FactCheckRequestDto requestDto;
    private Claims claims;

    @BeforeEach
    public void init() {
        requestDto = new FactCheckRequestDto("sample text");
        claims = Jwts.claims().add("userId", 42).build();
        when(decoder.decode(anyString())).thenReturn(claims);
    }

    @Test
    public void testInitiateFactCheckSuccess() throws Exception {
        EvaluationHistoryDto dto = new EvaluationHistoryDto();
        dto.setId(123);
        dto.setInputText(requestDto.getText());
        dto.setStatus("ONGOING");
        when(service.initiateFactCheck(any(FactCheckRequestDto.class), eq(42))).thenReturn(dto);
        mockMvc.perform(post("/app/evaluate")
                                .header("Authorization", "Bearer dummy-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.contained_object.id").value("123"))
               .andExpect(jsonPath("$.contained_object.input_text").value("sample text"))
               .andExpect(jsonPath("$.contained_object.status").value("ONGOING"))
               .andExpect(jsonPath("$.error").value(nullValue()));

        verify(service, times(1)).initiateFactCheck(any(FactCheckRequestDto.class), eq(42));
        verify(decoder, times(1)).decode(anyString());
    }

    @Test
    public void testInitiateFactCheckJwtException() throws Exception {
        when(decoder.decode(anyString())).thenThrow(new JwtException("testjwt"));
        mockMvc.perform(post("/app/evaluate")
                                .header("Authorization", "Bearer dummy-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.contained_object").value(nullValue()))
               .andExpect(jsonPath("$.error").value("testjwt"));

        verify(service, never()).initiateFactCheck(any(FactCheckRequestDto.class), eq(42));
        verify(decoder, times(1)).decode(anyString());
    }

    @Test
    public void testInitiateFactCheckRuntimeException() throws Exception {
        when(service.initiateFactCheck(any(FactCheckRequestDto.class), eq(42))).thenThrow(
                new RuntimeException("testruntime"));
        mockMvc.perform(post("/app/evaluate")
                                .header("Authorization", "Bearer dummy-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
               .andExpect(status().isInternalServerError())
               .andExpect(jsonPath("$.contained_object").value(nullValue()))
               .andExpect(jsonPath("$.error").value("testruntime"));

        verify(service, times(1)).initiateFactCheck(any(FactCheckRequestDto.class), eq(42));
        verify(decoder, times(1)).decode(anyString());
    }

    @Test
    public void testGetStatusSuccess() throws Exception {
        FactCheckResultDto dto = new FactCheckResultDto();
        dto.setId("123");
        dto.setMessage("sample text");
        dto.setCurrentStep(5);
        dto.setAllSteps(5);

        when(service.getEvaluationStatus(123L)).thenReturn(dto);

        mockMvc.perform(get("/app/status/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contained_object.id").value("123"))
                .andExpect(jsonPath("$.contained_object.message").value("sample text"))
                .andExpect(jsonPath("$.error").value(nullValue()));

        verify(service, times(1)).getEvaluationStatus(123L);
    }

    @Test
    public void testGetStatusNotFound() throws Exception {
        when(service.getEvaluationStatus(999L)).thenThrow(
                new RuntimeException("Evaluation not found for id: 999"));

        mockMvc.perform(get("/app/status/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.contained_object").value(nullValue()))
                .andExpect(jsonPath("$.error").value("Evaluation record not found"));

        verify(service, times(1)).getEvaluationStatus(999L);
    }

    @Test
    public void testGetStatusInternalError() throws Exception {
        when(service.getEvaluationStatus(123L)).thenThrow(
                new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/app/status/123"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.contained_object").value(nullValue()))
                .andExpect(jsonPath("$.error").value("Error retrieving status: Database connection failed"));

        verify(service, times(1)).getEvaluationStatus(123L);
    }

    @Test
    public void testGetResultSuccess() throws Exception {
        FactCheckResultDto resultDto = new FactCheckResultDto();
        resultDto.setId("123");
        resultDto.setMessage("sample text");
        resultDto.setCurrentStep(5);
        resultDto.setAllSteps(5);

        AnalyzeResult analyzeResult = new AnalyzeResult();
        analyzeResult.setFinalScore(0.8F);
        analyzeResult.setLabel(ClassificationLabel.TRUE);
        analyzeResult.setExplanation("Text is factually accurate based on analysis");
        analyzeResult.setReferences(Arrays.asList(
                new Reference("Reuters", new Date(), "https://example.com/1"),
                new Reference("BBC", new Date(),"https://example.com/2")
        ));
        resultDto.setResult(analyzeResult);

        when(service.getEvaluationResult(123)).thenReturn(resultDto);

        mockMvc.perform(get("/app/result/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contained_object.id").value("123"))
                .andExpect(jsonPath("$.contained_object.message").value("sample text"))
                .andExpect(jsonPath("$.contained_object.current_step").value(5))
                .andExpect(jsonPath("$.contained_object.all_steps").value(5))
                .andExpect(jsonPath("$.contained_object.result.final_score").value(0.8))
                .andExpect(jsonPath("$.contained_object.result.label").value("TRUE"))
                .andExpect(jsonPath("$.contained_object.result.explanation").value("Text is factually accurate based on analysis"))
                .andExpect(jsonPath("$.contained_object.result.references").isArray())
                .andExpect(jsonPath("$.contained_object.result.references.length()").value(2))
                .andExpect(jsonPath("$.contained_object.result.references[0].source").value("Reuters"))
                .andExpect(jsonPath("$.error").value(nullValue()));

        verify(service, times(1)).getEvaluationResult(123);
    }

    @Test
    public void testGetResultNotFound() throws Exception {
        when(service.getEvaluationResult(999)).thenThrow(
                new RuntimeException("Evaluation record not found"));

        mockMvc.perform(get("/app/result/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.contained_object").value(nullValue()))
                .andExpect(jsonPath("$.error").value("Evaluation record not found"));

        verify(service, times(1)).getEvaluationResult(999);
    }

    @Test
    public void testGetResultNotCompleted() throws Exception {
        when(service.getEvaluationResult(123)).thenThrow(
                new RuntimeException("Evaluation not completed"));

        mockMvc.perform(get("/app/result/123"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.contained_object").value(nullValue()))
                .andExpect(jsonPath("$.error").value("Evaluation still in progress"));

        verify(service, times(1)).getEvaluationResult(123);
    }

    @Test
    public void testGetResultInternalError() throws Exception {
        when(service.getEvaluationResult(123)).thenThrow(
                new RuntimeException("Database error"));

        mockMvc.perform(get("/app/result/123"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.contained_object").value(nullValue()))
                .andExpect(jsonPath("$.error").value("Error retrieving result: Database error"));

        verify(service, times(1)).getEvaluationResult(123);
    }

    @Test
    public void testGetUserHistorySuccess() throws Exception {
        FactCheckResultDto result1 = new FactCheckResultDto();
        result1.setId("123");
        result1.setMessage("first text");

        FactCheckResultDto result2 = new FactCheckResultDto();
        result2.setId("124");
        result2.setMessage("second text");

        List<FactCheckResultDto> history = Arrays.asList(result1, result2);

        when(service.getUserHistory( 42)).thenReturn(history);

        mockMvc.perform(get("/app/history")
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contained_object").isArray())
                .andExpect(jsonPath("$.contained_object.length()").value(2))
                .andExpect(jsonPath("$.contained_object[0].id").value("123"))
                .andExpect(jsonPath("$.contained_object[0].message").value("first text"))
                .andExpect(jsonPath("$.contained_object[1].id").value("124"))
                .andExpect(jsonPath("$.contained_object[1].message").value("second text"))
                .andExpect(jsonPath("$.error").value(nullValue()));

        verify(service, times(1)).getUserHistory(42);
        verify(decoder, times(1)).decode(anyString());
    }

    @Test
    public void testGetUserHistoryJwtException() throws Exception {
        when(decoder.decode(anyString())).thenThrow(new JwtException("Invalid token"));

        mockMvc.perform(get("/app/history")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.contained_object").value(nullValue()))
                .andExpect(jsonPath("$.error").value("Invalid or expired token: Invalid token"));

        verify(service, never()).getUserHistory(anyInt());
        verify(decoder, times(1)).decode(anyString());
    }

    @Test
    public void testGetUserHistoryRuntimeException() throws Exception {
        when(service.getUserHistory(42)).thenThrow(
                new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/app/history")
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.contained_object").value(nullValue()))
                .andExpect(jsonPath("$.error").value("Error retrieving history: Database connection failed"));

        verify(service, times(1)).getUserHistory(42);
        verify(decoder, times(1)).decode(anyString());
    }

    @Test
    public void testGetUserHistoryEmptyList() throws Exception {
        List<FactCheckResultDto> emptyHistory = List.of();

        when(service.getUserHistory(42)).thenReturn(emptyHistory);

        mockMvc.perform(get("/app/history")
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contained_object").isArray())
                .andExpect(jsonPath("$.contained_object.length()").value(0))
                .andExpect(jsonPath("$.error").value(nullValue()));

        verify(service, times(1)).getUserHistory(42);
        verify(decoder, times(1)).decode(anyString());
    }
}
