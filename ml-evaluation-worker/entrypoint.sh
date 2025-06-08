#!/bin/bash
set -e

MODELS=(
    "nie3e/go-emotions-polish-gpt2-small-v0.0.1 models/emotion"
    "nie3e/sentiment-polish-gpt2-large models/sentiment"
)

for ITEM in "${MODELS[@]}"; do
    MODEL_NAME=$(echo "$ITEM" | awk '{print $1}')
    MODEL_DIR=$(echo "$ITEM" | awk '{print $2}')

    if [ -d "$MODEL_DIR" ] && [ "$(ls -A "$MODEL_DIR")" ]; then
        echo "$MODEL_NAME already exists in $MODEL_DIR, skipping download."
    else
        echo "Downloading model $MODEL_NAME -> $MODEL_DIR"
        python -c "from huggingface_hub import snapshot_download; snapshot_download('$MODEL_NAME', local_dir='$MODEL_DIR')"
    fi

    echo "----------------------------"
done

echo "ml-evaluation models download complete"

python app/pika_consumer.py