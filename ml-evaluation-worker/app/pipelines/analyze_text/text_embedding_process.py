from typing import Dict, Any

from app.core import get_logger
from app.pika_utils import log_and_reply_step
from app.pipelines.base import Process
from app.pipelines.models import get_embedding_model

logger = get_logger(__name__)


class TextEmbeddingProcess(Process):
    @log_and_reply_step()
    def run(self, context: Dict[str, Any]) -> Dict[str, Any]:
        model = get_embedding_model()
        text = context["text_summary"]
        embedding = model.encode(text, normalize_embeddings=True)
        context["embedding"] = embedding
        logger.info(f"Text successfully embedded.")
        return context

    def get_name(self) -> str:
        return "Create embedding"
