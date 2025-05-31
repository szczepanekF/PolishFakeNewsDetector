package com.pfnd.BusinessLogicService.model.messages;

import java.util.List;
import java.util.UUID;

enum Verdict {
    TRUTH,
    LIE,
    NOT_TRUTH_NOR_LIE
}

public record FactCheckResult(UUID id, String explanation, Verdict verdict, double score, List<String> sources) {
}
