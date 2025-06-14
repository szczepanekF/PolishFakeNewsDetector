import asyncio
from json import JSONDecodeError
from typing import Type, List

from time import sleep

import httpx
from openai import RateLimitError
from pydantic import ValidationError, BaseModel

from app.core import get_logger
from app.core.config import settings
from app.llm import get_openai_client
from app.llm.prompts import get_output_schema_prompt

logger = get_logger(__name__)


def chat_completion(
    system_prompt: str,
    user_prompt: str,
    output_schema: Type[BaseModel],
    max_retries: int = 3,
) -> BaseModel | None:
    system_prompt += get_output_schema_prompt(output_schema)

    for attempt in range(1, max_retries + 1):
        try:
            messages = [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ]
            logger.info(f"[Attempt {attempt}] CLAIM SUMMARY REQUEST | {messages}")
            response = get_openai_client().chat.completions.create(
                model=settings.OPENAI_MODEL_NAME,
                messages=messages,
                temperature=0.7,
                max_tokens=1000,
            )
            content = _clean_llm_output(response.choices[0].message.content.strip())
            logger.info(f"CLAIM SUMMARY RESPONSE | {content}")
            return output_schema.model_validate_json(content)

        except (JSONDecodeError, ValidationError) as e:
            logger.warning(f"Validation failed (attempt {attempt}): {e}")
        except RateLimitError:
            wait = 30
            logger.warning(f"Rate limit (attempt {attempt}), sleeping {wait:.1f}s")
            sleep(wait)
        sleep(1)

    logger.error("Failed to get valid structured output after retries.")
    return None


async def batch_chat_completions(
    system_prompt: str,
    user_prompts: List[str],
    output_schema: Type[BaseModel],
    max_retries: int = 3,
    concurrency_limit: int = 10,
) -> List[BaseModel | None]:
    semaphore = asyncio.Semaphore(concurrency_limit)

    async def wrapped_chat_completion(user_prompt: str):
        async with semaphore:
            return await asyncio.to_thread(
                chat_completion,
                system_prompt,
                user_prompt,
                output_schema,
                max_retries,
            )

    tasks = [wrapped_chat_completion(prompt) for prompt in user_prompts]
    return await asyncio.gather(*tasks)


def _clean_llm_output(text: str) -> str:
    text = text.strip()
    if text.startswith("```"):
        lines = text.splitlines()
        if len(lines) >= 2:
            return "\n".join(lines[1:-1]).strip()
    return text
