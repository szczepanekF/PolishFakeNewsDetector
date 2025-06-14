from app.pipelines.base import Pipeline
from app.pipelines.claims_embedding.claims_insert_process import ClaimsInsertProcess
from app.pipelines.claims_embedding.claims_embedding_process import (
    ClaimsEmbeddingProcess,
)
from app.pipelines.claims_embedding.claims_summary_process import ClaimsSummaryProcess
from app.pipelines.claims_embedding.clean_process import CleanProcess
from app.pipelines.claims_embedding.process_finish import FinishProcess
from app.pipelines.claims_embedding.process_start import StartProcess
from app.pipelines.claims_embedding.retreive_new_claims_process import (
    RetrieveNewClaimsProcess,
)


class ClaimsEmbeddingPipeline(Pipeline):
    pass


claims_embedding_pipeline = ClaimsEmbeddingPipeline(
    steps=[
        StartProcess(1),
        RetrieveNewClaimsProcess(2),
        CleanProcess(3),
        ClaimsSummaryProcess(4),
        ClaimsEmbeddingProcess(5),
        ClaimsInsertProcess(6),
        FinishProcess(7),
    ]
)
