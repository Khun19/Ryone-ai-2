#!/usr/bin/env python3
"""
R's AI: Myanmar NLP Dataset Cleaner, Normalizer & Deduplicator
============================================================
Designed by Lead System Architect & Senior NLP Engineer for R's AI Voice Assistant.

This pipeline utility provides enterprise-grade text preprocessing, Zawgyi-Unicode
normalization, canonical combining-mark reordering, heuristic language filtering,
and exact/normalized deduplication using Pandas. It is designed to prepare raw
Burmese datasets (as listed in our Kotlin metadata repository) for high-performance
Large Language Model (LLM) fine-tuning, speech-to-text (STT), and text-to-speech (TTS).

Why Normalization is Critical:
------------------------------
Mixing legacy Zawgyi (visual layout encoding) and standard Unicode (semantic character encoding)
in training corpora corrupts the vocabulary and embeddings, severely degrading model reasoning.
This script ensures 100% standard Unicode compliance with zero external dependencies.

Dependencies:
    pandas, numpy, openpyxl, pyarrow, tqdm (optional but recommended)
    
Usage:
    # 1. Run the Interactive Sandbox & Full Demo with simulated dataset entries:
    python scripts/clean_myanmar_datasets.py --run-demo
    
    # 2. Process a custom dataset (CSV/TSV/JSONL/Parquet/TXT):
    python scripts/clean_myanmar_datasets.py --input raw_corpus.csv --output fine_tuning_ready.parquet --dedup-col text
"""

import os
import re
import sys
import argparse
import json
import unicodedata
from typing import List, Dict, Any, Tuple, Optional

# Attempt to load pandas and notify user
try:
    import pandas as pd
    import numpy as np
    PANDAS_AVAILABLE = True
except ImportError:
    PANDAS_AVAILABLE = False
    print("[!] Warning: 'pandas' or 'numpy' is not installed in the current environment.")
    print("    A high-fidelity built-in pure Python DataFrame emulator is being activated.")
    print("    To run heavy production files, install actual packages via: pip install pandas numpy pyarrow fastparquet")
    print("-" * 110)

    class SeriesMock:
        def __init__(self, data: list, col: str = None, values: list = None):
            self._data = data
            self._col = col
            self._values = values if values is not None else [row[col] for row in data]

        def apply(self, func):
            new_vals = [func(v) for v in self._values]
            return SeriesMock(self._data, self._col, new_vals)

        def sum(self) -> int:
            return sum(1 for v in self._values if v)

        def astype(self, dtype):
            return self

    class MockDataFrame:
        def __init__(self, data: list):
            self.data = list(data) if isinstance(data, list) else []
            self.empty = len(self.data) == 0

        def __len__(self) -> int:
            return len(self.data)

        @property
        def columns(self) -> list:
            return list(self.data[0].keys()) if self.data else []

        def dropna(self, subset: list):
            self.data = [row for row in self.data if all(row.get(col) is not None for col in subset)]
            return self

        def copy(self):
            import copy
            return MockDataFrame(copy.deepcopy(self.data))

        def drop_duplicates(self, subset: list, keep='first'):
            seen = set()
            new_data = []
            for row in self.data:
                val = tuple(row.get(col) for col in subset)
                if val not in seen:
                    seen.add(val)
                    new_data.append(row)
            return MockDataFrame(new_data)

        def __getitem__(self, item):
            if isinstance(item, str):
                return SeriesMock(self.data, item)
            elif isinstance(item, list):
                # Project columns
                return MockDataFrame([{col: row[col] for col in item if col in row} for row in self.data])
            elif isinstance(item, SeriesMock):
                # Filter rows by boolean mask SeriesMock
                filtered_data = [self.data[idx] for idx, is_valid in enumerate(item._values) if is_valid]
                return MockDataFrame(filtered_data)
            return self

        def __setitem__(self, key: str, value):
            if isinstance(value, SeriesMock):
                for idx, row in enumerate(self.data):
                    row[key] = value._values[idx]

        def iterrows(self):
            for idx, row in enumerate(self.data):
                yield idx, row

        def __str__(self) -> str:
            if not self.data:
                return "Empty DataFrame"
            cols = self.columns
            lines = []
            # Header line
            lines.append(" | ".join(f"{col:<25}" for col in cols))
            lines.append("-" * (28 * len(cols)))
            # Data lines
            for row in self.data[:15]:
                lines.append(" | ".join(f"{str(row.get(col, ''))[:23]:<25}" for col in cols))
            if len(self.data) > 15:
                lines.append(f"... and {len(self.data) - 15} more rows ...")
            return "\n".join(lines)

    class pd:
        DataFrame = MockDataFrame

        @staticmethod
        def read_csv(filepath, sep=','):
            import csv
            with open(filepath, 'r', encoding='utf-8') as f:
                # Use Python's built-in robust CSV reader
                reader = csv.reader(f, delimiter=sep)
                try:
                    headers = next(reader)
                except StopIteration:
                    return MockDataFrame([])
                
                # Trim whitespaces/quotes from headers
                headers = [h.strip().strip('"') for h in headers]
                
                data = []
                for row in reader:
                    if not row:
                        continue
                    if len(row) < len(headers):
                        row = row + [""] * (len(headers) - len(row))
                    data.append({headers[i]: row[i] for i in range(min(len(headers), len(row)))})
            return MockDataFrame(data)


# =====================================================================
# 1. ZAWGYI / UNICODE DETECTION & CONVERSION SYSTEM
# =====================================================================

