"""Tests for typed CSV loading and input validation."""

import csv

import pytest

from lab07.errors import DataError
from lab07.loader import load_records


def test_loads_typed_records_and_preserves_quoted_comma(tmp_path) -> None:
    """Converts CSV strings to typed fields and handles an escaped comma."""
    source = tmp_path / "medicines.csv"
    source.write_text(
        'name,form,price,days_left,prescription\n"Vitamin C, effervescent",Pills,12.50,30,yes\n',
        encoding="utf-8",
    )

    record = load_records(source)[0]

    assert record.name == "Vitamin C, effervescent"
    assert record.form == "Pills"
    assert record.price == 12.5
    assert record.days_left == 30
    assert record.prescription is True


def test_empty_file_returns_empty_list(tmp_path) -> None:
    """Treats a zero-byte CSV as an empty dataset."""
    source = tmp_path / "empty.csv"
    source.write_text("", encoding="utf-8")

    assert load_records(source) == []


def test_header_only_file_returns_empty_list(tmp_path) -> None:
    """Treats a valid header with no records as an empty dataset."""
    source = tmp_path / "empty.csv"
    source.write_text("name,form,price,days_left,prescription\n", encoding="utf-8")

    assert load_records(source) == []


@pytest.mark.parametrize(
    ("field", "value", "message"),
    [
        ("name", "", "field name"),
        ("form", "", "field form"),
        ("price", "not-a-price", "field price"),
        ("price", "-1", "field price"),
        ("price", "inf", "field price"),
        ("days_left", "1.5", "field days_left"),
        ("days_left", "-1", "field days_left"),
        ("prescription", "sometimes", "field prescription"),
    ],
)
def test_invalid_single_field_reports_row_and_field(
    tmp_path, field: str, value: str, message: str
) -> None:
    """Identifies the row and field containing an invalid value."""
    source = tmp_path / "invalid.csv"
    row = {
        "name": "Medicine",
        "form": "Pills",
        "price": "10.50",
        "days_left": "20",
        "prescription": "false",
    }
    row[field] = value
    source.write_text(
        "name,form,price,days_left,prescription\n"
        + ",".join(row[key] for key in ("name", "form", "price", "days_left", "prescription"))
        + "\n",
        encoding="utf-8",
    )

    with pytest.raises(DataError, match=rf"Row 2: {message}"):
        load_records(source)


def test_wrong_header_is_rejected(tmp_path) -> None:
    """Rejects missing or extra CSV columns before reading data rows."""
    source = tmp_path / "wrong.csv"
    source.write_text("name,form,price\n", encoding="utf-8")

    with pytest.raises(DataError, match="Invalid CSV header"):
        load_records(source)


@pytest.mark.parametrize(
    "row",
    ["Medicine,Pills,5.00,10\n", "Medicine,Pills,5.00,10,false,extra\n"],
)
def test_missing_or_extra_row_value_is_rejected(tmp_path, row: str) -> None:
    """Rejects short rows and surplus columns with a row-numbered message."""
    source = tmp_path / "malformed.csv"
    source.write_text(
        "name,form,price,days_left,prescription\n" + row,
        encoding="utf-8",
    )

    with pytest.raises(DataError, match="Row 2: field count"):
        load_records(source)


def test_malformed_csv_preserves_csv_error_as_cause(tmp_path) -> None:
    """Wraps malformed quoting and retains the csv module's original exception."""
    source = tmp_path / "malformed.csv"
    source.write_text(
        'name,form,price,days_left,prescription\n"Unclosed,Pills,5,10,false\n',
        encoding="utf-8",
    )

    with pytest.raises(DataError, match="Malformed CSV file") as error:
        load_records(source)
    assert isinstance(error.value.__cause__, csv.Error)


def test_missing_file_preserves_os_error_as_cause(tmp_path) -> None:
    """Wraps filesystem failures as DataError and keeps the original cause."""
    with pytest.raises(DataError, match="Could not read CSV file") as error:
        load_records(tmp_path / "missing.csv")
    assert isinstance(error.value.__cause__, OSError)


def test_invalid_utf8_preserves_decoding_error_as_cause(tmp_path) -> None:
    source = tmp_path / "invalid-encoding.csv"
    source.write_bytes(b"name,form,price,days_left,prescription\nM\xff,Pills,5,10,false\n")

    with pytest.raises(DataError, match="not valid UTF-8") as error:
        load_records(source)
    assert isinstance(error.value.__cause__, UnicodeDecodeError)


@pytest.mark.parametrize(
    ("prefix", "line_number"),
    [
        ("\n", 3),
        ('"Vitamin\nC",Pills,5,10,false\n\n', 5),
    ],
)
@pytest.mark.parametrize(
    ("row", "message"),
    [
        ("Medicine,Pills,bad,10,false\n", "field price"),
        ("Medicine,Pills,5,10\n", "field count"),
    ],
)
def test_validation_errors_use_source_lines_after_blank_or_multiline_records(
    tmp_path, prefix: str, line_number: int, row: str, message: str
) -> None:
    source = tmp_path / "source-lines.csv"
    source.write_text(
        "name,form,price,days_left,prescription\n" + prefix + row,
        encoding="utf-8",
    )

    with pytest.raises(DataError, match=rf"Row {line_number}: {message}"):
        load_records(source)
