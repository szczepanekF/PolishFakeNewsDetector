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
1. A list of scores related to the sentence, such as sentiment or emotionality. Each score contains a `value` and a `confidence` score between 0 and 1.
2. A list of reference texts that may support or contradict the sentence. Each reference has a unique `reference_id`.
3. A sentence to evaluate.

Your task is to assess how truthful and reliable the sentence is based on the references and scored values, and return:
- A classification label,
- A short explanation in natural, fluent Polish,
- A list of references that were actually used in your explanation (with both `reference_id` and assigned `footnote_number`).

### Footnote guidelines:
- Assign footnote numbers starting from 1 to N, but **only to references you actually cite in the explanation**.
- In the explanation, cite references using footnote numbers like `[1]`, `[2]`, etc.
- Do **not mention reference_id** in the explanation.
- Refer to the **content of the reference**, not in meta form — e.g., do **not** say “this is confirmed in [1]”, but refer to the actual fact or claim from the reference, followed by the footnote number.

### Important reference usage rules:
- You may only use a reference if you are **confident** that it directly supports or contradicts a specific element of the evaluated sentence.
- References **should**, but are **not required to**, relate directly to the sentence — if none are clearly relevant, do **not use them**.
- Never force usage of a reference — cite only if its content is truly meaningful for the evaluation.

### Classification labels:
- 0 = UNCLASSIFIED
- 1 = TRUE
- 2 = MOSTLY_TRUE
- 3 = MANIPULATION
- 4 = MOSTLY_FALSE
- 5 = FALSE

### Requirements:
- Base your decision strictly on the provided reference texts and scored metrics.
- Do not rely on external knowledge or guesswork.
- In the explanation:
  - Refer only to **used** references via footnote numbers `[1]`, `[2]`, etc.
  - Mention the **content** of the reference, not that it “confirms” something.
  - Describe the influence of sentiment/emotionality **without numerical values** (e.g., “negatywny ton może sugerować uprzedzenie”).
- Write in short, factual, fluent, and friendly Polish.
- Avoid technical or harsh language.
- Choose one of the classification labels based on how well the sentence is supported by the evidence.

### Output format:
```json
{
  "label": 3,
  "explanation": "Choć wypowiedź opiera się na danych z raportu o liczbie zatrudnionych [1], pomija istotne informacje z ostatniego okresu [2].",
  "used_references": [
    {"reference_id": "123", "footnote_number": 1},
    {"reference_id": "45", "footnote_number": 2}
  ]
}
"""
