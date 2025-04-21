import sys
import traceback

from fastapi import FastAPI
from starlette.requests import Request
from starlette.responses import JSONResponse

from app.api.api_v1 import api_router
from app.core.log import get_logger

logger = get_logger(__name__)
app = FastAPI()

@app.exception_handler(Exception)
async def exception_handler(request: Request, exc: Exception):
    exc_type, exc_value, exc_tb = sys.exc_info()
    formatted_traceback = "".join(
        traceback.format_exception(exc_type, exc_value, exc_tb)
    )

    logger.error(f"Unexpected error occurred: {exc}\n{formatted_traceback}")

    return JSONResponse(
        status_code=500,
        content={"message": "Internal Server Error", "detail": str(exc)},
    )


app.include_router(api_router, prefix="/api/v1")

@app.get("/health")
async def health_check():
    return {"status": "ok"}
