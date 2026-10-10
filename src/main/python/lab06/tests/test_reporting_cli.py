"""Tests for deterministic reports, file writing, and the command-line contract."""

from pathlib import Path

import pytest

from lab06.analysis import make_summary
from lab06.cli import main
from lab06.errors import DataError
from lab06.loader import load_records
from lab06.reporting import format_report, write_report


def sample_records():
    """Load the sample CSV used by application-level tests."""
    return load_records(Path(__file__).parents[1] / "data" / "medicines.csv")


def test_report_is_deterministic_and_contains_pharmacy_results() -> None:
    """Includes the threshold, counts, unique forms, nearest date, and mean price."""
    records = sample_records()
    summary = make_summary(
        records,
        "price",
        "days_left",
        price="Total price",
        days_left="Total days remaining",
    )
    report = format_report(summary)

    assert report == format_report(summary)
    assert "CSV records: 10" in report
    assert "Selected records: 9" in report
    assert "Liquid: 6" in report
    assert "Unique forms: Liquid, Pills" in report
    assert "Pakistani (Pills), 1 day remaining" in report
    assert "Average price of selected medicines: 4.24" in report
    assert "Total price: 38.20" in report
    assert "Total days remaining: 82.00" in report


def test_report_describes_empty_selection() -> None:
    """Renders an explicit empty-selection case without inventing values."""
    report = format_report(make_summary(sample_records(), max_days_left=-1), -1)

    assert "Selected records: 0" in report
    assert "Nearest expiry: no selected records" in report
    assert "Average price of selected medicines: no data" in report


def test_write_report_creates_parent_and_preserves_utf8(tmp_path) -> None:
    """Writes text exactly as UTF-8 and creates the parent directory."""
    output = tmp_path / "nested" / "report.txt"
    text = "Report ready — all checks passed\n"

    write_report(output, text)

    assert output.read_text(encoding="utf-8") == text


def test_write_report_wraps_os_error_and_preserves_cause(tmp_path) -> None:
    """Converts output errors to DataError while retaining the filesystem cause."""
    blocker = tmp_path / "file"
    blocker.write_text("not a directory", encoding="utf-8")

    try:
        write_report(blocker / "report.txt", "text\n")
    except DataError as error:
        assert isinstance(error.__cause__, OSError)
    else:
        raise AssertionError("Expected DataError for an impossible output path")


def test_cli_success_writes_report_and_returns_zero(tmp_path, capsys) -> None:
    """Runs the end-to-end CLI flow successfully with its default 30-day threshold."""
    source = Path(__file__).parents[1] / "data" / "medicines.csv"
    output = tmp_path / "reports" / "summary.txt"

    exit_code = main([str(source), str(output)])

    assert exit_code == 0
    assert "Records selected: 9" in capsys.readouterr().out
    assert "Pakistani" in output.read_text(encoding="utf-8")


def test_cli_bad_input_returns_nonzero_and_does_not_write_report(tmp_path, capsys) -> None:
    """Returns the documented error code for malformed data and writes no report."""
    source = tmp_path / "bad.csv"
    output = tmp_path / "report.txt"
    source.write_text(
        "name,form,price,days_left,prescription\nMedicine,Pills,invalid,3,false\n",
        encoding="utf-8",
    )

    exit_code = main([str(source), str(output)])

    captured = capsys.readouterr()
    assert exit_code == 2
    assert "Row 2: field price must be a number" in captured.err
    assert not output.exists()


def test_cli_invalid_utf8_returns_two_without_writing_report(tmp_path, capsys) -> None:
    source = tmp_path / "invalid-encoding.csv"
    output = tmp_path / "report.txt"
    source.write_bytes(b"name,form,price,days_left,prescription\n\xff,Pills,5,10,false\n")

    exit_code = main([str(source), str(output)])

    assert exit_code == 2
    assert "not valid UTF-8" in capsys.readouterr().err
    assert not output.exists()


@pytest.mark.parametrize(("threshold", "selected_count"), [(None, 9), (1, 1)])
def test_cli_named_paths_write_report(tmp_path, capsys, threshold, selected_count) -> None:
    source = Path(__file__).parents[1] / "data" / "medicines.csv"
    output = tmp_path / "named report.txt"
    args = ["--output", str(output), "--input", str(source)]
    if threshold is not None:
        args.extend(["--max-days-left", str(threshold)])

    assert main(args) == 0
    assert f"Records selected: {selected_count}" in capsys.readouterr().out
    assert f"Selected records: {selected_count}" in output.read_text(encoding="utf-8")


def test_cli_named_paths_return_two_for_invalid_csv(tmp_path, capsys) -> None:
    source = tmp_path / "bad.csv"
    output = tmp_path / "report.txt"
    source.write_text("wrong,header\n", encoding="utf-8")

    assert main(["--input", str(source), "--output", str(output)]) == 2
    assert "Invalid CSV header" in capsys.readouterr().err
    assert not output.exists()


def test_cli_help_lists_all_flags_without_requiring_paths(capsys) -> None:
    with pytest.raises(SystemExit) as exit_status:
        main(["--help"])

    assert exit_status.value.code == 0
    output = capsys.readouterr().out
    for flag in ("--help", "--version", "--input", "--output", "--max-days-left"):
        assert flag in output


def test_cli_version_does_not_require_paths(capsys) -> None:
    with pytest.raises(SystemExit) as exit_status:
        main(["--version"])

    assert exit_status.value.code == 0
    assert capsys.readouterr().out.strip() == "python -m lab06 6.0.0"


def test_cli_version_works_without_installed_metadata(monkeypatch, capsys) -> None:
    from importlib.metadata import PackageNotFoundError

    def missing_package(name):
        raise PackageNotFoundError(name)

    monkeypatch.setattr("lab06.cli.metadata.version", missing_package)
    with pytest.raises(SystemExit) as exit_status:
        main(["--version"])

    assert exit_status.value.code == 0
    assert capsys.readouterr().out.strip() == "python -m lab06 6.0.0"


@pytest.mark.parametrize(
    "args",
    [
        [],
        ["input.csv"],
        ["--input"],
        ["--input", "input.csv"],
        ["--output", "report.txt"],
        ["input.csv", "report.txt", "--input", "other.csv", "--output", "other.txt"],
    ],
)
def test_cli_incomplete_or_mixed_paths_are_usage_errors(args, capsys) -> None:
    with pytest.raises(SystemExit) as exit_status:
        main(args)

    assert exit_status.value.code == 2
    assert "error:" in capsys.readouterr().err
