package com.pfnd.BusinessLogicService.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class FactCheckResultDto {
    private String id;
    private String message;
    private int currentStep;
    private int allSteps;
    private AnalyzeResult result;
}