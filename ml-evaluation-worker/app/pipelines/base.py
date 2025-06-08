from abc import ABC, abstractmethod
from typing import Dict, Any

from app.schemas import TaskResponse, AnalyzeResult


class Process(ABC):
    """
    Abstract pipeline step.
    Each Process takes a context dict, modifies it, and returns it.
    """

    @abstractmethod
    def run(self, context: Dict[str, Any]) -> Dict[str, Any]:
        """
        Run the process step.
        Args:
            context: current pipeline context
        Returns:
            Updated pipeline context
        """
        pass

    def get_name(self) -> str:
        """
        Returns: name of process

        """
        pass


from typing import List


class Pipeline(ABC):
    """
    Abstract pipeline.
    Holds a list of Process steps and runs them in order.
    """

    def __init__(self, steps: List[Process]):
        self.steps = steps

    def run(self, context: Dict[str, Any], log_fn: Dict) -> Dict[str, Any]:
        """
        Run the pipeline.
        Args:
            log_fn: log function and its parameters
            context: initial context
        Returns:
            Final context after all steps
        """
        log = log_fn.pop("fun")
        log_params = log_fn
        context["step_num"] = 0
        context["analyze_result"] = AnalyzeResult()
        for step in self.steps:
            context = step.run(context)
            context["step_num"] += 1
            log(
                **log_params,
                body=TaskResponse(
                    id=context["id"],
                    text=context["text"],
                    status=f"{context["step_num"]}. {step.get_name()}",
                    result=context["analyze_result"],
                ),
            )
        return context["analyze_result"]
