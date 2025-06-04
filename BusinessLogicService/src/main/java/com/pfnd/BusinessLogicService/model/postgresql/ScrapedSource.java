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
@Table(name = "scrapedSources")
public class ScrapedSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String url;
    private String sourceName;
    private String content;
    private Date lastUpdated;
    @ManyToMany
    @JoinTable(
            name = "evaluation_sources",
            joinColumns = {@JoinColumn(name = "scraped_source_id")},
            inverseJoinColumns = {@JoinColumn(name = "evaluation_id")}
    )
    Set<EvaluationHistoryRecord> evaluations = new HashSet<>();
}

