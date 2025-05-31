package com.pfnd.BusinessLogicService.service.impl;

import com.pfnd.BusinessLogicService.model.dto.EvaluationStatusDto;
import com.pfnd.BusinessLogicService.model.dto.FactCheckRequestDto;
import com.pfnd.BusinessLogicService.model.dto.FactCheckResultDto;
import com.pfnd.BusinessLogicService.repository.EvalutationHistoryRepository;
import com.pfnd.BusinessLogicService.repository.ScrapedSourcesRepository;
import com.pfnd.BusinessLogicService.service.BusinessLogicService;
import com.pfnd.BusinessLogicService.service.FactCheckRequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessLogicServiceImpl implements BusinessLogicService {
    private ScrapedSourcesRepository scrapedSourcesRepository; //TODO implement user service responsible for handling edge cases
    private EvalutationHistoryRepository evalutationHistoryRepository; //TODO implement user service responsible for handling edge cases
    private FactCheckRequestHandler factCheckRequestHandler;
    @Override
    public UUID initiateFactCheck(FactCheckRequestDto request) {
        return null;
    }

    @Override
    public EvaluationStatusDto getEvaluationStatus(UUID id) {
        return null;
    }

    @Override
    public FactCheckResultDto getEvaluationResult(UUID id) {
        return null;
    }

    @Override
    public List<FactCheckResultDto> getUserHistory(String userId) {
        return List.of();
    }
}
