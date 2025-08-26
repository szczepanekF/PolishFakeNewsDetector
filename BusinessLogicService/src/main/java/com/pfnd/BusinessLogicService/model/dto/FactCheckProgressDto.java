package com.pfnd.BusinessLogicService.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FactCheckProgressDto {
    private String id;
    private String message;
    private int currentStep;
    private int allSteps; // TODO refactor this, sending a constant in each message exchange is not necessary
    private AnalyzeResult result; // TODO split this to separate class, leave the progress class alone

    public boolean isFinalStep() {
        return currentStep == allSteps;
    }
}