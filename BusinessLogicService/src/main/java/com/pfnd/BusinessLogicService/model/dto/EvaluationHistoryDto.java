package com.pfnd.BusinessLogicService.model.dto;

import com.pfnd.BusinessLogicService.model.postgresql.EvaluationHistoryRecord;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class EvaluationHistoryDto {
    private int id;
    private int userId;
    private String inputText;
    private String explanation;
    private float score;
    private String sources;
    private Date createdAt;
    private ClassificationLabel label;
    private String status;
    // omit scrapedSources for simplicity or map if needed

    public EvaluationHistoryDto(EvaluationHistoryRecord record) {
        this.setId(record.getId());
        this.setUserId(record.getUserId());
        this.setInputText(record.getInputText());
        this.setExplanation(record.getExplanation());
        this.setScore(record.getScore());
        this.setSources(record.getSources());
        this.setCreatedAt(record.getCreatedAt());
    }
}
