package com.pfnd.BusinessLogicService.service.impl;

import com.pfnd.BusinessLogicService.model.dto.AnalyzeResult;
import com.pfnd.BusinessLogicService.model.dto.FactCheckProgressDto;
import com.pfnd.BusinessLogicService.model.messages.FactCheckCommand;
import com.pfnd.BusinessLogicService.model.postgresql.AnalyzeResultRecord;
import com.pfnd.BusinessLogicService.model.postgresql.EvaluationHistoryRecord;
import com.pfnd.BusinessLogicService.model.postgresql.ReferenceRecord;
import com.pfnd.BusinessLogicService.model.postgresql.ResultRecord;
import com.pfnd.BusinessLogicService.repository.AnalyzeResultRepository;
import com.pfnd.BusinessLogicService.repository.EvaluationHistoryRepository;
import com.pfnd.BusinessLogicService.utils.Messages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationResultService {
    private final EvaluationHistoryRepository historyRepo;
    private final AnalyzeResultRepository resultRepo;

    public void updateHistoryRecord(FactCheckCommand request, FactCheckProgressDto progress) {
        EvaluationHistoryRecord history = historyRepo.findById(request.historyId()).orElseThrow(
                                                   () -> new RuntimeException(Messages.DOES_NOT_EXIST));
        Optional<AnalyzeResultRecord> analyzeResultRecord = resultRepo.findByHistoryRecord_Id(request.historyId());
        if (analyzeResultRecord.isPresent()) {
            log.error("{}{}", Messages.ENTITY_EXISTS, analyzeResultRecord.get());
            throw new RuntimeException(Messages.ENTITY_EXISTS + analyzeResultRecord.get());
        }
        AnalyzeResult receivedResult = progress.getResult();
        AnalyzeResultRecord resultRecord = buildResultRecord(receivedResult, history);
        history.setSteps(progress.getAllSteps());
        history.setStatus(progress.getMessage());
        try {
            resultRepo.saveAndFlush(resultRecord);
            historyRepo.save(history);
        } catch (Exception e) {
            log.error("{}{}", Messages.SAVE_ERROR, resultRecord, e);
            throw new RuntimeException(Messages.SAVE_ERROR + resultRecord);
        }
    }

    private AnalyzeResultRecord buildResultRecord(AnalyzeResult result, EvaluationHistoryRecord history) {
        List<ReferenceRecord> references = result.getReferences().stream().map(ReferenceRecord::new)
                                                               .toList();
        List<ResultRecord> results = result.getResults().entrySet().stream()
                                                        .map(entry -> new ResultRecord(entry.getKey(),
                                                                entry.getValue())).toList();

        AnalyzeResultRecord analyzeResultRecord = AnalyzeResultRecord.builder()
                                                              .historyRecord(history)
                                                              .finalScore(result.getFinalScore())
                                                              .label(result.getLabel())
                                                              .explanation(result.getExplanation())
                                                              .results(results)
                                                              .references(references)
                                                              .build();
        results.forEach(rr -> rr.setAnalyzeResult(analyzeResultRecord));
        references.forEach(rr -> rr.setAnalyzeResult(analyzeResultRecord));

        return analyzeResultRecord;
    }
}
