from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    ENV: str
    BROKER_URI: str


settings = Settings()
