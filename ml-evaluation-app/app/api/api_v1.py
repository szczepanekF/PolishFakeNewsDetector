from celery.result import AsyncResult
from fastapi import APIRouter

from app.models.schemas import AnalyzeInput, TaskStatusResponse, TaskStartedResponse
from app.celery_worker.celery_app import analyze_task, celery_app

api_router = APIRouter()


@api_router.post("/analyze", response_model=TaskStartedResponse)
def start_analyze(request: AnalyzeInput) -> TaskStartedResponse:
    task: AsyncResult = analyze_task.delay(request.text)
    return TaskStartedResponse(
        task_id=task.id,
    )


@api_router.get("/analyze/{task_id}", response_model=TaskStatusResponse)
def get_task_result(task_id: str) -> TaskStatusResponse:
    result = AsyncResult(task_id, app=celery_app)
    return TaskStatusResponse(
        task_id=task_id,
        status=result.status,
        result=result.result if result.ready() else None,
    )
