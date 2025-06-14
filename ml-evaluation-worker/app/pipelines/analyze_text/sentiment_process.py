from functools import lru_cache
from typing import Dict, Any

from transformers import pipeline

from app.core import get_logger

from app.pika_utils import log_and_reply_step
from app.pipelines.base import Process
from app.schemas import ScoredValue

logger = get_logger(__name__)


@lru_cache(maxsize=1)
def _get_sentiment_pipeline() -> pipeline:
    sentiment_pipeline = pipeline(
        "sentiment-analysis",
        model="models/sentiment",
    )
    logger.info("Initialized sentiment pipeline")
    return sentiment_pipeline


class SentimentProcess(Process):
    def get_name(self) -> str:
        return "Sentiment Analysis"

    @log_and_reply_step()
    def run(self, context: Dict[str, Any]) -> Dict[str, Any]:
        result = _get_sentiment_pipeline()(context["text"])[0]
        context["result"].results["sentiment"] = ScoredValue(
            value=result["label"].lower(), score=result["score"]
        )
        return context
