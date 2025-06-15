from sqlalchemy import create_engine
from sqlalchemy.engine import Engine
from functools import lru_cache
from app.core import get_config
from app.db.models import Base


@lru_cache()
def get_db_engine() -> Engine:
    """
    Returns: db engine
    """
    engine = create_engine(get_config().DB_URI)
    return engine