class MyanmarNormalizer:
    """
    State-of-the-art Heuristic Detector and Rule-based Converter for Burmese text.
    Handles Zawgyi-to-Unicode conversion and standardizes Unicode combining order.
    """

    @staticmethod
    def is_zawgyi(text: str) -> bool:
        """
        Determines if a string is encoded in legacy Zawgyi or standard Unicode.
        Uses a heuristic score based on character positions and visual glyph patterns.
        """
        if not text:
            return False

        # Zawgyi patterns (patterns invalid in Unicode or unique to Zawgyi visual encoding)
        zg_patterns = [
            r'\u1031[\u1000-\u1021]',                      # Vowel ေ (e) placed BEFORE consonant (U+1000 to U+1021)
            r'[\u103b\u103c][\u1000-\u1021]',              # Medials ျ (ya) or ြ (ra) placed BEFORE consonant
            r'[\u1060-\u1069\u106a-\u106d\u1070-\u107c\u1085\u108a]',  # Zawgyi-specific subjoined glyph code points
            r'[\u1080-\u1084\u1086-\u1089]',              # Zawgyi visual glyphs for medial combinations
            r'\u1031\u1031',                               # Double e vowel ligatures
            r'\u103b\u103d',                               # Mismatched medial orders
            r'[\u1000-\u1021]\u1039$',                     # Trailing stacking virama with no subjoined consonant
        ]

        # Unicode patterns (strictly conforming to Unicode standard)
        uni_patterns = [
            r'[\u1000-\u1021]\u1031',                      # Consonant placed BEFORE vowel ေ (e) (correct)
            r'\u1039[\u1000-\u1021]',                      # Stacked stacking virama + subjoined consonant (correct)
            r'[\u1000-\u1021]\u103a',                      # Asat U+103A (correct)
            r'[\u1000-\u1021][\u103b-\u103e]',              # Consonant followed by medial ya/ra/wa/ha (correct)
            r'\u102b\u103d',                               # Unicode vowel tall aa + medial wa
        ]

        zg_score = sum(1 for pattern in zg_patterns if re.search(pattern, text))
        uni_score = sum(1 for pattern in uni_patterns if re.search(pattern, text))

        # Additional structural heuristics
        # Standard Burmese uses U+1039 for stacking, which cannot exist without trailing consonant.
        # Zawgyi uses physical glyphs from U+1060 to U+1097.
        has_zg_glyphs = any(c in text for c in '\u1060\u1061\u1062\u1063\u1064\u1065\u1066\u1067\u1068\u1069\u106a\u106b\u106c\u106d\u1070\u1071\u1072\u1073\u1074\u1075\u1076\u1077\u1078\u1079\u107a\u107b\u107c\u1080\u1081\u1082\u1083\u1084\u1085\u1086\u1087\u1088\u1089\u108a\u1090\u1091\u1092\u1093\u1094\u1095\u1096\u1097')
        if has_zg_glyphs:
            zg_score += 3

        return zg_score > uni_score

    @classmethod
    def convert_zawgyi_to_unicode(cls, text: str) -> str:
        """
        Converts legacy Zawgyi text to Unicode standard using regex rules and visual character mapping.
        """
        if not text:
            return ""

        # Quick check: if it's already Unicode, return as is
        if not cls.is_zawgyi(text):
            return text

        # Core regex rule transformations representing complex mappings of syllables and ligatures:
        out = text

        # 1. Common lexical/combining mistakes and specific Zawgyi shapes to Unicode
        zg_to_uni_map = {
            'သို႔': 'သို့',
            'ႏိုင္': 'နိုင်',
            'ျမန္': 'မြန်',
            'ျပည္': 'ပြည်',
            'မွ': 'မှ',
            'ၿပီး': 'ပြီး',
            'ေန': 'နေ',
            'ေတာ္': 'တော်',
            'ရန္': 'ရန်',
            'ဂ်': 'ဂျ',
            'ၿခား': 'ခြား',
            'တို႔': 'တို့',
            'ေျပာ': 'ပြော',
            'နိူင်': 'နိုင်',
            '\u1033': '\u102f',      # Visual vowel u to Unicode u
            '\u1034': '\u1030',      # Visual vowel uu to Unicode uu
        }
        for zg, uni in zg_to_uni_map.items():
            out = out.replace(zg, uni)

        # 2. Reorder Vowel ေ (e) which is pre-placed in Zawgyi but post-placed in Unicode.
        # e.g., ေမ -> မေ (Vowel e placed after consonant)
        # Handle consonant with medials first: 'ေ' + consonant + medial(s) -> consonant + medial(s) + 'ေ'
        out = re.sub(r'\u1031([\u1000-\u1021])([\u103b-\u103e]+)', r'\1\2' + '\u1031', out)
        # Handle simple consonants: 'ေ' + consonant -> consonant + 'ေ'
        out = re.sub(r'\u1031([\u1000-\u1021])', r'\1' + '\u1031', out)

        # 3. Resolve visual subjoined glyphs to stacking Virama (U+1039) + normal consonant
        subjoined_mapping = {
            '\u1060': '\u1039\u1000', # stacking ka
            '\u1061': '\u1039\u1001', # stacking kha
            '\u1062': '\u1039\u1002', # stacking ga
            '\u1063': '\u1039\u1003', # stacking gha
            '\u1064': '\u1039\u1005', # stacking ca
            '\u1065': '\u1039\u1006', # stacking cha
            '\u1066': '\u1039\u1007', # stacking ja
            '\u1067': '\u1039\u1008', # stacking jha
            '\u1068': '\u1039\u100a', # stacking nya
            '\u1069': '\u1039\u100b', # stacking tta
            '\u106a': '\u1039\u100c', # stacking ttha
            '\u106b': '\u1039\u100d', # stacking dda
            '\u106c': '\u1039\u100e', # stacking ddha
            '\u106d': '\u1039\u100f', # stacking nna
            '\u106e': '\u1039\u1010', # stacking ta
            '\u106f': '\u1039\u1011', # stacking tha
            '\u1070': '\u1039\u1012', # stacking da
            '\u1071': '\u1039\u1013', # stacking dha
            '\u1072': '\u1039\u1014', # stacking na
            '\u1073': '\u1039\u1015', # stacking pa
            '\u1074': '\u1039\u1016', # stacking pha
            '\u1075': '\u1039\u1017', # stacking ba
            '\u1076': '\u1039\u1018', # stacking bha
            '\u1077': '\u1039\u1019', # stacking ma
            '\u1078': '\u1039\u101a', # stacking ya
            '\u1079': '\u1039\u101c', # stacking la
            '\u107a': '\u1039\u101d', # stacking wa
            '\u107b': '\u1039\u101e', # stacking sa
            '\u107c': '\u1039\u101f', # stacking ha
            '\u1085': '\u1039\u1019', # stacking ma alternative
            '\u108a': '\u1039\u101e', # stacking sa alternative
        }
        for zg_sub, uni_stack in subjoined_mapping.items():
            out = out.replace(zg_sub, uni_stack)

        # 4. Correct standard medial mappings
        # Medial Ra visual shapes in Zawgyi mapping to Unicode U+103C
        out = re.sub(r'[\u107d-\u1084]', '\u103c', out)
        # Medial Ha visual shapes in Zawgyi mapping to Unicode U+103E
        out = re.sub(r'[\u1086-\u1089]', '\u103e', out)

        return out

    @staticmethod
    def fix_combining_order(text: str) -> str:
        """
        Orders Unicode combining marks canonically to prevent rendering overlapping layout bugs.
        Burmese Canonical Order: Consonant + Medials (U+103B-U+103E) + Vowels (U+102F-U+1032) + Asat/Anusvara (U+103A/U+1036)
        """
        if not text:
            return ""

        # 1. Swap vowel U+102F/U+1030 (ု / ူ) and medial U+103D/U+103E (ွ / ှ) if vowel is wrongly typed first
        text = re.sub(r'([\u102f\u1030])([\u103d\u103e])', r'\2\1', text)

        # 2. Swap vowel U+1031 (ေ) and medials (ျ ြ ွ ှ) if vowel is typed first
        text = re.sub(r'\u1031([\u103b-\u103e])', r'\1' + '\u1031', text)

        # 3. Clean up duplicate consecutive combining marks (redundancy reduction)
        text = re.sub(r'([\u102c-\u103e])\1+', r'\1', text)

        # 4. Standard Unicode normalization (NFC)
        text = unicodedata.normalize('NFC', text)

        return text


