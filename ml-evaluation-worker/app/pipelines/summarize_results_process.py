from functools import lru_cache
from typing import Dict, Any

from transformers import pipeline

from app.core import get_logger
from app.pipelines.base import Process
from app.schemas import ScoredValue, ClassificationLabel

logger = get_logger(__name__)


class SummarizeResultsProcess(Process):
    def get_name(self) -> str:
        return "Results summary"

    def run(self, context: Dict[str, Any]) -> Dict[str, Any]:
        final_score = 0.0
        for step_name, result in context["analyze_result"].results.items():
            final_score += result.score
        final_score /= len(context["analyze_result"].results)

        context["analyze_result"].final_score = final_score
        context["analyze_result"].explanation = ""
        context["analyze_result"].label = ClassificationLabel.UNCLASSIFIED
        return context
