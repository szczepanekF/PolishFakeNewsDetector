from typing import Dict, Any

from app.core import get_logger
from app.nlp.pipeline import get_nlp_pipeline
from app.pipelines.base import Process
from app.schemas import ScoredValue

logger = get_logger(__name__)


class SentimentProcess(Process):
    def run(self, context: Dict[str, Any]) -> Dict[str, Any]:
        logger.debug(f"{context["id"]} | Sentiment | Analysis started")

        nlp = get_nlp_pipeline()
        logger.debug(f"{context["id"]} | Sentiment | NLP pipeline loaded")

        doc = nlp(context["text"])
        logger.debug(f"{context["id"]} | Sentiment | Text processed")

        context["analyze_result"].results["sentiment"] = ScoredValue(
            value=doc._.sentiment, score=doc._.sentiment_score
        )
        return context
