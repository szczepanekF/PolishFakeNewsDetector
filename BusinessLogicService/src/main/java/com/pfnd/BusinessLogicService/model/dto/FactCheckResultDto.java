package com.pfnd.BusinessLogicService.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class FactCheckResultDto {
    private String id;
    private int currentStep;
    private String message;
    private int allSteps;
    private AnalyzeResult result;
}