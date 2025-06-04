package com.pfnd.BusinessLogicService.service;

import com.pfnd.BusinessLogicService.model.dto.EvaluationHistoryDto;
import com.pfnd.BusinessLogicService.model.dto.FactCheckRequestDto;
import com.pfnd.BusinessLogicService.model.messages.FactCheckCommand;
import com.pfnd.BusinessLogicService.model.postgresql.EvaluationHistoryRecord;
import com.pfnd.BusinessLogicService.repository.EvalutationHistoryRepository;
import com.pfnd.BusinessLogicService.service.impl.BusinessLogicServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BusinessLogicServiceTest {

    private static final int USER_ID = 1;
    private static final FactCheckRequestDto request = new FactCheckRequestDto("test text");
    private EvaluationHistoryRecord record;
    @Mock
    private EvalutationHistoryRepository repository;

    @Mock
    private FactCheckRequestHandler factCheckRequestHandler;

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
        when(repository.saveAndFlush(any(EvaluationHistoryRecord.class))).thenReturn(record);

        EvaluationHistoryDto savedRecord = businessLogicService.initiateFactCheck(request, record.getUserId());

        assertEquals(savedRecord, new EvaluationHistoryDto(record));
        verify(factCheckRequestHandler, times(1)).requestEvaluation(any(FactCheckCommand.class));
    }

    @Test
    void shouldThrowAnErrorWhenSavingEvaluationHistoryRecordFailed() {
        when(repository.saveAndFlush(any(EvaluationHistoryRecord.class))).thenThrow(
                DataIntegrityViolationException.class);
        assertThrows(RuntimeException.class, () -> businessLogicService.initiateFactCheck(request, record.getUserId()));
        verify(factCheckRequestHandler, times(0)).requestEvaluation(any(FactCheckCommand.class));
    }

}
