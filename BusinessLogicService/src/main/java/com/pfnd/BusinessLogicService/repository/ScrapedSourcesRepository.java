package com.pfnd.BusinessLogicService.repository;

import com.pfnd.BusinessLogicService.model.postgresql.ScrapedSource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapedSourcesRepository extends JpaRepository<ScrapedSource, Integer> {
}
