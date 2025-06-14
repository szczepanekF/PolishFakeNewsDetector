from typing import Dict, Any

from sqlalchemy import select, outerjoin

from app.core import get_logger
from app.db.models import ClaimEmbedding, Claim
from app.pika_utils import log_and_reply_step
from app.pipelines.base import Process

logger = get_logger(__name__)


class RetrieveNewClaimsProcess(Process):
    @log_and_reply_step()
    def run(self, context: Dict[str, Any]) -> Dict[str, Any]:
        stmt = (
            select(Claim)
            .select_from(
                outerjoin(Claim, ClaimEmbedding, Claim.id == ClaimEmbedding.claim_id)
            )
            .where(ClaimEmbedding.claim_id == None)
        )
        claims = context["session"].execute(stmt).scalars().all()
        context["claims"] = claims
        logger.info(f"{len(context["claims"])} unprocessed claims retrieved.")
        return context

    def get_name(self) -> str:
        return "Retrieve claims embeddings from DB"
