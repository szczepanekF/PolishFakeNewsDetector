from functools import lru_cache
from typing import Dict, Any

from sentence_transformers import SentenceTransformer

from app.core import get_logger
from app.pika_utils import log_and_reply_step
from app.pipelines.base import Process

logger = get_logger(__name__)


@lru_cache(maxsize=1)
def _get_embedding_model() -> SentenceTransformer:
    model = SentenceTransformer("models/text-encoder")
    logger.info("Initialized Embedding model")
    return model


class ClaimsEmbeddingProcess(Process):
    @log_and_reply_step()
    def run(self, context: Dict[str, Any]) -> Dict[str, Any]:
        model = _get_embedding_model()
        texts = context["summaries"]
        embeddings = model.encode(texts, normalize_embeddings=True).tolist()
        context["embeddings"] = embeddings
        logger.info(f"{len(context["embeddings"])} claims successfully embedded.")
        return context

    def get_name(self) -> str:
        return "Create claims embeddings"
