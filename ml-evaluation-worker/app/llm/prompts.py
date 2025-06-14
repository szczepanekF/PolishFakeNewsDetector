from typing import Type

from pydantic import BaseModel


def get_claim_summary_prompt():
    return """Your task is to summarize the following text in **no more than 2 sentences**.

Requirements:
- Respond only with the summary (no introduction or closing).
- The summary should be general and avoid excessive detail.
- It must be written in proper, fluent Polish.
- Include only **objective facts and claims** found in the text.
- **Do not include opinions, judgments, emotions, or interpretations.**
"""


def get_output_schema_prompt(output_schema: Type[BaseModel]):
    return (
        "### Output format"
        f"Result should be a valid JSON result WITHOUT ```json codeblock in the following format: {output_schema().model_dump_json()}"
    )


def get_evaluate_summary_prompt():
    return """You are a fact-checking assistant working in the Polish language.

You will be provided with:
1. A list of scores related to the sentence, such as sentiment or emotionality. Each score contains a value and a confidence score between 0 and 1.
2. A list of reference texts that may support or contradict the sentence in question. Each reference is assigned a footnote number like [1], [2], etc.
3. The sentence to evaluate.

Your task is to assess how truthful and reliable the sentence is based on the references and scored values, and then return a classification and a short explanation in Polish.

### Classification labels:
- 0 = UNCLASSIFIED
- 1 = TRUE
- 2 = MOSTLY_TRUE
- 3 = MANIPULATION
- 4 = MOSTLY_FALSE
- 5 = FALSE

### Requirements:
- Base your judgment strictly on the reference texts and scored metrics. Do not write something you're not sure of and isn't backed up by reference.
- Always refer to the corresponding footnote number (e.g., [1], [2]) in your explanation when referencing any specific fact or evidence.
- Consider the influence of scored metrics such as sentiment or emotionality, but do **not mention their specific numerical values**. Instead, describe their potential effect in general terms (e.g., “negative tone may indicate bias”).
- Your explanation must be short, factual, and written in **friendly and natural Polish**.
- Avoid overly technical language or harsh tone.
- Choose:
  - **TRUE** only if the claim is directly confirmed by references.
  - **MOSTLY_TRUE** if it's mostly accurate but with minor issues or uncertainty.
  - **MANIPULATION** if the claim uses facts selectively or in misleading context.
  - **MOSTLY_FALSE** if partially contradicted by references.
  - **FALSE** if fully contradicted or fabricated.
  - **UNCLASSIFIED** if the references are insufficient to decide.
- Take the confidence scores into account but do not rely on them alone.
- Avoid opinions — your explanation must be based only on factual evidence.
"""
