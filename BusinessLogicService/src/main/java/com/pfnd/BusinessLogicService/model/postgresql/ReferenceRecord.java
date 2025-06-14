package com.pfnd.BusinessLogicService.model.postgresql;

import com.pfnd.BusinessLogicService.model.dto.Reference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ReferenceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Date publicationDate;
    private String source;
    private String link;

    public ReferenceRecord(Reference ref) {
        title = ref.getTitle();
        publicationDate = ref.getPublicationDate();
        source = ref.getSource();
        link = ref.getLink();
    }
}