from sqlalchemy import text

from app.db.models import Base
from app.db import get_db_engine


def init_db():
    engine = get_db_engine()
    with engine.connect() as conn:
        conn.execute(text("CREATE EXTENSION IF NOT EXISTS vector;"))
        conn.commit()
    Base.metadata.create_all(bind=engine)


if __name__ == "__main__":
    init_db()
    print("Database tables initiated")
