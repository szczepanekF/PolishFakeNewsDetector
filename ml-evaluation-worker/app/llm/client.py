from functools import lru_cache
from openai import OpenAI


from app.core.config import settings


@lru_cache(maxsize=1)
def get_openai_client():
    return OpenAI(api_key=settings.OPENAI_API_KEY, base_url=settings.OPENAI_API_URL)
