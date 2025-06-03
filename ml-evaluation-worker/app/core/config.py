from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    BROKER_URI: str


settings = Settings()


def get_config():
    return settings
