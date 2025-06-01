FROM python:3.12-slim

WORKDIR /app

RUN pip install poetry==1.8.3

COPY pyproject.toml poetry.lock ./

RUN poetry config virtualenvs.create false \
 && poetry lock --check \
 && poetry install --only main

COPY . .

RUN chmod +x entrypoint.sh
WORKDIR /app
ENV PYTHONPATH="/app"

<<<<<<<< HEAD:ml-evaluation-worker/Dockerfile
ENTRYPOINT ["bash", "-c", "./entrypoint.sh"]
========
RUN chmod +x entrypoint.sh
WORKDIR /app

ENTRYPOINT ["bash", "-c", "./entrypoint.sh && exec uvicorn app.main:app --host 0.0.0.0 --port 8000"]
>>>>>>>> dev:ml-evaluation-worker/Dockerfile_API
