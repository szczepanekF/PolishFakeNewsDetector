package com.pfnd.BusinessLogicService.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Reference {

    private String title;
    private Date publicationDate;
    private String source;
    private String link;
}