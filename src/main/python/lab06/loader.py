import csv
import math
from pathlib import Path

from .errors import DataError
from .models import MedicineRecord

REQUIRED_FIELDS = ("name", "form", "price", "days_left", "prescription")
_TRUE_VALUES = {"true", "yes", "1"}
_FALSE_VALUES = {"false", "no", "0"}


def _parse_record(row: dict[str, str | None], row_number: int) -> MedicineRecord:
    """Convert one DictReader row, raising a field-specific error on invalid input."""
    name = (row.get("name") or "").strip()
    form = (row.get("form") or "").strip()
    if not name:
        raise DataError(f"Row {row_number}: field name must not be empty")
    if not form:
        raise DataError(f"Row {row_number}: field form must not be empty")

    try:
        price = float(row["price"] or "")
    except (KeyError, TypeError, ValueError) as error:
        raise DataError(f"Row {row_number}: field price must be a number") from error
    if not math.isfinite(price) or price < 0:
        raise DataError(f"Row {row_number}: field price must be finite and non-negative")

    try:
        days_left = int(row["days_left"] or "")
    except (KeyError, TypeError, ValueError) as error:
        raise DataError(f"Row {row_number}: field days_left must be an integer") from error
    if days_left < 0:
        raise DataError(f"Row {row_number}: field days_left must not be negative")

    prescription_value = (row.get("prescription") or "").strip().lower()
    if prescription_value in _TRUE_VALUES:
        prescription = True
    elif prescription_value in _FALSE_VALUES:
        prescription = False
    else:
        raise DataError(f"Row {row_number}: field prescription must be true/false, yes/no, or 1/0")

    return MedicineRecord(name, form, price, days_left, prescription)


def load_records(path: Path) -> list[MedicineRecord]:
    """Read and validate a CSV file; an empty file represents an empty dataset."""
    try:
        with path.open("r", encoding="utf-8", newline="") as source:
            reader = csv.DictReader(source, strict=True)
            if reader.fieldnames is None:
                return []
            if tuple(reader.fieldnames) != REQUIRED_FIELDS:
                raise DataError("Invalid CSV header: expected fields " + ",".join(REQUIRED_FIELDS))
            if len(set(reader.fieldnames)) != len(REQUIRED_FIELDS):
                raise DataError("CSV header contains duplicate fields")

            records: list[MedicineRecord] = []
            for row in reader:
                row_number = reader.line_num
                if None in row or any(value is None for value in row.values()):
                    raise DataError(f"Row {row_number}: field count does not match the CSV header")
                records.append(_parse_record(row, row_number))
            return records
    except OSError as error:
        raise DataError(f"Could not read CSV file: {path}") from error
    except UnicodeDecodeError as error:
        raise DataError(f"CSV file is not valid UTF-8: {path}") from error
    except csv.Error as error:
        raise DataError(f"Malformed CSV file: {path}") from error
