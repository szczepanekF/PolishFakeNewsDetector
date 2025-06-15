from typing import Dict, Any

from app.core import get_logger
from app.pika_utils import log_and_reply_step
from app.pipelines.base import Process

logger = get_logger(__name__)


class FinishProcess(Process):
    @log_and_reply_step()
    def run(self, context: Dict[str, Any]) -> Dict[str, Any]:
        session = context.get("session")
        if session:
            try:
                session.commit()
                logger.info(f"DB session commited successfully.")
            except Exception as e:
                session.rollback()
                logger.error(f"Error while committing changes: {e}")
            finally:
                session.close()
                logger.info(f"DB session closed.")
        return context

    def get_name(self) -> str:
        return "Claims Embedding finished"
