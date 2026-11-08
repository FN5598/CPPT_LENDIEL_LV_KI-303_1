from collections.abc import Iterable
from typing import TypedDict

from .models import MedicineRecord

_NUMERIC_METRICS = {"price", "days_left"}


class Summary(TypedDict):
    """Stable results consumed by the report formatter."""

    source_count: int
    selected: list[MedicineRecord]
    count: int
    form_counts: dict[str, int]
    forms: set[str]
    nearest_expiry: MedicineRecord | None
    average_price: float | None
    metrics: dict[str, float]


def filter_records(
    records: Iterable[MedicineRecord], max_days_left: int = 30
) -> list[MedicineRecord]:
    """Select records whose remaining shelf life is at most the given threshold."""
    return [record for record in records if record.days_left <= max_days_left]


def make_summary(
    records: list[MedicineRecord],
    *metrics: str,
    max_days_left: int = 30,
    **labels: str,
) -> Summary:
    """Summarize selected records and optional numeric metrics.

    ``max_days_left`` is the defaulted parameter, ``metrics`` captures positional
    metric names, and ``labels`` maps those names to display labels.
    """
    unknown_metrics = set(metrics) - _NUMERIC_METRICS
    if unknown_metrics:
        name = sorted(unknown_metrics)[0]
        raise ValueError(f"Unknown numeric metric: {name}")
    unknown_labels = set(labels) - set(metrics)
    if unknown_labels:
        name = sorted(unknown_labels)[0]
        raise ValueError(f"A label was provided for an inactive metric: {name}")

    selected = filter_records(records, max_days_left)
    form_counts = {
        form: sum(record.form == form for record in selected)
        for form in sorted({record.form for record in selected})
    }
    nearest = min(
        selected,
        key=lambda record: (record.days_left, record.name.casefold()),
        default=None,
    )
    average_price = sum(record.price for record in selected) / len(selected) if selected else None
    metric_totals = {
        labels.get(metric, metric): sum(float(getattr(record, metric)) for record in selected)
        for metric in metrics
    }

    return {
        "source_count": len(records),
        "selected": selected,
        "count": len(selected),
        "form_counts": form_counts,
        "forms": {record.form for record in selected},
        "nearest_expiry": nearest,
        "average_price": average_price,
        "metrics": metric_totals,
    }
