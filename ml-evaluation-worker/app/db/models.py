from datetime import datetime
from typing import List

from pgvector.sqlalchemy import Vector
from sqlalchemy import Integer, String, Text, ForeignKey
from sqlalchemy.orm import declarative_base, Mapped, mapped_column

Base = declarative_base()


class Claim(Base):
    __tablename__ = "claims"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    content: Mapped[str] = mapped_column(Text, nullable=False)
    source: Mapped[str] = mapped_column(String, nullable=True)
    label: Mapped[str] = mapped_column(String, nullable=True)
    publication_date: Mapped[datetime] = mapped_column(nullable=True)
    link: Mapped[str] = mapped_column(String, nullable=True)


class ClaimEmbedding(Base):
    __tablename__ = "claims_embeddings"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    claim_id: Mapped[int] = mapped_column(
        ForeignKey("claims.id"), nullable=False, index=True
    )
    summary: Mapped[str] = mapped_column(String, nullable=False)
    embedding: Mapped[List[float]] = mapped_column(Vector(1024))
