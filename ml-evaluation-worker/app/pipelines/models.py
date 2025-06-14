from functools import lru_cache

from sentence_transformers import SentenceTransformer

from app.core import get_logger

logger = get_logger(__name__)


@lru_cache(maxsize=1)
def get_embedding_model() -> SentenceTransformer:
    model = SentenceTransformer("models/text-encoder")
    logger.info("Initialized Embedding model")
    return model
