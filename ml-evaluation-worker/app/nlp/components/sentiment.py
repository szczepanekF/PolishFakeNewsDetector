from spacy.language import Language
from spacy.tokens import Doc
from transformers import pipeline

if not Doc.has_extension("sentiment"):
    Doc.set_extension("sentiment", default=None)
if not Doc.has_extension("sentiment_score"):
    Doc.set_extension("sentiment_score", default=None)

sentiment_pipeline = pipeline(
    "sentiment-analysis",
    model="models/sentiment",
)


# neutral/negative/positive/ambiguous
@Language.component("sentiment_analyzer")
def sentiment_component(doc: Doc) -> Doc:
    result = sentiment_pipeline(doc._.clean_text)[0]
    doc._.sentiment = result["label"].lower()
    doc._.sentiment_score = result["score"]
    return doc
