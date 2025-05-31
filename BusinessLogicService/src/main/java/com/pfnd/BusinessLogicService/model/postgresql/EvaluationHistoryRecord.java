package com.pfnd.BusinessLogicService.model.postgresql;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Builder
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "evaluationHistory")
public class EvaluationHistoryRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String userId;
    private String inputText;
    private String explanation;
    private float score;
    private String sources;
    private Date createdAt;

    @ManyToMany(mappedBy = "evaluations")
    private Set<ScrapedSource> scrapedSources = new HashSet<>();
}
