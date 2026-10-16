import csv
import sys

# Bytes read from the file start for csv.Sniffer to infer the delimiter (64 KiB,
# enough for the header plus many data rows; avoids reading large files whole).
SNIFF_SAMPLE_BYTES = 64 * 1024


def detect_separator(input_path, default=","):
    # Infer the delimiter from the file content itself: csv.Sniffer builds a
    # per-character frequency table over a sample and picks the one giving a
    # consistent column count across rows - no fixed candidate list. Raises
    # csv.Error only when there is no delimiter (single-column file), where the
    # separator is irrelevant and the default is fine.
    try:
        with open(input_path, "r", encoding="utf-8-sig", errors="replace") as f:
            sample = f.read(SNIFF_SAMPLE_BYTES)
    except OSError:
        return default
    if not sample.strip():
        return default
    try:
        return csv.Sniffer().sniff(sample).delimiter
    except csv.Error:
        return default



# Prefixes of comment lines some formats write before the header.
COMMENT_PREFIXES = ("%", "#")


def read_input_csv(input_path, **read_csv_kwargs):
    # Reads the script input as a table with the header on the first line;
    # exits with an error when the file starts with other lines, as pandas
    # would otherwise take one of them for the header.
    # pandas is imported here so that scripts not reading tables do not depend on it
    import pandas as pd

    separator = detect_separator(input_path)
    reason = _find_header_problem(input_path, separator)
    if reason:
        sys.exit(f"Error: {input_path}: {reason}; the header must be on the first line")
    data = pd.read_csv(input_path, sep=separator, **read_csv_kwargs)
    return data, separator


def _find_header_problem(input_path, separator):
    with open(input_path, "r", encoding="utf-8-sig", errors="replace") as f:
        first = f.readline().rstrip("\r\n")
        second = f.readline().rstrip("\r\n")
    if not first.strip() or first.lstrip().startswith(COMMENT_PREFIXES):
        return "comment lines precede the header"
    if separator not in first and separator in second:
        return "a line precedes the header"
    values = [v.strip() for v in next(csv.reader([first], delimiter=separator)) if v.strip()]
    numbers = sum(1 for v in values if _is_number(v))
    if numbers * 2 > len(values):
        return "the file has no header line"
    return None


def _is_number(value):
    try:
        float(value)
        return True
    except ValueError:
        return False
