from typing import Dict, Any

from app.core import get_logger
from app.db.models import ClaimEmbedding
from app.pika_utils import log_and_reply_step
from app.pipelines.base import Process

logger = get_logger(__name__)


class ClaimsInsertProcess(Process):
    @log_and_reply_step()
    def run(self, context: Dict[str, Any]) -> Dict[str, Any]:
        claims = context["claims"]
        embeddings = context["embeddings"]
        summaries = context["summaries"]

        for claim, embedding, summary in zip(claims, embeddings, summaries):
            entry = ClaimEmbedding(
                claim_id=claim.id, embedding=embedding, summary=summary
            )
            context["session"].add(entry)

        logger.info(
            f"{len(embeddings)} Claim Embeddings and Summaries inserted to database."
        )
        return context

    def get_name(self) -> str:
        return "Inserting claims embeddings and summaries to DB"
