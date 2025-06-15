package com.pfnd.BusinessLogicService.service;

import java.util.*;
import com.pfnd.BusinessLogicService.model.dto.*;
import com.pfnd.BusinessLogicService.model.messages.FactCheckCommand;
import com.pfnd.BusinessLogicService.model.postgresql.AnalyzeResultRecord;
import com.pfnd.BusinessLogicService.model.postgresql.EvaluationHistoryRecord;
import com.pfnd.BusinessLogicService.model.postgresql.ReferenceRecord;
import com.pfnd.BusinessLogicService.model.postgresql.ResultRecord;
import com.pfnd.BusinessLogicService.repository.AnalyzeResultRepository;
import com.pfnd.BusinessLogicService.repository.EvaluationHistoryRepository;
import com.pfnd.BusinessLogicService.service.impl.BusinessLogicServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BusinessLogicServiceTest {

    private static final int USER_ID = 1;
    private static final FactCheckRequestDto request = new FactCheckRequestDto("test text");
    private EvaluationHistoryRecord record;
    @Mock
    private EvaluationHistoryRepository evaluationHistoryRepository;

    @Mock
    private FactCheckRequestHandler factCheckRequestHandler;

    @Mock
    private AnalyzeResultRepository analyzeResultRepository;

    @InjectMocks
    private BusinessLogicServiceImpl businessLogicService;

    @BeforeEach
    public void init() {
        record = new EvaluationHistoryRecord();
        record.setId(123);
        record.setInputText(request.getText());
    }

    @Test
    void shouldInitiateFactCheckSuccessfully() {
        when(evaluationHistoryRepository.saveAndFlush(any(EvaluationHistoryRecord.class))).thenReturn(record);

        EvaluationHistoryDto savedRecord = businessLogicService.initiateFactCheck(request, record.getUserId());

        assertEquals(savedRecord, new EvaluationHistoryDto(record));
        verify(factCheckRequestHandler, times(1)).requestEvaluation(any(FactCheckCommand.class));
    }

    @Test
    void shouldThrowAnErrorWhenSavingEvaluationHistoryRecordFailed() {
        when(evaluationHistoryRepository.saveAndFlush(any(EvaluationHistoryRecord.class))).thenThrow(
                DataIntegrityViolationException.class);
        assertThrows(RuntimeException.class, () -> businessLogicService.initiateFactCheck(request, record.getUserId()));
        verify(factCheckRequestHandler, times(0)).requestEvaluation(any(FactCheckCommand.class));
    }

    @Test
    void shouldGetEvaluationStatusSuccessfully() {
        when(factCheckRequestHandler.getInterimResult(123L)).thenReturn(Optional.empty());

        EvaluationHistoryRecord record = new EvaluationHistoryRecord();
        record.setId(123);
        record.setInputText("test text");
        record.setStatus("COMPLETED");

        when(evaluationHistoryRepository.findById(123)).thenReturn(Optional.of(record));

        FactCheckResultDto result = businessLogicService.getEvaluationStatus(123L);

        assertEquals("123", result.getId());
        assertEquals("test text", result.getMessage());

        verify(factCheckRequestHandler, times(1)).getInterimResult(123L);
        verify(evaluationHistoryRepository, times(1)).findById(123);
    }

    @Test
    void shouldGetEvaluationStatusFromCache() {
        FactCheckResultDto cachedResult = new FactCheckResultDto();
        cachedResult.setId("123");
        cachedResult.setMessage("cached text");

        when(factCheckRequestHandler.getInterimResult(123L)).thenReturn(Optional.of(cachedResult));

        FactCheckResultDto result = businessLogicService.getEvaluationStatus(123L);

        assertEquals("123", result.getId());
        assertEquals("cached text", result.getMessage());

        verify(factCheckRequestHandler, times(1)).getInterimResult(123L);
        verify(evaluationHistoryRepository, never()).findById(anyInt());
    }

    @Test
    void shouldThrowExceptionWhenEvaluationStatusNotFound() {
        when(factCheckRequestHandler.getInterimResult(999L)).thenReturn(Optional.empty());
        when(evaluationHistoryRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> businessLogicService.getEvaluationStatus(999L));

        assertEquals("Error fetching evaluation status: Evaluation not found for id: 999", exception.getMessage());

        verify(factCheckRequestHandler, times(1)).getInterimResult(999L);
        verify(evaluationHistoryRepository, times(1)).findById(999);
    }

    @Test
    void shouldThrowExceptionWhenGetEvaluationStatusFails() {
        when(factCheckRequestHandler.getInterimResult(123L)).thenReturn(Optional.empty());
        when(evaluationHistoryRepository.findById(123)).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> businessLogicService.getEvaluationStatus(123L));

        assertEquals("Error fetching evaluation status: Database error", exception.getMessage());

        verify(factCheckRequestHandler, times(1)).getInterimResult(123L);
        verify(evaluationHistoryRepository, times(1)).findById(123);
    }


    @Test
    void shouldGetEvaluationResultSuccessfully() {
        EvaluationHistoryRecord historyRecord = new EvaluationHistoryRecord();
        historyRecord.setId(123);
        historyRecord.setInputText("test text");
        historyRecord.setStatus("COMPLETED");

        ReferenceRecord refRecord = new ReferenceRecord();
        refRecord.setPublicationDate(new Date());
        refRecord.setSource("Reuters");
        refRecord.setLink("https://example.com");

        ResultRecord resultRecord1 = new ResultRecord();
        resultRecord1.setKey("credibility");
        resultRecord1.setValue(new ScoredValue("value1", 0.5F));

        ResultRecord resultRecord2 = new ResultRecord();
        resultRecord2.setKey("factuality");
        resultRecord2.setValue(new ScoredValue("value2", 0.6F));

        AnalyzeResultRecord analyzeRecord = new AnalyzeResultRecord();
        analyzeRecord.setFinalScore(0.8F);
        analyzeRecord.setLabel(ClassificationLabel.TRUE);
        analyzeRecord.setExplanation("Text is accurate");
        analyzeRecord.setReferences(List.of(refRecord));
        analyzeRecord.setResults(List.of(resultRecord1, resultRecord2));

        when(evaluationHistoryRepository.findById(123)).thenReturn(Optional.of(historyRecord));
        when(analyzeResultRepository.findByHistoryRecord_Id(123)).thenReturn(List.of(analyzeRecord));

        FactCheckResultDto result = businessLogicService.getEvaluationResult(123);

        assertEquals("123", result.getId());
        assertEquals("test text", result.getMessage());
        assertEquals(5, result.getCurrentStep());
        assertEquals(5, result.getAllSteps());
        assertNotNull(result.getResult());

        AnalyzeResult analyzeResult = result.getResult();
        assertEquals("test text", analyzeResult.getText());
        assertEquals(0.8F, analyzeResult.getFinalScore());
        assertEquals(ClassificationLabel.TRUE, analyzeResult.getLabel());
        assertEquals("Text is accurate", analyzeResult.getExplanation());

        assertNotNull(analyzeResult.getResults());
        assertEquals(2, analyzeResult.getResults().size());
        assertTrue(analyzeResult.getResults().containsKey("credibility"));
        assertTrue(analyzeResult.getResults().containsKey("factuality"));

        ScoredValue credibilityValue = analyzeResult.getResults().get("credibility");
        assertEquals("value1", credibilityValue.getValue());
        assertEquals(0.5F, credibilityValue.getScore());

        ScoredValue factualityValue = analyzeResult.getResults().get("factuality");
        assertEquals("value2", factualityValue.getValue());
        assertEquals(0.6F, factualityValue.getScore());

        assertNotNull(analyzeResult.getReferences());
        assertEquals(1, analyzeResult.getReferences().size());
        Reference reference = analyzeResult.getReferences().get(0);
        assertEquals("Reuters", reference.getSource());
        assertEquals("https://example.com", reference.getLink());
        assertNotNull(reference.getPublicationDate());

        verify(evaluationHistoryRepository, times(1)).findById(123);
        verify(analyzeResultRepository, times(1)).findByHistoryRecord_Id(123);
    }

    @Test
    void shouldThrowExceptionWhenEvaluationResultNotFound() {
        when(evaluationHistoryRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> businessLogicService.getEvaluationResult(999));

        assertEquals("Error fetching evaluation result: Evaluation record not found for id: 999", exception.getMessage());
        verify(evaluationHistoryRepository, times(1)).findById(999);
        verify(analyzeResultRepository, never()).findByHistoryRecord_Id(anyInt());
    }

    @Test
    void shouldThrowExceptionWhenEvaluationNotCompleted() {
        EvaluationHistoryRecord record = new EvaluationHistoryRecord();
        record.setId(123);
        record.setStatus("IN_PROGRESS");

        when(evaluationHistoryRepository.findById(123)).thenReturn(Optional.of(record));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> businessLogicService.getEvaluationResult(123));

        assertEquals("Error fetching evaluation result: Evaluation not completed yet for id: 123", exception.getMessage());
        verify(evaluationHistoryRepository, times(1)).findById(123);
        verify(analyzeResultRepository, never()).findByHistoryRecord_Id(anyInt());
    }

    @Test
    void shouldThrowExceptionWhenAnalysisResultNotFound() {
        EvaluationHistoryRecord record = new EvaluationHistoryRecord();
        record.setId(123);
        record.setStatus("COMPLETED");

        when(evaluationHistoryRepository.findById(123)).thenReturn(Optional.of(record));
        when(analyzeResultRepository.findByHistoryRecord_Id(123)).thenReturn(List.of());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> businessLogicService.getEvaluationResult(123));

        assertEquals("Error fetching evaluation result: Analysis result not found for evaluation id: 123", exception.getMessage());
        verify(evaluationHistoryRepository, times(1)).findById(123);
        verify(analyzeResultRepository, times(1)).findByHistoryRecord_Id(123);
    }


    @Test
    void shouldGetUserHistorySuccessfully() {
        EvaluationHistoryRecord completedRecord = new EvaluationHistoryRecord();
        completedRecord.setId(123);
        completedRecord.setInputText("completed text");
        completedRecord.setStatus("COMPLETED");

        EvaluationHistoryRecord inProgressRecord = new EvaluationHistoryRecord();
        inProgressRecord.setId(124);
        inProgressRecord.setInputText("second completed text");
        inProgressRecord.setStatus("COMPLETED");

        EvaluationHistoryRecord requestedRecord = new EvaluationHistoryRecord();
        requestedRecord.setId(125);
        requestedRecord.setInputText("third completed text");
        requestedRecord.setStatus("COMPLETED");

        List<EvaluationHistoryRecord> records = Arrays.asList(completedRecord, inProgressRecord, requestedRecord);

        ReferenceRecord refRecord = new ReferenceRecord();
        refRecord.setPublicationDate(new Date());
        refRecord.setSource("Reuters");
        refRecord.setLink("https://example.com");

        ResultRecord resultRecord1 = new ResultRecord();
        resultRecord1.setKey("credibility");
        resultRecord1.setValue(new ScoredValue("value1", 0.8F));

        ResultRecord resultRecord2 = new ResultRecord();
        resultRecord2.setKey("bias");
        resultRecord2.setValue(new ScoredValue("value2", 0.9F));

        AnalyzeResultRecord analyzeRecord1 = new AnalyzeResultRecord();
        analyzeRecord1.setFinalScore(0.9F);
        analyzeRecord1.setLabel(ClassificationLabel.TRUE);
        analyzeRecord1.setExplanation("First analysis complete");
        analyzeRecord1.setReferences(List.of(refRecord));
        analyzeRecord1.setResults(List.of(resultRecord1, resultRecord2));

        AnalyzeResultRecord analyzeRecord2 = new AnalyzeResultRecord();
        analyzeRecord2.setFinalScore(0.7F);
        analyzeRecord2.setLabel(ClassificationLabel.FALSE);
        analyzeRecord2.setExplanation("Second analysis complete");
        analyzeRecord2.setReferences(List.of(refRecord));
        analyzeRecord2.setResults(null);

        AnalyzeResultRecord analyzeRecord3 = new AnalyzeResultRecord();
        analyzeRecord3.setFinalScore(0.5F);
        analyzeRecord3.setLabel(ClassificationLabel.MOSTLY_TRUE);
        analyzeRecord3.setExplanation("Third analysis complete");
        analyzeRecord3.setReferences(null);
        analyzeRecord3.setResults(List.of(resultRecord1));

        when(evaluationHistoryRepository.findByUserIdOrderByCreatedAtDesc(42)).thenReturn(records);
        when(analyzeResultRepository.findByHistoryRecord_Id(123)).thenReturn(List.of(analyzeRecord1));
        when(analyzeResultRepository.findByHistoryRecord_Id(124)).thenReturn(List.of(analyzeRecord2));
        when(analyzeResultRepository.findByHistoryRecord_Id(125)).thenReturn(List.of(analyzeRecord3));

        List<FactCheckResultDto> result = businessLogicService.getUserHistory(42);

        assertEquals(3, result.size());

        FactCheckResultDto completed = result.get(0);
        assertEquals("123", completed.getId());
        assertEquals("completed text", completed.getMessage());
        assertEquals(5, completed.getCurrentStep());
        assertEquals(5, completed.getAllSteps());
        assertNotNull(completed.getResult());

        AnalyzeResult analyzeResult = completed.getResult();
        assertEquals("completed text", analyzeResult.getText());
        assertEquals(0.9F, analyzeResult.getFinalScore());
        assertEquals(ClassificationLabel.TRUE, analyzeResult.getLabel());
        assertEquals("First analysis complete", analyzeResult.getExplanation());

        assertNotNull(analyzeResult.getResults());
        assertEquals(2, analyzeResult.getResults().size());
        assertTrue(analyzeResult.getResults().containsKey("credibility"));
        assertTrue(analyzeResult.getResults().containsKey("bias"));
        assertEquals("value1", analyzeResult.getResults().get("credibility").getValue());
        assertEquals(0.8F, analyzeResult.getResults().get("credibility").getScore());
        assertEquals("value2", analyzeResult.getResults().get("bias").getValue());
        assertEquals(0.9F, analyzeResult.getResults().get("bias").getScore());

        assertNotNull(analyzeResult.getReferences());
        assertEquals(1, analyzeResult.getReferences().size());
        Reference reference = analyzeResult.getReferences().getFirst();
        assertEquals("Reuters", reference.getSource());
        assertEquals("https://example.com", reference.getLink());
        assertNotNull(reference.getPublicationDate());

        FactCheckResultDto second = result.get(1);
        assertEquals("124", second.getId());
        assertEquals("second completed text", second.getMessage());
        assertEquals(5, second.getCurrentStep());
        assertEquals(5, second.getAllSteps());
        assertNotNull(second.getResult());

        AnalyzeResult secondAnalyze = second.getResult();
        assertEquals("second completed text", secondAnalyze.getText());
        assertEquals(0.7F, secondAnalyze.getFinalScore());
        assertEquals(ClassificationLabel.FALSE, secondAnalyze.getLabel());
        assertEquals("Second analysis complete", secondAnalyze.getExplanation());

        assertNotNull(secondAnalyze.getResults());
        assertTrue(secondAnalyze.getResults().isEmpty());

        assertNotNull(secondAnalyze.getReferences());
        assertEquals(1, secondAnalyze.getReferences().size());

        FactCheckResultDto third = result.get(2);
        assertEquals("125", third.getId());
        assertEquals("third completed text", third.getMessage());
        assertEquals(5, third.getCurrentStep());
        assertEquals(5, third.getAllSteps());
        assertNotNull(third.getResult());

        AnalyzeResult thirdAnalyze = third.getResult();
        assertEquals("third completed text", thirdAnalyze.getText());
        assertEquals(0.5F, thirdAnalyze.getFinalScore());
        assertEquals(ClassificationLabel.MOSTLY_TRUE, thirdAnalyze.getLabel());
        assertEquals("Third analysis complete", thirdAnalyze.getExplanation());

        assertNotNull(thirdAnalyze.getResults());
        assertEquals(1, thirdAnalyze.getResults().size());
        assertTrue(thirdAnalyze.getResults().containsKey("credibility"));

        assertNotNull(thirdAnalyze.getReferences());
        assertTrue(thirdAnalyze.getReferences().isEmpty());

        verify(evaluationHistoryRepository, times(1)).findByUserIdOrderByCreatedAtDesc(42);
        verify(analyzeResultRepository, times(1)).findByHistoryRecord_Id(123);
        verify(analyzeResultRepository, times(1)).findByHistoryRecord_Id(124);
        verify(analyzeResultRepository, times(1)).findByHistoryRecord_Id(125);
    }

    @Test
    void shouldGetEmptyUserHistory() {
        when(evaluationHistoryRepository.findByUserIdOrderByCreatedAtDesc(42)).thenReturn(List.of());

        List<FactCheckResultDto> result = businessLogicService.getUserHistory(42);

        assertEquals(0, result.size());
        verify(evaluationHistoryRepository, times(1)).findByUserIdOrderByCreatedAtDesc(42);
        verify(analyzeResultRepository, never()).findByHistoryRecord_Id(anyInt());
    }

    @Test
    void shouldThrowExceptionWhenGetUserHistoryFails() {
        when(evaluationHistoryRepository.findByUserIdOrderByCreatedAtDesc(42)).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> businessLogicService.getUserHistory(42));

        assertEquals("Error fetching user history: Database error", exception.getMessage());
        verify(evaluationHistoryRepository, times(1)).findByUserIdOrderByCreatedAtDesc(42);
    }

    @Test
    void shouldHandleAnalyzeResultFetchErrorInUserHistory() {
        EvaluationHistoryRecord completedRecord = new EvaluationHistoryRecord();
        completedRecord.setId(123);
        completedRecord.setInputText("completed text");
        completedRecord.setStatus("COMPLETED");

        when(evaluationHistoryRepository.findByUserIdOrderByCreatedAtDesc(42)).thenReturn(List.of(completedRecord));
        when(analyzeResultRepository.findByHistoryRecord_Id(123)).thenThrow(new RuntimeException("Analysis error"));

        List<FactCheckResultDto> result = businessLogicService.getUserHistory(42);

        assertEquals(1, result.size());
        FactCheckResultDto dto = result.getFirst();
        assertEquals("123", dto.getId());
        assertEquals(5, dto.getCurrentStep());
        assertEquals(5, dto.getAllSteps());
        assertNull(dto.getResult());

        verify(evaluationHistoryRepository, times(1)).findByUserIdOrderByCreatedAtDesc(42);
        verify(analyzeResultRepository, times(1)).findByHistoryRecord_Id(123);
    }
}
