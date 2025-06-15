from app.core import get_config, get_logger
from app.pipelines.analyze_text import analyze_pipeline
from app.pipelines.claims_embedding import claims_embedding_pipeline

logger = get_logger(__name__)
logger.info("Initiating pika consumer...")

import json

import pika
from pika.adapters.blocking_connection import BlockingChannel
from pika.spec import Basic, BasicProperties


def on_message_analyze_tasks(
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

    analyze_pipeline.run(
        {
            "channel": channel,
            "correlation_id": correlation_id,
            "reply_to": reply_to,
            "logger": logger,
            channel: channel,
            "all_steps": len(analyze_pipeline.steps),
            "text": text,
        },
    )

    ch.basic_ack(delivery_tag=method.delivery_tag)


def on_message_claims_embedding(
    ch: BlockingChannel, method: Basic.Deliver, properties: BasicProperties, body: bytes
):
    """
    Function handling messages received from broker on "claims_embedding" queue.
    Args:
        ch: Channel that's being listened to
        method: Delivery metadata about this message (delivery_tag, routing_key, exchange)
        properties: Message properties (reply_to, correlation_id, content_type, headers)
        body: Message payload
    """
    reply_to = properties.reply_to
    correlation_id = properties.correlation_id

    claims_embedding_pipeline.run(
        {
            "channel": channel,
            "correlation_id": correlation_id,
            "reply_to": reply_to,
            "logger": logger,
            channel: channel,
            "all_steps": len(claims_embedding_pipeline.steps),
        },
    )

    ch.basic_ack(delivery_tag=method.delivery_tag)


connection = pika.BlockingConnection(pika.URLParameters(get_config().BROKER_URI))
channel = connection.channel()

channel.queue_declare(queue="analyze_tasks", durable=True)
channel.basic_consume(
    queue="analyze_tasks", on_message_callback=on_message_analyze_tasks
)

channel.queue_declare(queue="run_claims_embedding", durable=True)
channel.basic_consume(
    queue="run_claims_embedding", on_message_callback=on_message_claims_embedding
)
logger.info("Waiting for tasks...")
channel.start_consuming()
