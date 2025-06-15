package com.pfnd.BusinessLogicService.service;

import com.pfnd.BusinessLogicService.model.dto.EvaluationHistoryDto;
import com.pfnd.BusinessLogicService.model.dto.FactCheckRequestDto;
import com.pfnd.BusinessLogicService.model.dto.FactCheckResultDto;

import java.util.List;
import java.util.UUID;

public interface BusinessLogicService {
    EvaluationHistoryDto initiateFactCheck(FactCheckRequestDto request, int userId);

    FactCheckResultDto getEvaluationStatus(long id);

    FactCheckResultDto getEvaluationResult(UUID id);

    List<FactCheckResultDto> getUserHistory(String userId);


}
