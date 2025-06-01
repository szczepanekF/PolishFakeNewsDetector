from celery import Celery

from app.core.config import settings
from app.celery_worker.tasks.nlp.pipeline import get_nlp_pipeline

celery_app = Celery(
    "ml_analyze", broker=settings.BROKER_URI, backend="redis://redis:6379/0"
)

celery_app.conf.update(
    task_serializer="json",
    result_serializer="json",
    accept_content=["json"],
    task_track_started=True,
    result_expires=3600,
)
from app.core.log import get_logger

logger = get_logger(__name__)


@celery_app.task
def analyze_task(text: str) -> dict:
    logger.info("TASK STARTED")
    nlp = get_nlp_pipeline()
    logger.info("NLP PIPELINE LOADED")
    doc = nlp(text)
    result = {
        "text": doc._.clean_text,
        "sentiment": {
            "value": doc._.sentiment,
            "score": doc._.sentiment_score,
        },
    }
    logger.info(f"TEXT PROCESSED {result}")
    return result
