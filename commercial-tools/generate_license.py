#!/usr/bin/env python3
"""Offline AIRI Download Manager license issuer.

The private RSA signing key must stay outside the public repository.
Install dependency: pip install cryptography
"""
from __future__ import annotations
import argparse
import base64
import datetime as dt
import json
from pathlib import Path
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding


def b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).decode("ascii").rstrip("=")


def expiry_from_args(days: int, expires: str | None) -> int:
    if expires:
        d = dt.datetime.strptime(expires, "%Y-%m-%d").replace(
            hour=23, minute=59, second=59, tzinfo=dt.timezone.utc
        )
        return int(d.timestamp())
    if days > 0:
        return int((dt.datetime.now(dt.timezone.utc) + dt.timedelta(days=days)).timestamp())
    return 0


def main() -> int:
    p = argparse.ArgumentParser(description="Issue a signed AIRI Download Manager license")
    p.add_argument("--private-key", required=True, help="Path to AIRI RSA private PEM key")
    p.add_argument("--customer", required=True, help="Customer/person/company name")
    p.add_argument("--machine", required=True, help="Machine ID shown in AIRI, or * for transferable license")
    p.add_argument("--edition", default="Pro", help="Edition shown in AIRI (default: Pro)")
    p.add_argument("--days", type=int, default=0, help="License validity in days; 0 = lifetime")
    p.add_argument("--expires", help="Explicit UTC expiry date YYYY-MM-DD; overrides --days")
    p.add_argument("--output", help="Optional file to save the generated license")
    args = p.parse_args()

    key = serialization.load_pem_private_key(Path(args.private_key).read_bytes(), password=None)
    payload = {
        "product": "AIRI-DM",
        "customer": args.customer,
        "edition": args.edition,
        "machine": args.machine,
        "expiry": str(expiry_from_args(args.days, args.expires)),
    }
    raw = json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode("utf-8")
    signature = key.sign(raw, padding.PKCS1v15(), hashes.SHA256())
    license_key = f"AIRI1.{b64url(raw)}.{b64url(signature)}"

    print("AIRI Download Manager License")
    print(f"Customer : {args.customer}")
    print(f"Edition  : {args.edition}")
    print(f"Machine  : {args.machine}")
    print(f"Expiry   : {payload['expiry']} (0 = lifetime)")
    print("\nLICENSE KEY:\n")
    print(license_key)
    if args.output:
        Path(args.output).write_text(license_key + "\n", encoding="utf-8")
        print(f"\nSaved to: {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
