package com.pfnd.BusinessLogicService.model.postgresql;

import com.pfnd.BusinessLogicService.model.dto.ClassificationLabel;
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
    private int id;
    private int userId;
    private String inputText;
    private Date createdAt;

    //TODO this below seems like another entity
    private String status;
    private String explanation;
    private float score;
    private String sources;
    private ClassificationLabel label;


    @ManyToMany(mappedBy = "evaluations")
    private Set<ScrapedSource> scrapedSources = new HashSet<>();
}
