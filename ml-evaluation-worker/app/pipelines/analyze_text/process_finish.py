from typing import Dict, Any

from app.core import get_logger
from app.pika_utils import log_and_reply_step
from app.pipelines.base import Process

logger = get_logger(__name__)


class FinishProcess(Process):
    def get_name(self) -> str:
        return "Finished"

    @log_and_reply_step()
    def run(self, context: Dict[str, Any]) -> Dict[str, Any]:
        session = context.get("session")
        if session:
            session.close()
            logger.info(f"DB session closed.")
        return context
