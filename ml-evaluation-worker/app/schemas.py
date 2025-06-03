from enum import Enum
from typing import Optional, Dict
from pydantic import BaseModel, Field


class ScoredValue(BaseModel):
    value: str
    score: float


class Reference(BaseModel):
    id: int
    title: str
    url: str


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
    final_score: float = Field(default=0.0)
    label: ClassificationLabel = Field(default=ClassificationLabel.UNCLASSIFIED)
    explanation: str = Field(default_factory=str)
    results: Dict[str, ScoredValue] = Field(default_factory=dict)
    references: list[Reference] = Field(default_factory=list)


class TaskResponse(BaseModel):
    id: str = Field(...)
    text: str = Field(...)
    status: str = Field(...)
    result: Optional[AnalyzeResult] = Field(default=None)
