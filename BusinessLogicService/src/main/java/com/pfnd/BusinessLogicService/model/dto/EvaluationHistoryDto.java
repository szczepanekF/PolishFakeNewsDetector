package com.pfnd.BusinessLogicService.model.dto;

import com.pfnd.BusinessLogicService.model.postgresql.EvaluationHistoryRecord;
import jakarta.persistence.Convert;
import jakarta.persistence.Lob;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class EvaluationHistoryDto {
    private int id;
    private int userId;
    private String inputText;
    private Date createdAt;

    private String status;
    public EvaluationHistoryDto(EvaluationHistoryRecord record) {
        this.setId(record.getId());
        this.setUserId(record.getUserId());
        this.setInputText(record.getInputText());
        this.setStatus(record.getStatus());
        this.setCreatedAt(record.getCreatedAt());
    }
}
