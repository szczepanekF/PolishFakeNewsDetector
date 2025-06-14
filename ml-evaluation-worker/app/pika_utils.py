import json
from functools import wraps
from typing import Callable, Union

import pika
from pika.adapters.blocking_connection import BlockingChannel
from pydantic import BaseModel

from app.schemas import TaskResponse


def send_reply(
    channel: BlockingChannel, reply_to: str, correlation_id: str, data: BaseModel | dict
):
    """
    Sends reply with data to specified queue
    Args:
        channel (BlockingChannel): channel to send reply to
        reply_to (str): queue to send reply to
        correlation_id (str): task id
        data (str): payload
    """
    if isinstance(data, BaseModel):
        body = data.model_dump_json().encode("utf-8")
    else:
        body = json.dumps(data).encode("utf-8")

    channel.basic_publish(
        exchange="",
        routing_key=reply_to,
        properties=pika.BasicProperties(
            correlation_id=correlation_id, content_type="application/json"
        ),
        body=body,
    )


def log_and_reply_step():
    """
    Decorator that logs and replies step result.
    Works only with class instance methods.
    """

    def decorator(func: Callable):
        @wraps(func)
        def wrapper(*args, **kwargs):
            self = args[0]
            context = args[1]

            result = func(*args, **kwargs)

            if context:
                logger = context.get("logger")
                channel = context.get("channel")
                reply_to = context.get("reply_to")
                correlation_id = context.get("correlation_id")

                body: Union[dict, BaseModel] = TaskResponse(
                    id=context["correlation_id"],
                    current_step=self.get_id(),
                    all_steps=context["all_steps"],
                    message=self.get_name(),
                    result=context.get("result"),
                )

                if logger:
                    logger.info(body)

                if channel and reply_to and correlation_id:
                    send_reply(channel, reply_to, correlation_id, body)

            return result

        return wrapper

    return decorator
