package com.pfnd.BusinessLogicService.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AnalyzeResult {
    private float finalScore;
    private ClassificationLabel label;
    private String explanation;
    private Map<String, ScoredValue> results;
    private List<Reference> references;
}