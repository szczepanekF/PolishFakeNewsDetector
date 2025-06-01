import json

import pika
from pika.adapters.blocking_connection import BlockingChannel
from pika.spec import Basic, BasicProperties
from pydantic import BaseModel

from app.nlp.pipeline import get_nlp_pipeline
from app.core.config import settings
from app.core.log import get_logger
from app.models.schemas import TaskResponse, AnalyzeResult, ScoredValue

logger = get_logger(__name__)

connection = pika.BlockingConnection(pika.URLParameters(settings.BROKER_URI))
channel = connection.channel()

channel.queue_declare(queue="analyze_tasks", durable=True)


def log_and_reply(reply_to: str, correlation_id: str, body: dict | BaseModel):
    """
    Logs body as info and sends it as reply to broker queue.
    Args:
        reply_to (str): broker queue to send reply to
        correlation_id (str): id
        body (dict | BaseModel): data to log
    """
    logger.info(body)
    send_reply(reply_to, correlation_id, body)


def on_message(
    ch: BlockingChannel, method: Basic.Deliver, properties: BasicProperties, body: bytes
):
    """
    Function handling messages received from broker on "analyze_tasks" queue.
    Args:
        ch: Channel that's being listened to
        method: Delivery metadata about this message (delivery_tag, routing_key, exchange)
        properties: Message properties (reply_to, correlation_id, content_type, headers)
        body: Message payload
    """
    reply_to = properties.reply_to
    correlation_id = properties.correlation_id

    request = json.loads(body)
    text = request.get("text", "")

    log_and_reply(
        reply_to,
        correlation_id,
        TaskResponse(id=correlation_id, text=text, status="0. STARTED"),
    )

    results = {}

    # 1. SENTIMENT
    log_and_reply(
        reply_to,
        correlation_id,
        TaskResponse(id=correlation_id, text=text, status="1. SENTIMENT"),
    )
    results["sentiment"] = analyze_sentiment(correlation_id, text)

    # SUCCESS
    result = AnalyzeResult(overall_score=0.0, results=results, references=[])
    log_and_reply(
        reply_to,
        correlation_id,
        TaskResponse(id=correlation_id, text=text, status="SUCCESS", result=result),
    )

    ch.basic_ack(delivery_tag=method.delivery_tag)


def analyze_sentiment(correlation_id: str, text: str) -> ScoredValue:
    """
    Analyze sentiment step
    Args:
        correlation_id (str): task id
        text (str): text being analyzed

    Returns:
        ScoredValue: classification's label and score
    """
    logger.debug(f"{correlation_id} | Sentiment | Analysis started")

    nlp = get_nlp_pipeline()
    logger.debug(f"{correlation_id} | Sentiment | NLP pipeline loaded")

    doc = nlp(text)
    logger.debug(f"{correlation_id} | Sentiment | Text processed")

    return ScoredValue(value=doc._.sentiment, score=doc._.sentiment_score)


def send_reply(reply_to: str, correlation_id: str, data: BaseModel | dict):
    """
    Sends reply with data to specified queue
    Args:
        reply_to (str): queue to send reply to
        correlation_id (str): task id
        data (str): payload
    """
    logger.info(f"Sending reply to {reply_to} correlation_id={correlation_id}")
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


channel.basic_consume(queue="analyze_tasks", on_message_callback=on_message)
logger.info("Waiting for analyze_tasks...")
channel.start_consuming()
