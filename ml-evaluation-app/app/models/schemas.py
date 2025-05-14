from typing import Optional, Any

from pydantic import BaseModel


class AnalyzeInput(BaseModel):
    text: str


class Sentiment(BaseModel):
    value: str
    score: float


class AnalyzeResult(BaseModel):
    text: str
    sentiment: Sentiment


class TaskStatusResponse(BaseModel):
    task_id: str
    status: str
    result: Optional[Any] = None


class TaskStartedResponse(BaseModel):
    task_id: str
