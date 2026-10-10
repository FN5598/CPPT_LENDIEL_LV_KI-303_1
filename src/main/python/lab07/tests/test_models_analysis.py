"""Behavioral contracts for validation, polymorphism, snapshots and compatibility."""

import dataclasses
from pathlib import Path

import pytest

from lab07.analysis import best_available, filter_records, make_summary
from lab07.loader import load_records
from lab07.models import FreeMedicine, Medicine, PrescriptionMedicine
from lab07.reporting import format_report

DATA = Path(__file__).parents[1] / "data"


@pytest.mark.parametrize("value", [0, 1, 30, 31])
def test_days_setter_accepts_boundaries(value):
    medicine = FreeMedicine("A", "Pills", 2, 5)
    medicine.days_left = value
    assert medicine.days_left == value


@pytest.mark.parametrize("value", [-1, 1.5, True, "30", None])
def test_days_setter_rejects_invalid_values_atomically(value):
    medicine = FreeMedicine("A", "Pills", 2, 5)
    with pytest.raises(ValueError, match="field days_left"):
        medicine.days_left = value
    assert medicine.days_left == 5
    with pytest.raises(ValueError):
        FreeMedicine("A", "Pills", 2, value)


@pytest.mark.parametrize("value", [-1, float("nan"), float("inf"), True, "2", None])
def test_price_setter_rejects_invalid_values_atomically(value):
    medicine = FreeMedicine("A", "Pills", 2, 5)
    with pytest.raises(ValueError, match="field price"):
        medicine.price = value
    assert medicine.price == 2
    with pytest.raises(ValueError):
        FreeMedicine("A", "Pills", value, 5)


@pytest.mark.parametrize("value", [0, 2, 2.5])
def test_price_setter_accepts_nonnegative_values(value):
    medicine = FreeMedicine("A", "Pills", 2, 5)
    medicine.price = value
    assert medicine.price == value


@pytest.mark.parametrize("name,form", [("", "Pills"), ("A", "  "), (None, "Pills")])
def test_constructor_rejects_empty_labels(name, form):
    with pytest.raises(ValueError):
        FreeMedicine(name, form, 0, 0)


def test_identity_labels_are_normalized_and_read_only():
    medicine = PrescriptionMedicine(" A ", " Pills ", 0, 0)
    assert (medicine.name, medicine.form, medicine.prescription) == ("A", "Pills", True)
    for field, value in [("name", "B"), ("form", "Liquid"), ("prescription", False)]:
        with pytest.raises(AttributeError):
            setattr(medicine, field, value)
    with pytest.raises(TypeError):
        Medicine("A", "Pills", 0, 0)


def test_equality_str_repr_and_mutable_entities_are_unhashable():
    first = FreeMedicine("A", "Pills", 2, 30)
    same = FreeMedicine("A", "Pills", 2, 30)
    assert first == same
    assert first != PrescriptionMedicine("A", "Pills", 2, 30)
    assert first != FreeMedicine("A", "Pills", 3, 30)
    assert first != object()
    assert first.__eq__(object()) is NotImplemented
    assert str(first) == "A (Pills), 30 days remaining"
    assert repr(first) == "FreeMedicine(name='A', form='Pills', price=2.0, days_left=30)"
    with pytest.raises(TypeError):
        hash(first)
    same.days_left = 31
    assert first != same


def test_subtypes_have_distinct_scores_and_zero_boundary():
    free = FreeMedicine("A", "Pills", 2, 30)
    prescription = PrescriptionMedicine("A", "Pills", 2, 30)
    assert free.availability_score() == pytest.approx(0.5)
    assert prescription.availability_score() == pytest.approx(0.25)
    assert best_available([prescription, free]) is free
    free.days_left = 0
    prescription.days_left = 0
    assert free.availability_score() == prescription.availability_score() == 0
    assert best_available([]) is None
    assert best_available([prescription, free]) is prescription


def test_analysis_accepts_unrelated_duck_typed_object():
    class ExternalMedicine:
        name = "External"
        form = "Liquid"
        price = 3.0
        days_left = 1

        def availability_score(self):
            return 0.9

    external = ExternalMedicine()
    assert best_available([FreeMedicine("A", "Pills", 2, 30), external]) is external
    summary = make_summary([external])
    assert summary.count == 1
    assert summary.availability_scores == (0.9,)


def test_frozen_summary_and_nested_snapshot_survive_entity_mutation():
    medicine = FreeMedicine("A", "Pills", 2, 30)
    summary = make_summary([medicine], "price")
    assert summary == make_summary([medicine], "price")
    before = format_report(summary)
    with pytest.raises(dataclasses.FrozenInstanceError):
        summary.source_count = 4
    with pytest.raises(dataclasses.FrozenInstanceError):
        summary.selected[0].price = 99
    medicine.price = 10
    medicine.days_left = 31
    assert format_report(summary) == before
    assert summary.metrics == (("price", 2.0),)


def test_dataset_subtypes_duplicates_and_thresholds():
    records = load_records(DATA / "boundaries.csv")
    assert len(records) == 6
    assert isinstance(records[0], FreeMedicine)
    assert isinstance(records[1], PrescriptionMedicine)
    assert records[1] == records[2]
    assert records[0].name == "Vitamin C, effervescent"
    assert len(filter_records(records)) == 5
    summary = make_summary(records, "price", "days_left", price="Price", days_left="Days")
    assert summary.count == 5
    assert dict(summary.form_counts) == {"Liquid": 1, "Pills": 4}
    assert dict(summary.metrics) == {"Days": 91, "Price": 17}
    assert summary.nearest_expiry.days_left == 0


def test_empty_summary_has_no_invented_values():
    summary = make_summary([])
    assert summary.source_count == summary.count == 0
    assert summary.selected == summary.form_counts == summary.availability_scores == ()
    assert summary.forms == frozenset()
    assert summary.nearest_expiry is summary.average_price is None


def test_nearest_expiry_has_stable_name_tie_breaking():
    records = [FreeMedicine("Zulu", "Pills", 1, 3), FreeMedicine("alpha", "Pills", 2, 3)]
    assert make_summary(records).nearest_expiry.name == "alpha"


def test_unknown_metrics_and_labels_fail():
    with pytest.raises(ValueError, match="Unknown numeric metric"):
        make_summary([], "typo")
    with pytest.raises(ValueError, match="inactive metric"):
        make_summary([], price="Price")


@pytest.mark.parametrize("filename", ["medicines.csv", "boundaries.csv", "empty.csv"])
@pytest.mark.parametrize("threshold", [-1, 0, 1, 30, 31])
def test_report_is_exactly_compatible_with_lab06(filename, threshold):
    from lab06.analysis import make_summary as old_summary
    from lab06.loader import load_records as old_load
    from lab06.reporting import format_report as old_report

    options = {
        "max_days_left": threshold,
        "price": "Total price",
        "days_left": "Total days remaining",
    }
    source = DATA / filename
    expected = old_report(old_summary(old_load(source), "price", "days_left", **options), threshold)
    actual = format_report(
        make_summary(load_records(source), "price", "days_left", **options), threshold
    )
    assert actual == expected
