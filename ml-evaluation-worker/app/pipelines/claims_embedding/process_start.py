from sqlalchemy.orm import Session

from app.core import get_logger
from app.db import get_db_engine
from app.pika_utils import log_and_reply_step
from app.pipelines.base import Process

logger = get_logger(__name__)


class StartProcess(Process):
    @log_and_reply_step()
    def run(self, context):
        engine = get_db_engine()
        session = Session(bind=engine)
        context["session"] = session
        logger.info(f"DB session established: {session}")
        return context

    def get_name(self):
        return "Claims Embedding start"
