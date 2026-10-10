"""Validated pharmacy entities, polymorphic availability, and immutable results."""

import math
from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Protocol


class AvailableMedicine(Protocol):
    """Structural contract consumed by analysis, including unrelated implementations."""

    @property
    def name(self) -> str:
        """Return the medicine name."""
        ...

    @property
    def form(self) -> str:
        """Return the dosage form."""
        ...

    @property
    def price(self) -> float:
        """Return the non-negative price."""
        ...

    @property
    def days_left(self) -> int:
        """Return the remaining shelf life."""
        ...

    def availability_score(self) -> float:
        """Return an educational availability index, not clinical advice."""
        ...


class Medicine(ABC):
    """Base entity: immutable labels and validated mutable price and shelf life."""

    def __init__(self, name: str, form: str, price: float, days_left: int) -> None:
        """Initialize common state using the same validation as later assignments."""
        if not isinstance(name, str) or not name.strip():
            raise ValueError("field name must not be empty")
        if not isinstance(form, str) or not form.strip():
            raise ValueError("field form must not be empty")
        self._name = name.strip()
        self._form = form.strip()
        self.price = price
        self.days_left = days_left

    @property
    def name(self) -> str:
        """Return the normalized, read-only name."""
        return self._name

    @property
    def form(self) -> str:
        """Return the normalized, read-only dosage form."""
        return self._form

    @property
    def price(self) -> float:
        """Return the validated price."""
        return self._price

    @price.setter
    def price(self, value: float) -> None:
        """Reject non-numbers, booleans, negative and non-finite prices atomically."""
        if isinstance(value, bool) or not isinstance(value, (int, float)):
            raise ValueError("field price must be a number")
        if not math.isfinite(value) or value < 0:
            raise ValueError("field price must be finite and non-negative")
        self._price = float(value)

    @property
    def days_left(self) -> int:
        """Return the remaining shelf life in whole days."""
        return self._days_left

    @days_left.setter
    def days_left(self, value: int) -> None:
        """Allow zero and positive integers; preserve state on invalid assignment."""
        if type(value) is not int:
            raise ValueError("field days_left must be an integer")
        if value < 0:
            raise ValueError("field days_left must not be negative")
        self._days_left = value

    @property
    @abstractmethod
    def prescription(self) -> bool:
        """Identify the subtype without allowing its identity to change."""

    @abstractmethod
    def availability_score(self) -> float:
        """Calculate the subtype's educational availability index."""

    def __str__(self) -> str:
        """Return a concise reader-facing description."""
        return f"{self.name} ({self.form}), {self.days_left} days remaining"

    def __repr__(self) -> str:
        """Return constructor-style diagnostic state."""
        return (
            f"{type(self).__name__}(name={self.name!r}, form={self.form!r}, "
            f"price={self.price!r}, days_left={self.days_left!r})"
        )

    def __eq__(self, other: object) -> bool:
        """Compare complete state within a subtype; defer incompatible comparisons."""
        if not isinstance(other, Medicine):
            return NotImplemented
        return type(self) is type(other) and (self.name, self.form, self.price, self.days_left) == (
            other.name,
            other.form,
            other.price,
            other.days_left,
        )

    __hash__ = None


class PrescriptionMedicine(Medicine):
    """Medicine requiring a prescription, with an availability coefficient of 0.5."""

    def __init__(self, name: str, form: str, price: float, days_left: int) -> None:
        """Reuse all common validation through super()."""
        super().__init__(name, form, price, days_left)

    @property
    def prescription(self) -> bool:
        """Prescription identity is fixed for this subtype."""
        return True

    def availability_score(self) -> float:
        """Return half the shelf-life factor, representing restricted availability."""
        return 0.5 * self.days_left / (self.days_left + 30)


class FreeMedicine(Medicine):
    """Over-the-counter medicine, with an availability coefficient of 1.0."""

    def __init__(self, name: str, form: str, price: float, days_left: int) -> None:
        """Reuse all common validation through super()."""
        super().__init__(name, form, price, days_left)

    @property
    def prescription(self) -> bool:
        """Prescription identity is fixed for this subtype."""
        return False

    def availability_score(self) -> float:
        """Return the shelf-life factor for unrestricted availability."""
        return self.days_left / (self.days_left + 30)


@dataclass(frozen=True, slots=True)
class MedicineSnapshot:
    """Capture report fields so later entity mutations cannot alter a summary."""

    name: str
    form: str
    price: float
    days_left: int


@dataclass(frozen=True, slots=True)
class ReportSummary:
    """Deeply immutable computed results; tuples replace mutable dictionaries/lists."""

    source_count: int
    selected: tuple[MedicineSnapshot, ...]
    form_counts: tuple[tuple[str, int], ...]
    forms: frozenset[str]
    nearest_expiry: MedicineSnapshot | None
    average_price: float | None
    metrics: tuple[tuple[str, float], ...]
    availability_scores: tuple[float, ...]

    @property
    def count(self) -> int:
        """Derive the count from the immutable selection."""
        return len(self.selected)
