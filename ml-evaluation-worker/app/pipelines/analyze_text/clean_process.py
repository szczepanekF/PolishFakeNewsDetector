import html
import re
from typing import Dict, Any

from bs4 import BeautifulSoup

from app.pika_utils import log_and_reply_step
from app.pipelines.base import Process

EMOJI_PATTERN = re.compile(
    "[\U0001f600-\U0001f64f"  # emotikony
    "\U0001f300-\U0001f5ff"  # symbole i piktogramy
    "\U0001f680-\U0001f6ff"  # transport
    "\U0001f1e0-\U0001f1ff"  # flagi
    "]+",
    flags=re.UNICODE,
)

INVISIBLE_CHARS_PATTERN = re.compile(
    r"[\u200b\u200c\u200d\u2060\ufeff\u180e\u2028\u2029\u00ad]"
)


class CleanProcess(Process):
    def get_name(self) -> str:
        return "Text preparation"

    @log_and_reply_step()
    def run(self, context: Dict[str, Any]) -> Dict[str, Any]:
        text = context["text"]

        # remove html tags
        text = BeautifulSoup(text, "html.parser").get_text()

        # convert html characters
        text = html.unescape(text)

        # removing emoji
        text = EMOJI_PATTERN.sub("", text)

        # replace whitespace characters with single space
        text = re.sub(r"\s+", " ", text)

        # remove invisible characters
        text = INVISIBLE_CHARS_PATTERN.sub("", text)

        text = text.strip()

        context["text"] = text
        return context
