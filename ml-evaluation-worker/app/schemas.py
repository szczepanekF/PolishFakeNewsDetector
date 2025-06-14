from datetime import datetime
from enum import Enum
from typing import Optional, Dict
from pydantic import BaseModel, Field


class ScoredValue(BaseModel):
    value: str
    score: float


class Reference(BaseModel):
    footnote_number: int = Field(...)
    source: str = Field(...)
    publication_date: datetime = Field(...)
    link: str = Field(...)


class ClassificationLabel(Enum):
    """
    Text evaluation classification labels
    """

    UNCLASSIFIED = 0
    TRUE = 1
    MOSTLY_TRUE = 2
    MANIPULATION = 3
    MOSTLY_FALSE = 4
    FALSE = 5


class AnalyzeResult(BaseModel):
    text: str = Field(...)
    final_score: float = Field(default=0.0)
    label: ClassificationLabel = Field(default=ClassificationLabel.UNCLASSIFIED)
    explanation: str = Field(default_factory=str)
    results: Dict[str, ScoredValue] = Field(default_factory=dict)
    references: list[Reference] = Field(default_factory=list)


class TaskResponse(BaseModel):
    id: str = Field(...)
    current_step: int = Field(...)
    all_steps: int = Field(...)
    message: str = Field(...)
    result: Optional[AnalyzeResult] = Field(default=None)


# LLM


class ClaimSummaryOutputSchema(BaseModel):
    summary: str = "<krótkie podsumowanie>"


class ReferenceInputSchema(BaseModel):
    reference_id: int
    content: str
    label: str


class EvaluationSummaryInputSchema(BaseModel):
    text: str = Field(...)
    metrics: Dict[str, ScoredValue] = Field(default_factory=dict)
    references: list[ReferenceInputSchema] = Field(default_factory=list)


class ReferenceFootnote(BaseModel):
    footnote_number: int = 0
    reference_id: int = 0


class EvaluationSummaryOutputSchema(BaseModel):
    label: ClassificationLabel = 0
    explanation: str = (
        "<krótkie, ale treściwe uzasadnienie decyzji, oparte na referencjach i ocenach>"
    )
    used_references: list[ReferenceFootnote] = [ReferenceFootnote()]