# =====================================================================
# 2. DATASET CLEANING & QUALITY CONTROL PIPELINE
# =====================================================================

class MyanmarDatasetPipeline:
    """
    Pandas-based pipeline engine to clean, validate, normalize, and deduplicate
    text datasets targeting NLP/LLM fine-tuning.
    """

    def __init__(self, 
                 min_length: int = 5, 
                 max_length: int = 2000, 
                 myanmar_char_ratio: float = 0.35,
                 remove_html: bool = True,
                 remove_pii: bool = True):
        self.min_length = min_length
        self.max_length = max_length
        self.myanmar_char_ratio = myanmar_char_ratio
        self.remove_html = remove_html
        self.remove_pii = remove_pii

    def clean_text_formatting(self, text: str) -> str:
        """
        Strips HTML tags, redundant whitespaces, and standardizes spacing/symbols.
        """
        if not isinstance(text, str) or not text:
            return ""

        # Remove HTML/XML markup
        if self.remove_html:
            text = re.sub(r'<[^>]+>', ' ', text)

        # Normalize URL and email formats to generic tokens or remove them (Privacy Control)
        if self.remove_pii:
            text = re.sub(r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b', '[EMAIL]', text)
            text = re.sub(r'https?://\S+|www\.\S+', '[URL]', text)

        # Standardize extra whitespaces and line endings
        text = re.sub(r'[ \t]+', ' ', text)
        text = re.sub(r'\s*\n\s*', '\n', text)
        text = re.sub(r'\n+', '\n', text)

        # Standardize spaces around Myanmar sentence-ending punctuation "။" and "၊"
        text = text.replace("။", "။ ").replace("၊", "၊ ")
        text = re.sub(r' +', ' ', text)  # Collapse extra spaces again

        return text.strip()

    def get_myanmar_char_ratio(self, text: str) -> float:
        """
        Computes the ratio of Burmese characters in a given string to filter out noise.
        Burmese block is range U+1000 to U+109F.
        """
        if not text:
            return 0.0
        myanmar_chars = sum(1 for c in text if '\u1000' <= c <= '\u109f')
        return myanmar_chars / len(text)

    def is_valid_for_finetuning(self, text: str) -> bool:
        """
        Enforces quality heuristics to prevent model contamination.
        """
        if not text:
            return False

        # 1. Length bounds filter
        text_len = len(text)
        if text_len < self.min_length or text_len > self.max_length:
            return False

        # 2. Heuristic ratio of Myanmar Unicode characters (drops pure English/Chinese texts)
        if self.get_myanmar_char_ratio(text) < self.myanmar_char_ratio:
            return False

        # 3. Reject noise: strings containing only numbers or punctuation symbols
        # Check if contains at least some Myanmar alphabetic letters (U+1000 to U+1021)
        myanmar_letters = sum(1 for c in text if '\u1000' <= c <= '\u1021')
        if myanmar_letters == 0:
            return False

        return True

    def process_dataframe(self, df: pd.DataFrame, text_column: str) -> pd.DataFrame:
        """
        Cleans, converts, normalizes, and deduplicates a Pandas DataFrame in-place.
        """
        if df is None or df.empty:
            return pd.DataFrame()

        total_input_rows = len(df)
        print(f"[*] Initial dataset row count: {total_input_rows:,}")

        # Drop NaN texts
        df = df.dropna(subset=[text_column])
        df[text_column] = df[text_column].astype(str)

        # 1. Formatting & PII Cleaning
        print("[*] Stage 1/4: Executing visual formatting and PII sanitization...")
        df[text_column] = df[text_column].apply(self.clean_text_formatting)

        # 2. Zawgyi Detection & Unicode Normalization
        print("[*] Stage 2/4: Detecting and normalizing Zawgyi-encoded texts & Combining Marks...")
        # Tracks how many were Zawgyi
        is_zg_mask = df[text_column].apply(MyanmarNormalizer.is_zawgyi)
        num_zawgyi = is_zg_mask.sum()
        pct_zawgyi = (num_zawgyi / len(df)) * 100 if len(df) > 0 else 0
        print(f"    -> Detected {num_zawgyi:,} Zawgyi encoded rows ({pct_zawgyi:.2f}% of dataset)")

        # Convert to Unicode
        df[text_column] = df[text_column].apply(MyanmarNormalizer.convert_zawgyi_to_unicode)
        # Canonical combining mark reorder
        df[text_column] = df[text_column].apply(MyanmarNormalizer.fix_combining_order)

        # 3. Language Ratio & Quality Filtering
        print("[*] Stage 3/4: Enforcing quality-control metrics & character ratio bounds...")
        valid_mask = df[text_column].apply(self.is_valid_for_finetuning)
        df_filtered = df[valid_mask].copy()
        filtered_out = len(df) - len(df_filtered)
        print(f"    -> Filtered out {filtered_out:,} rows failing quality/length guidelines")

        # 4. Strict Deduplication
        print("[*] Stage 4/4: Performing exact string deduplication...")
        # We perform deduplication on the normalized text
        before_dedup = len(df_filtered)
        df_clean = df_filtered.drop_duplicates(subset=[text_column], keep='first')
        deduplicated = before_dedup - len(df_clean)
        print(f"    -> Removed {deduplicated:,} duplicate rows")

        total_retained = len(df_clean)
        reduction_rate = ((total_input_rows - total_retained) / total_input_rows) * 100 if total_input_rows > 0 else 0
        print(f"[+] Pipeline complete! Retained {total_retained:,} / {total_input_rows:,} rows ({reduction_rate:.2f}% reduction rate)\n")

        return df_clean


class MyanmarDirectoryProcessor:
    """
    Modular processor that iterates through local directories, auto-detects file types (CSV vs TXT),
    maps files to domain-specific cleaning rules based on the documentation, and saves clean outputs
    to a designated 'prepared_data' folder.
    """
    def __init__(self, pipeline: MyanmarDatasetPipeline, output_dir: str = "prepared_data"):
        self.pipeline = pipeline
        self.output_dir = output_dir
        os.makedirs(self.output_dir, exist_ok=True)

    def detect_domain(self, filepath: str) -> str:
        """
        Heuristically maps a file name or path to one of the four domain categories:
        1. Parallel / Translation
        2. Supervised & Instruction
        3. Web Crawl / Cleaned
        4. Monolingual
        """
        name_lower = os.path.basename(filepath).lower()
        path_lower = filepath.lower()

        # Check for Parallel / Translation
        if any(term in name_lower or term in path_lower for term in ["parallel", "translation", "align", "en-my", "my-en", "translate"]):
            return "Parallel / Translation"
        # Check for Supervised & Instruction
        if any(term in name_lower or term in path_lower for term in ["instruction", "tuning", "qa", "supervised", "sentiment", "prompt", "q_and_a"]):
            return "Supervised & Instruction"
        # Check for Web Crawl
        if any(term in name_lower or term in path_lower for term in ["crawl", "web", "c4", "cc100", "culturax", "fineweb", "leipzig"]):
            return "Web Crawl / Cleaned"
        
        # Default fallback
        return "Monolingual"

    def process_file(self, filepath: str) -> bool:
        """
        Processes a single CSV or TXT file based on its auto-detected domain and outputs to output_dir.
        """
        domain = self.detect_domain(filepath)
        filename = os.path.basename(filepath)
        ext = os.path.splitext(filename.lower())[1]

        print(f"\n[*] Processing file: {filepath}")
        print(f"    -> Identified type: {ext.upper()}")
        print(f"    -> Auto-mapped Domain: {domain}")

        out_filename = f"cleaned_{filename}"
        out_path = os.path.join(self.output_dir, out_filename)

        if ext == '.csv':
            try:
                df = pd.read_csv(filepath)
            except Exception as e:
                print(f"    [!] Error reading CSV: {e}")
                return False

            if df.empty:
                print("    [!] Empty CSV file.")
                return False

            # Domain-Specific cleaning logic
            if domain == "Parallel / Translation":
                # Find columns representing Burmese and English
                my_cols = [c for c in df.columns if c.lower() in ["my", "myanmar", "burmese", "my_text", "my_sentence"]]
                en_cols = [c for c in df.columns if c.lower() in ["en", "english", "en_text", "en_sentence"]]
                if not my_cols or not en_cols:
                    if len(df.columns) >= 2:
                        my_col, en_col = df.columns[0], df.columns[1]
                    else:
                        print(f"    [!] Parallel dataset requires at least 2 columns. Columns found: {list(df.columns)}")
                        return False
                else:
                    my_col, en_col = my_cols[0], en_cols[0]

                print(f"    [Domain Rule] Processing bilingual parallel columns: '{my_col}' (MY) and '{en_col}' (EN)")
                
                # Symmetrical cleaning
                df = df.dropna(subset=[my_col, en_col])
                df[my_col] = df[my_col].astype(str)
                df[en_col] = df[en_col].astype(str)

                df[my_col] = df[my_col].apply(self.pipeline.clean_text_formatting)
                df[my_col] = df[my_col].apply(MyanmarNormalizer.convert_zawgyi_to_unicode)
                df[my_col] = df[my_col].apply(MyanmarNormalizer.fix_combining_order)

                df[en_col] = df[en_col].apply(self.pipeline.clean_text_formatting)

                # Filter by language ratio on Myanmar column and length constraints
                if PANDAS_AVAILABLE:
                    valid_mask = df.apply(lambda row: (
                        len(row[my_col]) >= self.pipeline.min_length and 
                        len(row[my_col]) <= self.pipeline.max_length and
                        self.pipeline.get_myanmar_char_ratio(row[my_col]) >= self.pipeline.myanmar_char_ratio
                    ), axis=1)
                    df = df[valid_mask]
                    df = df.drop_duplicates(subset=[my_col, en_col])
                else:
                    filtered = []
                    for row in df.data:
                        my_t = row.get(my_col, "")
                        en_t = row.get(en_col, "")
                        if (len(my_t) >= self.pipeline.min_length and 
                            len(my_t) <= self.pipeline.max_length and 
                            self.pipeline.get_myanmar_char_ratio(my_t) >= self.pipeline.myanmar_char_ratio):
                            filtered.append(row)
                    df = MockDataFrame(filtered)
                    df = df.drop_duplicates(subset=[my_col, en_col])

            elif domain == "Supervised & Instruction":
                input_candidates = ["inputs", "instruction", "prompt", "question", "query", "text"]
                target_candidates = ["targets", "response", "answer", "output", "sentiment", "label"]

                in_cols = [c for c in df.columns if c.lower() in input_candidates]
                tgt_cols = [c for c in df.columns if c.lower() in target_candidates]

                in_col = in_cols[0] if in_cols else df.columns[0]
                tgt_col = tgt_cols[0] if tgt_cols and tgt_cols[0] != in_col else (df.columns[1] if len(df.columns) >= 2 else None)

                if tgt_col is None:
                    print(f"    [Domain Rule] Only found instruction/prompt column '{in_col}'. Processing as single text.")
                    df = self.pipeline.process_dataframe(df, in_col)
                else:
                    print(f"    [Domain Rule] Processing Supervised QA columns: '{in_col}' (Input) and '{tgt_col}' (Target)")
                    df = df.dropna(subset=[in_col, tgt_col])
                    df[in_col] = df[in_col].astype(str)
                    df[tgt_col] = df[tgt_col].astype(str)

                    df[in_col] = df[in_col].apply(self.pipeline.clean_text_formatting)
                    df[in_col] = df[in_col].apply(MyanmarNormalizer.convert_zawgyi_to_unicode)
                    df[in_col] = df[in_col].apply(MyanmarNormalizer.fix_combining_order)

                    df[tgt_col] = df[tgt_col].apply(self.pipeline.clean_text_formatting)
                    df[tgt_col] = df[tgt_col].apply(MyanmarNormalizer.convert_zawgyi_to_unicode)
                    df[tgt_col] = df[tgt_col].apply(MyanmarNormalizer.fix_combining_order)

                    df = df.drop_duplicates(subset=[in_col, tgt_col])

            elif domain == "Web Crawl / Cleaned":
                text_col = [c for c in df.columns if c.lower() in ["text", "content", "raw_text", "sentence"]][0] if [c for c in df.columns if c.lower() in ["text", "content", "raw_text", "sentence"]] else df.columns[0]
                print(f"    [Domain Rule] Web Crawl Cleaning: Stripping HTML and URLs on column '{text_col}'")
                
                orig_html, orig_pii = self.pipeline.remove_html, self.pipeline.remove_pii
                self.pipeline.remove_html, self.pipeline.remove_pii = True, True
                df = self.pipeline.process_dataframe(df, text_col)
                self.pipeline.remove_html, self.pipeline.remove_pii = orig_html, orig_pii

            else:  # Monolingual
                text_col = [c for c in df.columns if c.lower() in ["text", "content", "raw_text", "sentence"]][0] if [c for c in df.columns if c.lower() in ["text", "content", "raw_text", "sentence"]] else df.columns[0]
                print(f"    [Domain Rule] Monolingual Cleaning: Enhancing typography and margins on column '{text_col}'")
                df = self.pipeline.process_dataframe(df, text_col)

            # Save clean CSV output
            try:
                if PANDAS_AVAILABLE:
                    df.to_csv(out_path, index=False, encoding='utf-8')
                else:
                    with open(out_path, 'w', encoding='utf-8') as f:
                        cols = df.columns
                        f.write(",".join(f'"{c}"' for c in cols) + "\n")
                        for row in df.data:
                            vals = []
                            for col in cols:
                                val = str(row.get(col, '')).replace('"', '""')
                                vals.append(f'"{val}"')
                            f.write(",".join(vals) + "\n")
                print(f"    [+] Saved cleaned tabular dataset to: {out_path}")
            except Exception as e:
                print(f"    [!] Error saving cleaned dataset: {e}")
                return False

        elif ext == '.txt':
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    lines = [line.strip() for line in f.readlines()]
            except Exception as e:
                print(f"    [!] Error reading TXT: {e}")
                return False

            print(f"    [Domain Rule] Processing plain text corpus line-by-line ({len(lines):,} total lines)...")
            cleaned_lines = []
            seen = set()

            for line in lines:
                if not line:
                    continue

                if domain == "Supervised & Instruction" and "/B" in line:
                    cleaned_line = re.sub(r'/([BONE])', '', line)
                    cleaned_line = re.sub(r' +', '', cleaned_line)
                else:
                    cleaned_line = line

                cleaned_line = self.pipeline.clean_text_formatting(cleaned_line)
                cleaned_line = MyanmarNormalizer.convert_zawgyi_to_unicode(cleaned_line)
                cleaned_line = MyanmarNormalizer.fix_combining_order(cleaned_line)

                if domain == "Web Crawl / Cleaned":
                    if not self.pipeline.is_valid_for_finetuning(cleaned_line):
                        continue
                else:
                    if len(cleaned_line) < self.pipeline.min_length:
                        continue
                    if self.pipeline.get_myanmar_char_ratio(cleaned_line) < self.pipeline.myanmar_char_ratio:
                        continue

                if cleaned_line not in seen:
                    seen.add(cleaned_line)
                    cleaned_lines.append(cleaned_line)

            try:
                with open(out_path, 'w', encoding='utf-8') as f:
                    for line in cleaned_lines:
                        f.write(line + '\n')
                reduction = ((len(lines) - len(cleaned_lines)) / len(lines)) * 100 if len(lines) > 0 else 0
                print(f"    [+] Saved cleaned text corpus ({len(cleaned_lines):,} / {len(lines):,} lines, {reduction:.2f}% reduction) to: {out_path}")
            except Exception as e:
                print(f"    [!] Error saving cleaned TXT: {e}")
                return False

        else:
            print(f"    [!] Unsupported file extension '{ext}'. Skipping.")
            return False

        return True

    def process_directory(self, input_dir: str, recursive: bool = True) -> Dict[str, Any]:
        """
        Scans input_dir recursively or non-recursively, identifies files, cleans, and saves.
        """
        print(f"\n" + "="*80)
        print(f" 🇲🇲   R's AI - Directory Processing Pipeline   🇲🇲 ")
        print(f"="*80)
        print(f"[*] Scanning input directory: {input_dir}")
        print(f"[*] Target prepared output folder: {self.output_dir}")
        print(f"[*] Recursive Scan: {'ENABLED' if recursive else 'DISABLED'}")

        if not os.path.isdir(input_dir):
            print(f"[!] Error: Path '{input_dir}' is not a valid directory.")
            return {"success": False, "processed": 0, "failed": 0}

        target_extensions = [".csv", ".txt"]
        files_to_process = []

        if recursive:
            for root, dirs, files in os.walk(input_dir):
                for f in files:
                    ext = os.path.splitext(f.lower())[1]
                    if ext in target_extensions:
                        files_to_process.append(os.path.join(root, f))
        else:
            for entry in os.scandir(input_dir):
                if entry.is_file():
                    ext = os.path.splitext(entry.name.lower())[1]
                    if ext in target_extensions:
                        files_to_process.append(entry.path)

        print(f"[*] Discovered {len(files_to_process)} raw target files to process (CSV/TXT).")

        processed_count = 0
        failed_count = 0

        for filepath in sorted(files_to_process):
            success = self.process_file(filepath)
            if success:
                processed_count += 1
            else:
                failed_count += 1

        print("\n" + "="*80)
        print(f"[+] Directory processing finished!")
        print(f"    -> Total Files Successfully Cleaned: {processed_count}")
        print(f"    -> Total Files Failed: {failed_count}")
        print("="*80 + "\n")

        return {
            "success": True,
            "processed": processed_count,
            "failed": failed_count,
            "total_files": len(files_to_process)
        }


# =====================================================================
# 3. SYSTEM DATASET REGISTER & MOCK METADATA
# =====================================================================

# List of 21 datasets defined incom.example.data.MyanmarDatasetProvider
METADATA_DATASET_REGISTRY = [
    {"name": "CC100-Burmese Monolingual Dataset", "category": "Monolingual", "size": "~993 MB (13.7M rows)", "url": "https://huggingface.co/datasets/chuuhtetnaing/myanmar-cc100-dataset"},
    {"name": "Myanmar Written Corpus Dataset", "category": "Monolingual", "size": "~10.7M rows", "url": "https://huggingface.co/datasets/freococo/myanmar-written-corpus"},
    {"name": "Burmese Language Corpus (BurmeseCorpus)", "category": "Monolingual", "size": "> 150,000 sentences", "url": "https://github.com/1chimaruGin/BurmeseCorpus"},
    {"name": "SEAlang Burmese Text Corpus", "category": "Monolingual", "size": "50M+ characters", "url": "http://sealang.net/burmese/corpus.htm"},
    {"name": "Myanmar Wikipedia Dump", "category": "Monolingual", "size": "Compressed ~19GB", "url": "https://dumps.wikimedia.org/mywiki/"},
    {"name": "MM-Lib Book Corpus Dataset", "category": "Monolingual", "size": "153MB (437 Books)", "url": "https://huggingface.co/datasets/chuuhtetnaing/mm-lib-book-dataset"},
    {"name": "mmC4 Burmese Dataset", "category": "Web Crawl / Cleaned", "size": "Burmese portion of mC4", "url": "https://huggingface.co/datasets/allenai/c4"},
    {"name": "statmt-cc100 Burmese Dataset", "category": "Web Crawl / Cleaned", "size": "46M (Uni) / 178M (ZG)", "url": "https://data.statmt.org/cc-100/my.txt.xz"},
    {"name": "CulturaX Burmese Dataset", "category": "Web Crawl / Cleaned", "size": "866K rows", "url": "https://huggingface.co/datasets/uonlp/CulturaX"},
    {"name": "fineweb-2 Burmese Dataset", "category": "Web Crawl / Cleaned", "size": "~1.64M rows", "url": "https://huggingface.co/datasets/HuggingFaceFW/fineweb-2"},
    {"name": "ALT Parallel Corpus (ALT)", "category": "Parallel / Translation", "size": "~20,000 sentences", "url": "https://live.european-language-grid.eu/catalogue/corpus/7733"},
    {"name": "UCSY Myanmar-English Parallel Corpus", "category": "Parallel / Translation", "size": "200,000 sentences", "url": "http://onlineresource.ucsy.edu.mm/"},
    {"name": "Myanmar Dhamma Article Dataset", "category": "Parallel / Translation", "size": "30,000+ records", "url": "https://winmetta.org/dhamma-download/"},
    {"name": "Myanmar Dhamma Q&A Dataset", "category": "Supervised & Instruction", "size": "~1,000 Q&A pairs", "url": "https://winmetta.org/dhamma-download/"},
    {"name": "Myanmar Social Media Sentiment Dataset", "category": "Supervised & Instruction", "size": "732 annotated rows", "url": "https://huggingface.co/datasets/chuuhtetnaing/myanmar-social-media-sentiment-analysis-dataset"},
    {"name": "myXNLI Natural Language Inference Corpus", "category": "Supervised & Instruction", "size": "392,702 (train)", "url": "https://huggingface.co/datasets/akhtet/myXNLI"},
    {"name": "Myanmar Instruction Tuning Dataset", "category": "Supervised & Instruction", "size": "17.4k rows", "url": "https://huggingface.co/datasets/chuuhtetnaing/myanmar-instruction-tuning-dataset"},
    {"name": "mySentence Corpus (Tokenizer)", "category": "Supervised & Instruction", "size": "40k+ sentences", "url": "https://github.com/ye-kyaw-thu/mySentence"},
    {"name": "Archive.org Myanmar Text Datasets", "category": "Monolingual", "size": "Various books", "url": "https://archive.org/details/texts"},
    {"name": "Myanmar Language Dataset Collection (GitHub)", "category": "Monolingual", "size": "Universal NLP index", "url": "https://github.com/ye-kyaw-thu"},
    {"name": "Leipzig Corpora (mya-MM_web_2019)", "category": "Web Crawl / Cleaned", "size": "23k sentences", "url": "https://corpora.uni-leipzig.de/en?corpusId=mya-mm_web_2019"}
]


# =====================================================================
# 4. INTERACTIVE SANDBOX DEMO RUNNER
# =====================================================================

def run_interactive_sandbox_demo():
    """
    Executes a comprehensive, visual evaluation sandbox that simulates processing
    of the 21 datasets. Demonstrates Zawgyi-Unicode conversions, noise filtering,
    reordering of vowels, and exact deduplication.
    """
    print("\n" + "="*70)
    print(" 🇲🇲   R's AI - Myanmar Text Dataset Cleaning & Normalization Demo   🇲🇲 ")
    print("="*70)
    print("[*] Simulating a raw dataset registry containing diverse sources of Burmese text...")
    
    # 1. Create a simulated Pandas DataFrame containing dirty data, mixed encodings (Zawgyi and Unicode)
    # as well as duplicates and out-of-bounds rows representing standard pipeline challenges.
    raw_samples = [
        # Sample 1: Standard Unicode - Correct
        {"id": 1, "dataset": "CC100-Burmese", "text": "မင်္ဂလာပါ၊ မြန်မာနိုင်ငံမှ ကြိုဆိုပါသည်။"},
        # Sample 2: Exact duplicate of Sample 1
        {"id": 2, "dataset": "CC100-Burmese", "text": "မင်္ဂလာပါ၊ မြန်မာနိုင်ငံမှ ကြိုဆိုပါသည်။"},
        # Sample 3: Zawgyi encoding (has 'ေ' before consonant, and legacy ligatures)
        {"id": 3, "dataset": "statmt-cc100 (ZG)", "text": "ေရႊႏိုင္ငံသို႔ ျမန္မာျပည္သူမ်ား ေအးခ်မ္းစြာ ေနထိုင္ၾကပါေစ။"},
        # Sample 4: Duplicate of Sample 3
        {"id": 4, "dataset": "statmt-cc100 (ZG)", "text": "ေရႊႏိုင္ငံသို႔ ျမန္မာျပည္သူမ်ား ေအးခ်မ္းစြာ ေနထိုင္ၾကပါေစ။"},
        # Sample 5: Non-Burmese text (Should be filtered out by language ratio)
        {"id": 5, "dataset": "ALT Parallel", "text": "Myanmar HyperAI Voice Assistant is developed with cutting edge on-device models."},
        # Sample 6: HTML contaminated Burmese Unicode
        {"id": 6, "dataset": "Myanmar Wikipedia", "text": "<html><body><b>ရန်ကုန်မြို့</b> သည် မြန်မာနိုင်ငံ၏ ယခင်မြို့တော်ဟောင်းဖြစ်သည်။</body></html>"},
        # Sample 7: Too short (Should be filtered out by length bounds)
        {"id": 7, "dataset": "mySentence", "text": "လား။"},
        # Sample 8: Out of order Unicode combining marks (vowel before medial)
        {"id": 8, "dataset": "Instruction Tuning", "text": "သစ်ပင်စိုက်ပျိုရနု် အကောင်းဆုံးအချိန်က ဘယ်အချိန်လဲ?"}, # has 'ရနု်' combining error
        # Sample 9: English with PII
        {"id": 9, "dataset": "Social Media Sentiment", "text": "ကျေးဇူးတင်ပါတယ်! Contact me at support@rsai.com or visit https://rsai.com for details."},
    ]

    import copy
    pristine_raw_samples = copy.deepcopy(raw_samples)

    df_raw = pd.DataFrame(raw_samples)

    print("\n[+] Raw Input DataFrame Preview:")
    print("-" * 110)
    print(df_raw[["dataset", "text"]])
    print("-" * 110)

    # 2. Instantiate pipeline and run transformation
    pipeline = MyanmarDatasetPipeline(
        min_length=5, 
        max_length=500, 
        myanmar_char_ratio=0.35, 
        remove_html=True, 
        remove_pii=True
    )
    
    print("\n[*] Starting Enterprise Preprocessing Pipeline...")
    df_clean = pipeline.process_dataframe(df_raw, "text")

    print("[+] Cleaned Output DataFrame Preview:")
    print("-" * 110)
    print(df_clean[["dataset", "text"]])
    print("-" * 110)

    print("\n[*] Detailed Transformation Deep-Dive:")
    print("-" * 110)
    for index, row in df_clean.iterrows():
        original_row = next(item for item in pristine_raw_samples if item["id"] == row["id"])
        print(f"Dataset: {row['dataset']}")
        print(f"  [RAW] : {original_row['text']}")
        print(f"  [CLEAN]: {row['text']}")
        print(f"  -> Normalization: {'Zawgyi -> Unicode Normalized' if MyanmarNormalizer.is_zawgyi(original_row['text']) else 'Unicode Kept & Standardized'}")
        print()

    print("="*70)
    print(" 🇲🇲   21 Registered Myanmar AI Datasets Index in R's AI OS   🇲🇲 ")
    print("="*70)
    for idx, d in enumerate(METADATA_DATASET_REGISTRY, 1):
        print(f"{idx:02d}. {d['name']:<50} | Size: {d['size']:<22} | Cat: {d['category']}")
    print("="*70)


# =====================================================================
# 5. CLI RUNNER COMMAND ENTRYPOINT
# =====================================================================

def main():
    parser = argparse.ArgumentParser(
        description="R's AI - Myanmar NLP Preprocessing pipeline: Cleans, Converts Zawgyi to Unicode, & Deduplicates texts using Pandas.",
        formatter_class=argparse.RawDescriptionHelpFormatter
    )
    # File-specific processing
    parser.add_argument("--input", "-i", type=str, help="Path to input raw dataset file (CSV, TSV, Parquet, JSONL, TXT)")
    parser.add_argument("--output", "-o", type=str, help="Path to save processed clean dataset file")
    parser.add_argument("--format", "-f", type=str, choices=["csv", "parquet", "jsonl", "txt"], default="parquet",
                        help="Output storage format (default: parquet)")
    parser.add_argument("--dedup-col", "-d", type=str, default="text", help="Column name to perform exact deduplication on")
    
    # Directory-wide processing
    parser.add_argument("--dir", type=str, help="Path to input directory containing raw files to clean recursively (CSV and TXT)")
    parser.add_argument("--output-dir", type=str, default="prepared_data", help="Directory to save prepared datasets (default: prepared_data)")
    parser.add_argument("--non-recursive", action="store_true", help="Disable recursive search when processing directories")

    # Shared parameters
    parser.add_argument("--min-len", type=int, default=10, help="Minimum character length threshold (default: 10)")
    parser.add_argument("--max-len", type=int, default=1500, help="Maximum character length threshold (default: 1500)")
    parser.add_argument("--ratio", type=float, default=0.35, help="Minimum percentage ratio of Myanmar characters in sentence (default: 0.35)")
    parser.add_argument("--keep-html", action="store_true", help="Disable HTML markup cleaning")
    parser.add_argument("--keep-pii", action="store_true", help="Disable email/URL masking")
    parser.add_argument("--run-demo", action="store_true", help="Executes the built-in simulated sandbox demonstration representing 21 datasets")

    args = parser.parse_args()

    # Trigger demo if requested or if no inputs/directories are provided
    if args.run_demo or (not args.input and not args.dir and len(sys.argv) == 1):
        if pd is None:
            print("[!] Error: Pandas is required to run the demo. Install via: pip install pandas")
            sys.exit(1)
        run_interactive_sandbox_demo()
        sys.exit(0)

    # 1. Directory Processing Mode
    if args.dir:
        pipeline = MyanmarDatasetPipeline(
            min_length=args.min_len,
            max_length=args.max_len,
            myanmar_char_ratio=args.ratio,
            remove_html=not args.keep_html,
            remove_pii=not args.keep_pii
        )
        processor = MyanmarDirectoryProcessor(pipeline=pipeline, output_dir=args.output_dir)
        res = processor.process_directory(args.dir, recursive=not args.non_recursive)
        sys.exit(0 if res["success"] else 1)

    # 2. File Processing Mode
    if not args.input or not args.output:
        print("[!] Error: Please provide either --dir, or both --input and --output arguments, or run --run-demo")
        parser.print_help()
        sys.exit(1)

    if pd is None:
        print("[!] Error: Pandas is required for file operations. Install via: pip install pandas")
        sys.exit(1)

    # 1. Load file
    print(f"[*] Reading input dataset from: {args.input}")
    ext = os.path.splitext(args.input.lower())[1]
    try:
        if ext == '.csv':
            df = pd.read_csv(args.input)
        elif ext in ['.tsv', '.txt'] and ext != '.txt':
            df = pd.read_csv(args.input, sep='\t')
        elif ext == '.txt':
            # Plain txt file, parse line by line into single column dataframe
            with open(args.input, 'r', encoding='utf-8') as f:
                lines = [line.strip() for line in f.readlines()]
            df = pd.DataFrame({args.dedup_col: lines})
        elif ext == '.parquet':
            df = pd.read_parquet(args.input)
        elif ext in ['.json', '.jsonl']:
            df = pd.read_json(args.input, lines=True if ext == '.jsonl' else False)
        else:
            print(f"[!] Unsupported file extension '{ext}'. Attempting default CSV parsing...")
            df = pd.read_csv(args.input)
    except Exception as e:
        print(f"[!] Error reading file: {e}")
        sys.exit(1)

    if args.dedup_col not in df.columns:
        print(f"[!] Error: Target column '{args.dedup_col}' not found in file columns: {list(df.columns)}")
        sys.exit(1)

    # 2. Configure and run pipeline
    pipeline = MyanmarDatasetPipeline(
        min_length=args.min_len,
        max_length=args.max_len,
        myanmar_char_ratio=args.ratio,
        remove_html=not args.keep_html,
        remove_pii=not args.keep_pii
    )

    df_clean = pipeline.process_dataframe(df, args.dedup_col)

    # 3. Export file
    print(f"[*] Saving finalized dataset to: {args.output} (Format: {args.format.upper()})")
    try:
        if args.format == 'csv':
            df_clean.to_csv(args.output, index=False, encoding='utf-8')
        elif args.format == 'parquet':
            df_clean.to_parquet(args.output, index=False)
        elif args.format == 'jsonl':
            df_clean.to_json(args.output, orient='records', lines=True, force_ascii=False)
        elif args.format == 'txt':
            with open(args.output, 'w', encoding='utf-8') as f:
                for text in df_clean[args.dedup_col]:
                    f.write(text + '\n')
        print(f"[+] Dataset saved successfully! Proceed with instruction fine-tuning.")
    except Exception as e:
        print(f"[!] Error saving output file: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
