package com.pfnd.BusinessLogicService.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfnd.BusinessLogicService.model.dto.EvaluationHistoryDto;
import com.pfnd.BusinessLogicService.model.dto.FactCheckRequestDto;
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
}
