from functools import lru_cache
from typing import Dict, Any

from transformers import pipeline

from app.core import get_logger
from app.pika_utils import log_and_reply_step
from app.pipelines.base import Process
from app.schemas import ScoredValue

logger = get_logger(__name__)


@lru_cache(maxsize=1)
def _get_emotion_pipeline() -> pipeline:
    emotion_pipeline = pipeline(
        "text-classification",
        model="models/emotion",
    )
    logger.info("Initialized emotion pipeline")
    return emotion_pipeline


class EmotionDetectionProcess(Process):
    def get_name(self) -> str:
        return "Emotion Detection"

    @log_and_reply_step()
    def run(self, context: Dict[str, Any]) -> Dict[str, Any]:
        result = _get_emotion_pipeline()(context["text"])[0]
        context["result"].results["emotion"] = ScoredValue(
            value=result["label"].lower(), score=result["score"]
        )
        return context
