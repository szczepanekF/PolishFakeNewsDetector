from typing import Dict, Any

from sqlalchemy.orm import Session

from app.core import get_logger
from app.db import get_db_engine
from app.pika_utils import log_and_reply_step
from app.pipelines.base import Process
from app.schemas import AnalyzeResult

logger = get_logger(__name__)


class StartProcess(Process):
    def get_name(self) -> str:
        return "Started"

    @log_and_reply_step()
    def run(self, context: Dict[str, Any]) -> Dict[str, Any]:
        engine = get_db_engine()
        session = Session(bind=engine)
        context["session"] = session
        logger.info(f"DB session established: {session}")
        context["result"] = AnalyzeResult(text=context["text"])
        return context
