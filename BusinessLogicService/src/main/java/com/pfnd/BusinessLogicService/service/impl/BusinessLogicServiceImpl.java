package com.pfnd.BusinessLogicService.service.impl;

import com.pfnd.BusinessLogicService.utils.Messages;
import com.pfnd.BusinessLogicService.model.dto.*;
import com.pfnd.BusinessLogicService.model.messages.FactCheckCommand;
import com.pfnd.BusinessLogicService.model.postgresql.AnalyzeResultRecord;
import com.pfnd.BusinessLogicService.model.postgresql.EvaluationHistoryRecord;
import com.pfnd.BusinessLogicService.model.postgresql.ResultRecord;
import com.pfnd.BusinessLogicService.repository.AnalyzeResultRepository;
import com.pfnd.BusinessLogicService.repository.EvaluationHistoryRepository;
import com.pfnd.BusinessLogicService.service.BusinessLogicService;
import com.pfnd.BusinessLogicService.service.FactCheckRequestHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessLogicServiceImpl implements BusinessLogicService {
    private final EvaluationHistoryRepository evaluationHistoryRepository;
    private final AnalyzeResultRepository analyzeResultRepository;
    private final FactCheckRequestHandler factCheckRequestHandler;

    @Override
    public EvaluationHistoryDto initiateFactCheck(FactCheckRequestDto request, int userId) {
        EvaluationHistoryRecord record = EvaluationHistoryRecord.builder().userId(userId).createdAt(new Date())
                                                                .inputText(request.getText()).status("STARTED")
                                                                .steps(-1).build();
        try {
            record = evaluationHistoryRepository.saveAndFlush(record);
        } catch (Exception e) {
            log.error("{}{}", Messages.SAVE_ERROR, record, e);
            throw new RuntimeException(Messages.SAVE_ERROR + record);
        }
        factCheckRequestHandler.requestEvaluation(
                new FactCheckCommand(record.getId(), record.getInputText(), record.getUserId()));
        return new EvaluationHistoryDto(record);
    }

    @Override
    public FactCheckProgressDto getEvaluationStatus(long id) {
        Optional<FactCheckProgressDto> cacheResult = factCheckRequestHandler.getInterimResult(id);

        if (cacheResult.isPresent()) {
            log.debug("Found evaluation status in cache for id: {}", id);
            return cacheResult.get();
        }
        log.info("Cache miss for id: {}, checking database", id);

        try {
            Optional<AnalyzeResultRecord> recordOpt = analyzeResultRepository.findByHistoryRecord_Id((int) id);

            if (recordOpt.isEmpty()) {
                log.warn("Evaluation record not found in database for id: {}", id);
                throw new RuntimeException("Evaluation not found for id: " + id);
            }

            AnalyzeResultRecord record = recordOpt.get();

            FactCheckProgressDto dto = new FactCheckProgressDto();
            dto.setId(String.valueOf(record.getHistoryRecord().getId()));
            dto.setMessage(record.getHistoryRecord().getStatus());
            dto.setCurrentStep(record.getHistoryRecord().getSteps());
            dto.setAllSteps(record.getHistoryRecord().getSteps());
            dto.setResult(createAnalyzeResultDto(record));
            return dto;
        } catch (Exception e) {
            log.error("Error fetching evaluation status for id: {}", id, e);
            throw new RuntimeException("Error fetching evaluation status: " + e.getMessage());
        }
    }

    @Override
    public FactCheckProgressDto getEvaluationResult(int id) {
        log.info("Fetching evaluation result for id: {}", id);

        Optional<EvaluationHistoryRecord> recordOpt = evaluationHistoryRepository.findById(id);

        if (recordOpt.isEmpty()) {
            log.warn("Evaluation record not found for id: {}", id);
            throw new RuntimeException("Evaluation record not found for id: " + id);
        }

        EvaluationHistoryRecord record = recordOpt.get();

        if (record.getStatus().equals("STARTED") || record.getSteps() == -1) {
            log.warn("Evaluation not completed yet for id: {}", id);
            throw new RuntimeException("Evaluation not completed yet for id: " + id);
        }

        Optional<AnalyzeResultRecord> analyzeResults = analyzeResultRepository.findByHistoryRecord_Id(id);

        if (analyzeResults.isEmpty()) {
            log.warn("Analysis result not found for evaluation id: {}", id);
            throw new RuntimeException("Analysis result not found for evaluation id: " + id);
        }

        AnalyzeResultRecord analyzeResult = analyzeResults.get();

        FactCheckProgressDto result = new FactCheckProgressDto();
        result.setId(String.valueOf(record.getId()));
        result.setMessage(record.getInputText());
        result.setCurrentStep(record.getSteps());
        result.setAllSteps(record.getSteps());
        result.setResult(createAnalyzeResultDto(analyzeResult));

        return result;
    }

    @Override
    public List<FactCheckProgressDto> getUserHistory(int userId) {
        log.info("Fetching user history for userId: {}", userId);
        try {

            List<EvaluationHistoryRecord> records =
                    evaluationHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);


            return records.stream()
                          .filter(record -> "COMPLETED".equals(record.getStatus()))
                          .map(record -> {
                              FactCheckProgressDto dto = new FactCheckProgressDto();
                              dto.setId(String.valueOf(record.getId()));
                              dto.setMessage(record.getInputText());
                              dto.setCurrentStep(5);
                              dto.setAllSteps(5);

                              try {
                                  Optional<AnalyzeResultRecord> analyzeResults = analyzeResultRepository.findByHistoryRecord_Id(
                                          record.getId());
                                  analyzeResults.ifPresent(analyzeResultRecord -> dto.setResult(
                                          createAnalyzeResultDto(analyzeResultRecord)));
                              } catch (Exception e) {
                                  log.warn("Could not fetch analyze result for record id: {}", record.getId(), e);
                              }

                              return dto;
                          })
                          .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching user history for userId: {}", userId, e);
            throw new RuntimeException("Error fetching user history: " + e.getMessage());
        }
    }

    public AnalyzeResult createAnalyzeResultDto(AnalyzeResultRecord analyzeResult) {
        AnalyzeResult analyzeDto = new AnalyzeResult();

        analyzeDto.setText(analyzeResult.getHistoryRecord().getInputText());

        analyzeDto.setFinalScore(analyzeResult.getFinalScore());
        analyzeDto.setLabel(analyzeResult.getLabel());
        analyzeDto.setExplanation(analyzeResult.getExplanation());

        Map<String, ScoredValue> resultsMap = new HashMap<>();
        if (analyzeResult.getResults() != null) {
            for (ResultRecord resultRecord : analyzeResult.getResults()) {
                resultsMap.put(resultRecord.getKey(), resultRecord.getValue());
            }
        }
        analyzeDto.setResults(resultsMap);

        List<Reference> references = new ArrayList<>();
        if (analyzeResult.getReferences() != null) {
            references = analyzeResult.getReferences().stream()
                                      .map(ref -> new Reference(ref.getFootnoteNumber(), ref.getSource(),
                                              ref.getPublicationDate(),
                                              ref.getLink()))
                                      .collect(Collectors.toList());
        }
        analyzeDto.setReferences(references);
        return analyzeDto;
    }
}
