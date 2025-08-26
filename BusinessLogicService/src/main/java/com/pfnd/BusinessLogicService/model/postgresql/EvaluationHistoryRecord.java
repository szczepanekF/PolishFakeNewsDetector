package com.pfnd.BusinessLogicService.model.postgresql;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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
    @Column(columnDefinition = "TEXT")
    private String inputText;
    private Date createdAt;
    private String status;
    private int steps;
}
