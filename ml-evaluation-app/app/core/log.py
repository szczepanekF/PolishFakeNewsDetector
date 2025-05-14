import logging.config
import os

LOGGING_CONFIG_PATH = os.path.join(
    os.path.dirname(os.path.abspath(__file__)), "logging.conf"
)
logging.config.fileConfig(LOGGING_CONFIG_PATH)


def get_logger(name: str):
    return logging.getLogger(name)
