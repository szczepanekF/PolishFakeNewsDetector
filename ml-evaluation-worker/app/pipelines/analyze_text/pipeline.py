from app.pipelines.analyze_text.emotion_detection_process import EmotionDetectionProcess, _get_emotion_pipeline
from app.pipelines.analyze_text.find_references_process import FindReferencesProcess
from app.pipelines.analyze_text.process_finish import FinishProcess
from app.pipelines.analyze_text.process_start import StartProcess
from app.pipelines.analyze_text.sentiment_process import SentimentProcess, _get_sentiment_pipeline
from app.pipelines.analyze_text.summarize_results_process import SummarizeResultsProcess
from app.pipelines.analyze_text.text_embedding_process import TextEmbeddingProcess
from app.pipelines.analyze_text.text_summary_process import TextSummaryProcess
from app.pipelines.base import Pipeline
from app.pipelines.analyze_text.clean_process import CleanProcess


class AnalyzePipeline(Pipeline):
    pass

_get_sentiment_pipeline()
_get_emotion_pipeline()


analyze_pipeline = AnalyzePipeline(
    steps=[
        StartProcess(1),
        CleanProcess(2),
        SentimentProcess(3),
        EmotionDetectionProcess(4),
        TextSummaryProcess(5),
        TextEmbeddingProcess(6),
        FindReferencesProcess(7),
        SummarizeResultsProcess(8),
        FinishProcess(9),
    ]
)
