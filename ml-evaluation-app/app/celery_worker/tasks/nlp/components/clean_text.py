import re
import html
from spacy.language import Language
from spacy.tokens import Doc
from bs4 import BeautifulSoup

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

if not Doc.has_extension("clean_text"):
    Doc.set_extension("clean_text", default=None)


@Language.component("clean_text")
def clean_text_component(doc: Doc) -> Doc:
    text = doc.text

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

    doc._.clean_text = text.strip()
    return doc
