from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    BROKER_URI: str
    DB_URI: str

    OPENAI_API_URL: str
    OPENAI_API_KEY: str
    OPENAI_MODEL_NAME: str


settings = Settings()


def get_config():
    return settings
