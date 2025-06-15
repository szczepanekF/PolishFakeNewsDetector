package com.pfnd.BusinessLogicService.model.postgresql;

import com.pfnd.BusinessLogicService.model.dto.Reference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ReferenceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date publicationDate;
    private String source;
    private String link;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analyze_result_id")
    private AnalyzeResultRecord analyzeResult;

    public ReferenceRecord(Reference ref) {
        publicationDate = ref.getPublicationDate();
        source = ref.getSource();
        link = ref.getLink();
    }
}