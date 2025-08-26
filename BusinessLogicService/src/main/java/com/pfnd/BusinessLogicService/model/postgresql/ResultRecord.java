package com.pfnd.BusinessLogicService.model.postgresql;

import com.pfnd.BusinessLogicService.model.dto.ScoredValue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String key;
    @Embedded
    private ScoredValue value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analyze_result_id")
    private AnalyzeResultRecord analyzeResult;
    public ResultRecord(String key, ScoredValue value) {
        this.key = key;
        this.value = value;
    }
}