#!/bin/bash
set -e  # exit on error

echo "Downloading spaCy model: pl_core_news_lg"
python -m spacy download pl_core_news_lg

MODEL_DIR="models/sentiment"

if [ -d "$MODEL_DIR" ] && [ "$(ls -A $MODEL_DIR)" ]; then
  echo "Transformers model already exists in $MODEL_DIR, skipping download."
else
  echo "Downloading Transformers model: nie3e/sentiment-polish-gpt2-large"
  python -c "\
from huggingface_hub import snapshot_download; \
snapshot_download('nie3e/sentiment-polish-gpt2-large', local_dir='$MODEL_DIR', local_dir_use_symlinks=False)"
fi

echo "ml-evaluation models download complete"

python app/pika_consumer.py