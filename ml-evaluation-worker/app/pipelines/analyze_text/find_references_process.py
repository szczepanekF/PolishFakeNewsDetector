from typing import Dict, Any

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core import get_logger
from app.db.models import Claim, ClaimEmbedding
from app.pika_utils import log_and_reply_step
from app.pipelines.base import Process

logger = get_logger(__name__)


class FindReferencesProcess(Process):
    def get_name(self) -> str:
        return "Finding references"

    @log_and_reply_step()
    def run(self, context: Dict[str, Any]) -> Dict[str, Any]:
        session: Session = context["session"]
        query_vector = context["embedding"]

        cosine_dist = ClaimEmbedding.embedding.cosine_distance(query_vector).label(
            "score"
        )

        stmt = (
            select(Claim, cosine_dist)
            .join(ClaimEmbedding, Claim.id == ClaimEmbedding.claim_id)
            .where(cosine_dist <= 0.4)
            .order_by("score")
            .limit(5)
        )

        results = session.execute(stmt).all()

        for claim, score in results:
            logger.info(f"Reference: id={claim.id}, score={score}")

        context["claims"] = [claim for claim, _ in results]
        return context
