package com.pfnd.BusinessLogicService.service.impl;

import com.pfnd.BusinessLogicService.Messages;
import com.pfnd.BusinessLogicService.model.dto.EvaluationHistoryDto;
import com.pfnd.BusinessLogicService.model.dto.FactCheckRequestDto;
import com.pfnd.BusinessLogicService.model.dto.FactCheckResultDto;
import com.pfnd.BusinessLogicService.model.messages.FactCheckCommand;
import com.pfnd.BusinessLogicService.model.postgresql.EvaluationHistoryRecord;
import com.pfnd.BusinessLogicService.repository.EvalutationHistoryRepository;
import com.pfnd.BusinessLogicService.service.BusinessLogicService;
import com.pfnd.BusinessLogicService.service.FactCheckRequestHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessLogicServiceImpl implements BusinessLogicService {
    private final EvalutationHistoryRepository evalutationHistoryRepository; //TODO implement user service responsible for handling edge cases
    private final FactCheckRequestHandler factCheckRequestHandler;

    @Override
    public EvaluationHistoryDto initiateFactCheck(FactCheckRequestDto request, int userId) {
        EvaluationHistoryRecord record = EvaluationHistoryRecord.builder().userId(userId).createdAt(new Date())
                                                                .inputText(request.getText()).status("REQUESTED").build();
        try {
            record = evalutationHistoryRepository.saveAndFlush(record);
        } catch (Exception e) {
            log.error("{}{}", Messages.SAVE_ERROR, record, e);
            throw new RuntimeException(Messages.SAVE_ERROR + record);
        }
        factCheckRequestHandler.requestEvaluation(
                new FactCheckCommand(record.getId(), record.getInputText(), record.getUserId()));
        return new EvaluationHistoryDto(record);
    }

    @Override
    public EvaluationHistoryDto getEvaluationStatus(UUID id) {
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
