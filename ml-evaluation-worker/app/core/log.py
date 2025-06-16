import logging.config
import os
import logstash

from app.core.config import settings

LOGGING_CONFIG_PATH = os.path.join(
    os.path.dirname(os.path.abspath(__file__)), "logging.conf"
)
logging.config.fileConfig(LOGGING_CONFIG_PATH, disable_existing_loggers=False)

logstash_handler = logstash.TCPLogstashHandler(
    host=settings.LOGSTASH_URL.split(":")[0],
    port=int(settings.LOGSTASH_URL.split(":")[1]),
    version=1,
)

root_logger = logging.getLogger()
root_logger.addHandler(logstash_handler)

if logstash_handler not in root_logger.handlers:
    root_logger.addHandler(logstash_handler)


def get_logger(name: str):
    logger = logging.getLogger(name)
    return logger
