from pathlib import Path

from .models import ReportSummary
from .errors import DataError


def format_report(summary: ReportSummary, max_days_left: int = 30) -> str:
    """Preserve Lab 6 report text while consuming immutable object results."""
    nearest = summary.nearest_expiry
    average = summary.average_price
    lines = [
        "Pharmacy Register Report",
        f"CSV records: {summary.source_count}",
        f"Selection threshold: up to {max_days_left} days remaining, inclusive",
        f"Selected records: {summary.count}",
        "Medicine count by form:",
    ]
    if summary.form_counts:
        lines.extend(f"  {form}: {count}" for form, count in sorted(summary.form_counts))
    else:
        lines.append("  no records")
    lines.append("Unique forms: " + (", ".join(sorted(summary.forms)) or "none"))
    if nearest is None:
        lines.append("Nearest expiry: no selected records")
    else:
        day_word = "day" if nearest.days_left == 1 else "days"
        lines.append(
            "Nearest expiry: "
            f"{nearest.name} ({nearest.form}), "
            f"{nearest.days_left} {day_word} remaining"
        )
    lines.append(
        "Average price of selected medicines: "
        + (f"{average:.2f}" if average is not None else "no data")
    )
    for label, value in sorted(summary.metrics):
        lines.append(f"{label}: {value:.2f}")
    return "\n".join(lines) + "\n"


def write_report(path: Path, text: str) -> None:
    """Create the output directory and write the report as UTF-8 text."""
    try:
        path.parent.mkdir(parents=True, exist_ok=True)
        with path.open("w", encoding="utf-8", newline="\n") as target:
            target.write(text)
    except OSError as error:
        raise DataError(f"Could not write report: {path}") from error
