import asyncio
from typing import Dict, Any

from app.core import get_logger
from app.llm.prompts import get_claim_summary_prompt
from app.llm.requests import batch_chat_completions
from app.pika_utils import log_and_reply_step
from app.pipelines.base import Process
from app.schemas import ClaimSummaryOutputSchema

logger = get_logger(__name__)


class ClaimsSummaryProcess(Process):
    @log_and_reply_step()
    def run(self, context: Dict[str, Any]) -> Dict[str, Any]:

        claims = context["claims"]
        texts = [claim.cleaned_content for claim in claims]
        summaries = asyncio.run(
            batch_chat_completions(
                system_prompt=get_claim_summary_prompt(),
                user_prompts=texts,
                output_schema=ClaimSummaryOutputSchema,
            )
        )
        summaries = [s.summary if s is not None else "" for s in summaries]
        context["summaries"] = summaries
        logger.info(f"{len(context["summaries"])} claims successfully summarized.")
        return context

    def get_name(self) -> str:
        return "Summarize claims"
