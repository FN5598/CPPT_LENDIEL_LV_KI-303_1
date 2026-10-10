import argparse
import sys
import tomllib
from importlib import metadata
from pathlib import Path

from .analysis import make_summary
from .errors import DataError
from .loader import load_records
from .reporting import format_report, write_report


def _package_version() -> str:
    """Read installed metadata, or pyproject.toml when running from source."""
    try:
        return metadata.version("kzp-lab07-v11")
    except metadata.PackageNotFoundError:
        with Path(__file__).with_name("pyproject.toml").open("rb") as source:
            return tomllib.load(source)["project"]["version"]


def build_parser() -> argparse.ArgumentParser:
    """Create the parser for input, output, and the variant threshold."""
    parser = argparse.ArgumentParser(
        prog="python -m lab07",
        description="Create a text report from a pharmacy CSV file.",
    )
    parser.add_argument(
        "input_csv", nargs="?", type=Path, help="input CSV path (alternative to --input)"
    )
    parser.add_argument(
        "output_report", nargs="?", type=Path, help="output report path (alternative to --output)"
    )
    parser.add_argument(
        "--input", dest="input_path", type=Path, metavar="CSV", help="input CSV path"
    )
    parser.add_argument(
        "--output", dest="output_path", type=Path, metavar="REPORT", help="output report path"
    )
    parser.add_argument("--version", action="version", version=f"%(prog)s {_package_version()}")
    parser.add_argument(
        "--max-days-left",
        type=int,
        default=30,
        help="maximum days remaining, inclusive (default: 30)",
    )
    return parser


def main(argv: list[str] | None = None) -> int:
    """Run the CSV-to-report flow, returning 0 on success or 2 for data errors."""
    parser = build_parser()
    args = parser.parse_args(argv)
    if args.input_path is not None or args.output_path is not None:
        if args.input_csv is not None or args.output_report is not None:
            parser.error("use either positional paths or --input/--output, not both")
        args.input_csv = args.input_path
        args.output_report = args.output_path
    if args.input_csv is None or args.output_report is None:
        parser.error("provide --input CSV --output REPORT, or two positional paths")
    try:
        records = load_records(args.input_csv)
        summary = make_summary(
            records,
            "price",
            "days_left",
            max_days_left=args.max_days_left,
            price="Total price",
            days_left="Total days remaining",
        )
        write_report(args.output_report, format_report(summary, args.max_days_left))
    except DataError as error:
        print(f"Data error: {error}", file=sys.stderr)
        return 2

    print(f"Report written: {args.output_report}")
    print(f"Records processed: {summary.source_count}")
    print(f"Records selected: {summary.count}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
