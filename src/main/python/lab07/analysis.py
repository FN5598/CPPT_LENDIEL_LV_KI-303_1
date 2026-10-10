"""Analyze the structural medicine contract without concrete-subtype branches."""

from collections.abc import Iterable
from typing import TypeVar

from .models import AvailableMedicine, MedicineSnapshot, ReportSummary

T = TypeVar("T", bound=AvailableMedicine)
_NUMERIC_METRICS = {"price", "days_left"}


def best_available(records: Iterable[T]) -> T | None:
    """Choose by polymorphic score; retain the first record on equal scores."""
    return max(records, key=lambda record: record.availability_score(), default=None)


def filter_records(records: Iterable[T], max_days_left: int = 30) -> list[T]:
    """Include the boundary, preserving the original record order."""
    return [record for record in records if record.days_left <= max_days_left]


def make_summary(
    records: list[AvailableMedicine],
    *metrics: str,
    max_days_left: int = 30,
    **labels: str,
) -> ReportSummary:
    """Aggregate validated objects into a frozen snapshot, preserving Lab 6 results."""
    unknown = set(metrics) - _NUMERIC_METRICS
    if unknown:
        raise ValueError(f"Unknown numeric metric: {sorted(unknown)[0]}")
    unknown_labels = set(labels) - set(metrics)
    if unknown_labels:
        raise ValueError(
            f"A label was provided for an inactive metric: {sorted(unknown_labels)[0]}"
        )
    selected = filter_records(records, max_days_left)
    snapshots = tuple(
        MedicineSnapshot(record.name, record.form, record.price, record.days_left)
        for record in selected
    )
    forms = frozenset(record.form for record in snapshots)
    return ReportSummary(
        source_count=len(records),
        selected=snapshots,
        form_counts=tuple(
            (form, sum(record.form == form for record in snapshots)) for form in sorted(forms)
        ),
        forms=forms,
        nearest_expiry=min(
            snapshots, key=lambda record: (record.days_left, record.name.casefold()), default=None
        ),
        average_price=sum(record.price for record in snapshots) / len(snapshots)
        if snapshots
        else None,
        metrics=tuple(
            sorted(
                {
                    labels.get(metric, metric): sum(
                        float(getattr(record, metric)) for record in snapshots
                    )
                    for metric in metrics
                }.items()
            )
        ),
        availability_scores=tuple(record.availability_score() for record in selected),
    )
