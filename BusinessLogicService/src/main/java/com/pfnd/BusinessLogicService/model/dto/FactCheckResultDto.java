package com.pfnd.BusinessLogicService.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class FactCheckResultDto {
    private String id;
    private String text;
    private String status;
    private AnalyzeResult sentiment;


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class ScoredValue {
        private String value;
        private float score;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Reference {
        private int id;
        private String title;
        private String url;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class AnalyzeResult {
        private float finalScore;
        private ClassificationLabel label;
        private String explanation;
        private Map<String, ScoredValue> results;
        private List<Reference> references;
    }
}