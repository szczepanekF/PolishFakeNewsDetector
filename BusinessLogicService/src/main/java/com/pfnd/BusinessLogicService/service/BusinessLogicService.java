package com.pfnd.BusinessLogicService.service;

import com.pfnd.BusinessLogicService.model.dto.*;

import java.util.List;

public interface BusinessLogicService {
    EvaluationHistoryDto initiateFactCheck(FactCheckRequestDto request, int userId);

    FactCheckResultDto getEvaluationStatus(long id);

    FactCheckResultDto getEvaluationResult(int id);

    List<FactCheckResultDto> getUserHistory(int userId);
    // TODO change this getUserHistory method to this
//    List<EvaluationHistoryDto> getUserHistory(int userId);
}
