from functools import wraps
from typing import Callable


def log_step():
    """
    Basic decorator that logs step.
    """

    def decorator(func: Callable):
        @wraps(func)
        def wrapper(*args, **kwargs):
            self = args[0]
            context = args[1]

            result = func(*args, **kwargs)

            if context:
                logger = context.get("logger")
                if logger:
                    logger.info({"step_id": self.get_id, "step_name": self.get_name})

            return result

        return wrapper

    return decorator
