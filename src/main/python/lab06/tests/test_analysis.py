"""Tests for pharmacy-record filtering, aggregation, and flexible metrics."""

import pytest

from lab06.analysis import filter_records, make_summary
from lab06.loader import load_records
from lab06.models import MedicineRecord


def sample_records() -> list[MedicineRecord]:
    """Load the checked-in sample dataset from a stable package-relative path."""
    from pathlib import Path

    return load_records(Path(__file__).parents[1] / "data" / "medicines.csv")


def test_filter_includes_threshold_boundary_and_excludes_later_expiry() -> None:
    """Implements days_left <= threshold, including exactly 30 days."""
    records = sample_records()

    selected = filter_records(records)

    assert len(selected) == 9
    assert all(record.days_left <= 30 for record in selected)
    assert any(record.days_left == 30 for record in selected)
    assert all(record.name != "Beyond" for record in selected)


def test_empty_selection_has_zero_counts_and_no_optional_results() -> None:
    """Returns a meaningful empty summary when no rows meet the threshold."""
    summary = make_summary(sample_records(), max_days_left=-1)

    assert summary["count"] == 0
    assert summary["form_counts"] == {}
    assert summary["forms"] == set()
    assert summary["nearest_expiry"] is None
    assert summary["average_price"] is None


def test_empty_input_has_empty_summary() -> None:
    """Handles an empty input collection without division or min errors."""
    summary = make_summary([])

    assert summary["source_count"] == 0
    assert summary["selected"] == []
    assert summary["count"] == 0
    assert summary["average_price"] is None


def test_summary_counts_forms_and_finds_nearest_expiry() -> None:
    """Uses a list, a dictionary, a set, and stable tie handling for results."""
    summary = make_summary(sample_records())

    assert summary["form_counts"] == {"Liquid": 6, "Pills": 3}
    assert summary["forms"] == {"Liquid", "Pills"}
    assert summary["nearest_expiry"].name == "Pakistani"
    assert summary["average_price"] == pytest.approx(38.2 / 9)


def test_duplicate_form_is_counted_but_remains_unique_in_set() -> None:
    """Counts duplicate form rows while retaining one copy in the unique-form set."""
    summary = make_summary(sample_records())

    assert summary["form_counts"]["Pills"] == 3
    assert len(summary["forms"]) == 2


def test_summary_accepts_args_default_parameter_and_kwargs_labels() -> None:
    """Checks the required positional metrics, threshold default, and label mapping."""
    records = [MedicineRecord("Medicine", "Pills", 15.0, 4, False)]

    summary = make_summary(
        records,
        "price",
        "days_left",
        price="Price",
        days_left="Days",
    )

    assert summary["metrics"] == {"Price": 15.0, "Days": 4.0}
    assert make_summary(records)["count"] == 1


def test_unknown_metric_and_unmatched_label_are_rejected() -> None:
    """Avoids silently accepting misspelled metric names or labels."""
    records = sample_records()

    with pytest.raises(ValueError, match="Unknown numeric metric: typo"):
        make_summary(records, "typo")
    with pytest.raises(ValueError, match="A label was provided for an inactive metric: price"):
        make_summary(records, price="Price")
