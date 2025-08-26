package com.pfnd.BusinessLogicService.model.postgresql;

import com.pfnd.BusinessLogicService.model.dto.ClassificationLabel;
import com.pfnd.BusinessLogicService.model.dto.Reference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "analyzeResults")
public class AnalyzeResultRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "historyId", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_history_record"))
    public EvaluationHistoryRecord historyRecord;

    private float finalScore;
    @Enumerated(EnumType.STRING)
    private ClassificationLabel label;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @OneToMany(mappedBy = "analyzeResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultRecord> results = new ArrayList<>();

    @OneToMany(mappedBy = "analyzeResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReferenceRecord> references = new ArrayList<>();

    @Override
    public String toString() {
        return "AnalyzeResultRecord{" +
                "id=" + id +
                ", historyRecordId=" + (historyRecord != null ? historyRecord.getId() : "null") +
                ", finalScore=" + finalScore +
                ", label=" + label +
                ", explanation='" + (explanation != null ? explanation.substring(0, Math.min(explanation.length(), 100)) + "..." : "null") + '\'' +
                ", resultsCount=" + (results != null ? results.size() : 0) +
                ", referencesCount=" + (references != null ? references.size() : 0) +
                '}';
    }
}