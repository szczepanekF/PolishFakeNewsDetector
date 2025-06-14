from json import JSONDecodeError
from typing import Type

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
    """
    Simple chat completion function
    Args:
        system_prompt (str): System message (initial)
        user_prompt (str): User message
        output_schema (BaseModel): Schema to validate output
        max_retries (int): Number of retries

    Returns:
        Validated output in output_schema structure
    """
    system_prompt += get_output_schema_prompt(output_schema)
    for attempt in range(1, max_retries + 1):
        try:
            messages = [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ]
            logger.info(f"CLAIM SUMMARY REQUEST | {messages}")
            response = get_openai_client().chat.completions.create(
                model=settings.OPENAI_MODEL_NAME,
                messages=messages,
                temperature=0.7,
                max_tokens=200,
            )
            content = _clean_llm_output(response.choices[0].message.content.strip())
            logger.info(f"CLAIM SUMMARY RESPONSE | {content}")
            validated = output_schema.model_validate_json(content)
            return validated

        except (JSONDecodeError, ValidationError) as e:
            logger.warning(f"Attempt {attempt} failed to validate output. Error: {e}")
        except RateLimitError as e:
            logger.warning(f"Rate limit exceeded, waiting 1 minute: {e}")
            sleep(60)
            continue
    logger.error("Failed to get valid structured output after 3 attempts.")
    return None


def _clean_llm_output(text: str) -> str:
    text = text.strip()
    if text.startswith("```"):
        lines = text.splitlines()
        if len(lines) >= 2:
            return "\n".join(lines[1:-1]).strip()
    return text
