import spacy
from spacy import Language

from app.core.log import get_logger

_nlp = None

logger = get_logger(__name__)


def get_nlp_pipeline() -> Language:
    global _nlp
    if _nlp is None:
        _nlp = spacy.load("pl_core_news_lg")
        _nlp.add_pipe("clean_text", first=True)
        _nlp.add_pipe("sentiment_analyzer", last=True)
        logger.info("Initialized nlp pipeline")
    return _nlp
