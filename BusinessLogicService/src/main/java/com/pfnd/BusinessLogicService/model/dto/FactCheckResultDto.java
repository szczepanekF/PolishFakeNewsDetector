package com.pfnd.BusinessLogicService.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FactCheckResultDto {
    private String id;
    private String message;
    private int currentStep;
    private int allSteps;
    private AnalyzeResult result;
}