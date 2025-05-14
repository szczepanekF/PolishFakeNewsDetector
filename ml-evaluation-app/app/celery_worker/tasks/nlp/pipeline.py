import spacy
from spacy import Language

_nlp = None


def get_nlp_pipeline() -> Language:
    global _nlp
    if _nlp is None:
        _nlp = spacy.load("pl_core_news_lg")
        _nlp.add_pipe("clean_text", first=True)
        _nlp.add_pipe("sentiment_analyzer", last=True)
    return _nlp
