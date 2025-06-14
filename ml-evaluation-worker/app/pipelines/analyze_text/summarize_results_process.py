from typing import Dict, Any

from app.llm.prompts import get_evaluate_summary_prompt
from app.llm.requests import chat_completion
from app.pika_utils import log_and_reply_step
from app.pipelines.base import Process
from app.schemas import (
    EvaluationSummaryOutputSchema,
    EvaluationSummaryInputSchema,
    ReferenceInputSchema,
    Reference,
)


class SummarizeResultsProcess(Process):
    def get_name(self) -> str:
        return "Results summary and final evaluation"

    @log_and_reply_step()
    def run(self, context: Dict[str, Any]) -> Dict[str, Any]:
        final_score = 0.0
        for step_name, result in context["result"].results.items():
            final_score += result.score
        final_score /= len(context["result"].results)

        llm_result = chat_completion(
            system_prompt=get_evaluate_summary_prompt(),
            user_prompt=EvaluationSummaryInputSchema(
                text=context["result"].text,
                metrics=context["result"].results,
                references=[
                    ReferenceInputSchema(
                        id=claim.id,
                        footnote_number=ft_num + 1,
                        content=claim.content,
                        label=claim.label,
                    )
                    for ft_num, claim in enumerate(context["claims"])
                ],
            ).model_dump_json(),
            output_schema=EvaluationSummaryOutputSchema,
        )
        context["result"].final_score = final_score
        context["result"].explanation = llm_result.explanation
        context["result"].label = llm_result.label

        used_references_ids = llm_result.used_references_ids
        context["result"].references = [
            Reference(
                source=claim.source,
                publication_date=claim.publication_date,
                link=claim.link,
            )
            for claim in context["claims"]
            if claim.id in used_references_ids
        ]
        return context
