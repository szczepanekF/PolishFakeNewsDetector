from abc import ABC, abstractmethod
from typing import Dict, Any

from typing import List


class Process(ABC):
    """
    Abstract pipeline step.
    Each Process takes a context dict, modifies it, and returns it.
    """

    def __init__(self, process_id: int):
        self.id = process_id

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

    def get_id(self) -> int:
        """
        Returns: number of step
        """
        return self.id


class Pipeline(ABC):
    """
    Abstract pipeline.
    Holds a list of Process steps and runs them in order.
    """

    def __init__(self, steps: List[Process]):
        self.steps = steps

    def run(self, context: Dict[str, Any]) -> Dict[str, Any]:
        """
        Run the pipeline.
        Args:
            context (dict): Process context
        Returns:
            Final context after all steps
        """
        for step in self.steps:
            context = step.run(context)
        return context
