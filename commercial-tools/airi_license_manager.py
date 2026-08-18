from __future__ import annotations

import base64
import datetime as dt
import hashlib
import json
import os
from pathlib import Path
import tkinter as tk
from tkinter import filedialog, messagebox, ttk

from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding

PRODUCT = "AIRI-DM"
APP_TITLE = "AIRI License Manager — Admin Edition"
EXPECTED_PUBLIC_FINGERPRINT = "00F12E8BC33FCBD1495168F3F54800B1C80AD041F1FBC5D75C49707C6D4891CE"
AIRI_BLUE = "#0F4C81"
AIRI_SKY = "#58A6FF"
AIRI_CREAM = "#FFF8EA"
AIRI_NAVY = "#17283F"
AIRI_MUTED = "#607086"
WHITE = "#FFFFFF"
BORDER = "#DCE6F0"
GREEN = "#17643A"
RED = "#A12A2A"


def b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).decode("ascii").rstrip("=")


def public_fingerprint(private_key) -> str:
    der = private_key.public_key().public_bytes(
        serialization.Encoding.DER,
        serialization.PublicFormat.SubjectPublicKeyInfo,
    )
    return hashlib.sha256(der).hexdigest().upper()


def expiry_timestamp(mode: str, custom_days: str, explicit_date: str) -> int:
    if mode == "Lifetime":
        return 0
    if mode == "30 Days":
        days = 30
    elif mode == "1 Year":
        days = 365
    elif mode == "Custom Days":
        days = int(custom_days)
        if days <= 0:
            raise ValueError("Custom days must be greater than zero.")
    elif mode == "Expiry Date":
        d = dt.datetime.strptime(explicit_date.strip(), "%Y-%m-%d").replace(
            hour=23, minute=59, second=59, tzinfo=dt.timezone.utc
        )
        return int(d.timestamp())
    else:
        raise ValueError("Unknown validity mode.")
    return int((dt.datetime.now(dt.timezone.utc) + dt.timedelta(days=days)).timestamp())


def issue_license(private_key_path: Path, customer: str, machine: str, edition: str,
                  validity_mode: str, custom_days: str, explicit_date: str) -> tuple[str, dict, str]:
    if not customer.strip():
        raise ValueError("Customer name is required.")
    machine = machine.strip().upper()
    if not (machine == "*" or machine.startswith("AIRI-")):
        raise ValueError("Machine ID must start with AIRI- or be * for a transferable license.")
    if not private_key_path.exists():
        raise ValueError("Private signing key was not found.")

    private_key = serialization.load_pem_private_key(private_key_path.read_bytes(), password=None)
    fp = public_fingerprint(private_key)
    if fp != EXPECTED_PUBLIC_FINGERPRINT:
        raise ValueError(
            "This private key does not match the AIRI DM public key.\n\n"
            f"Expected: {EXPECTED_PUBLIC_FINGERPRINT}\n"
            f"Loaded:   {fp}"
        )

    expiry = expiry_timestamp(validity_mode, custom_days, explicit_date)
    payload = {
        "product": PRODUCT,
        "customer": customer.strip(),
        "edition": edition.strip() or "Pro",
        "machine": machine,
        "expiry": str(expiry),
    }
    raw = json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode("utf-8")
    signature = private_key.sign(raw, padding.PKCS1v15(), hashes.SHA256())
    license_key = f"AIRI1.{b64url(raw)}.{b64url(signature)}"
    return license_key, payload, fp


class LicenseManagerApp(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title(APP_TITLE)
        self.geometry("980x700")
        self.minsize(900, 640)
        self.configure(bg=AIRI_CREAM)

        base = Path(os.getenv("APPDATA", str(Path.home()))) / "AIRI Technology" / "License Manager"
        base.mkdir(parents=True, exist_ok=True)
        self.data_dir = base
        self.history_path = base / "licenses.json"

        self.key_path = tk.StringVar(value=str(base / "airi-license-private.pem"))
        self.customer = tk.StringVar()
        self.machine = tk.StringVar()
        self.edition = tk.StringVar(value="Pro")
        self.validity = tk.StringVar(value="Lifetime")
        self.custom_days = tk.StringVar(value="365")
        self.expiry_date = tk.StringVar(value=(dt.date.today() + dt.timedelta(days=365)).isoformat())
        self.status = tk.StringVar(value="Ready — load the AIRI Technology signing key")
        self.fingerprint = tk.StringVar(value="Signing key not loaded")
        self.license_text = None

        self._build_styles()
        self._build_ui()
        self._load_history_count()

    def _build_styles(self):
        style = ttk.Style(self)
        try:
            style.theme_use("clam")
        except tk.TclError:
            pass
        style.configure("TFrame", background=AIRI_CREAM)
        style.configure("Card.TFrame", background=WHITE, relief="flat")
        style.configure("TLabel", background=AIRI_CREAM, foreground=AIRI_NAVY, font=("Segoe UI", 10))
        style.configure("Card.TLabel", background=WHITE, foreground=AIRI_NAVY, font=("Segoe UI", 10))
        style.configure("Muted.Card.TLabel", background=WHITE, foreground=AIRI_MUTED, font=("Segoe UI", 9))
        style.configure("Title.TLabel", background=AIRI_CREAM, foreground=AIRI_NAVY, font=("Segoe UI Semibold", 24))
        style.configure("Brand.TLabel", background=AIRI_CREAM, foreground=AIRI_BLUE, font=("Segoe UI Semibold", 10))
        style.configure("Primary.TButton", font=("Segoe UI Semibold", 10), padding=(16, 10), foreground=WHITE, background=AIRI_BLUE)
        style.map("Primary.TButton", background=[("active", "#0B3D69")])
        style.configure("Secondary.TButton", font=("Segoe UI Semibold", 10), padding=(14, 9), foreground=AIRI_BLUE, background="#EDF5FC")
        style.map("Secondary.TButton", background=[("active", "#E2EFF9")])
        style.configure("TEntry", padding=8, fieldbackground=WHITE)
        style.configure("TCombobox", padding=7, fieldbackground=WHITE)

    def _card(self, parent, **grid):
        frame = tk.Frame(parent, bg=WHITE, highlightbackground=BORDER, highlightthickness=1, bd=0)
        frame.grid(**grid)
        return frame

    def _build_ui(self):
        root = ttk.Frame(self, padding=(28, 22, 28, 20))
        root.pack(fill="both", expand=True)
        root.columnconfigure(0, weight=1)
        root.columnconfigure(1, weight=1)
        root.rowconfigure(3, weight=1)

        ttk.Label(root, text="AIRI LICENSE MANAGER", style="Brand.TLabel").grid(row=0, column=0, sticky="w")
        ttk.Label(root, text="Admin Edition", style="Title.TLabel").grid(row=1, column=0, sticky="w", pady=(2, 0))
        ttk.Label(root, text="Issue signed licenses for AIRI Download Manager", foreground=AIRI_MUTED,
                  background=AIRI_CREAM, font=("Segoe UI", 10)).grid(row=2, column=0, sticky="w", pady=(3, 18))
        badge = tk.Label(root, text="AIRI Technology  •  Founder Akhbar Arianda", bg="#EDF5FC", fg=AIRI_BLUE,
                         font=("Segoe UI Semibold", 9), padx=12, pady=7)
        badge.grid(row=0, column=1, rowspan=2, sticky="e")

        left = self._card(root, row=3, column=0, sticky="nsew", padx=(0, 10))
        right = self._card(root, row=3, column=1, sticky="nsew", padx=(10, 0))
        for c in (left, right):
            c.columnconfigure(0, weight=1)

        tk.Label(left, text="Customer & Device", bg=WHITE, fg=AIRI_NAVY,
                 font=("Segoe UI Semibold", 14)).grid(row=0, column=0, sticky="w", padx=20, pady=(18, 4))
        tk.Label(left, text="Paste the Machine ID sent by the buyer.", bg=WHITE, fg=AIRI_MUTED,
                 font=("Segoe UI", 9)).grid(row=1, column=0, sticky="w", padx=20, pady=(0, 16))

        self._field(left, "Customer name", self.customer, 2)
        self._field(left, "Machine ID", self.machine, 4)

        tk.Label(left, text="Edition", bg=WHITE, fg=AIRI_NAVY, font=("Segoe UI Semibold", 9)).grid(row=6, column=0, sticky="w", padx=20, pady=(10, 5))
        ttk.Combobox(left, textvariable=self.edition, values=("Pro", "Business", "Enterprise"), state="readonly").grid(row=7, column=0, sticky="ew", padx=20)

        tk.Label(left, text="Validity", bg=WHITE, fg=AIRI_NAVY, font=("Segoe UI Semibold", 9)).grid(row=8, column=0, sticky="w", padx=20, pady=(12, 5))
        validity = ttk.Combobox(left, textvariable=self.validity,
                                values=("Lifetime", "30 Days", "1 Year", "Custom Days", "Expiry Date"), state="readonly")
        validity.grid(row=9, column=0, sticky="ew", padx=20)
        validity.bind("<<ComboboxSelected>>", lambda e: self._refresh_validity_fields())

        self.custom_frame = tk.Frame(left, bg=WHITE)
        self.custom_frame.grid(row=10, column=0, sticky="ew", padx=20, pady=(10, 0))
        self.custom_frame.columnconfigure(0, weight=1)
        tk.Label(self.custom_frame, text="Custom days", bg=WHITE, fg=AIRI_NAVY, font=("Segoe UI Semibold", 9)).grid(row=0, column=0, sticky="w")
        ttk.Entry(self.custom_frame, textvariable=self.custom_days).grid(row=1, column=0, sticky="ew", pady=(5, 0))

        self.expiry_frame = tk.Frame(left, bg=WHITE)
        self.expiry_frame.grid(row=11, column=0, sticky="ew", padx=20, pady=(10, 0))
        self.expiry_frame.columnconfigure(0, weight=1)
        tk.Label(self.expiry_frame, text="Expiry date (YYYY-MM-DD)", bg=WHITE, fg=AIRI_NAVY, font=("Segoe UI Semibold", 9)).grid(row=0, column=0, sticky="w")
        ttk.Entry(self.expiry_frame, textvariable=self.expiry_date).grid(row=1, column=0, sticky="ew", pady=(5, 0))
        self._refresh_validity_fields()

        keybox = tk.Frame(left, bg="#F8FBFF", highlightbackground=BORDER, highlightthickness=1)
        keybox.grid(row=12, column=0, sticky="ew", padx=20, pady=(18, 10))
        keybox.columnconfigure(0, weight=1)
        tk.Label(keybox, text="AIRI signing key", bg="#F8FBFF", fg=AIRI_NAVY,
                 font=("Segoe UI Semibold", 9)).grid(row=0, column=0, sticky="w", padx=12, pady=(10, 2))
        tk.Label(keybox, textvariable=self.fingerprint, bg="#F8FBFF", fg=AIRI_MUTED,
                 font=("Consolas", 8), wraplength=370, justify="left").grid(row=1, column=0, sticky="w", padx=12, pady=(0, 6))
        ttk.Button(keybox, text="Select private key", style="Secondary.TButton", command=self.select_key).grid(row=2, column=0, sticky="w", padx=12, pady=(0, 10))

        ttk.Button(left, text="Generate License", style="Primary.TButton", command=self.generate).grid(row=13, column=0, sticky="ew", padx=20, pady=(8, 20))

        tk.Label(right, text="Generated License", bg=WHITE, fg=AIRI_NAVY,
                 font=("Segoe UI Semibold", 14)).grid(row=0, column=0, sticky="w", padx=20, pady=(18, 4))
        tk.Label(right, text="Send only this AIRI1… key to the buyer.", bg=WHITE, fg=AIRI_MUTED,
                 font=("Segoe UI", 9)).grid(row=1, column=0, sticky="w", padx=20, pady=(0, 12))

        text_wrap = tk.Frame(right, bg="#F8FBFF", highlightbackground=BORDER, highlightthickness=1)
        text_wrap.grid(row=2, column=0, sticky="nsew", padx=20, pady=(0, 12))
        right.rowconfigure(2, weight=1)
        text_wrap.rowconfigure(0, weight=1)
        text_wrap.columnconfigure(0, weight=1)
        self.license_text = tk.Text(text_wrap, wrap="word", bd=0, relief="flat", bg="#F8FBFF", fg=AIRI_NAVY,
                                    insertbackground=AIRI_BLUE, font=("Consolas", 10), padx=12, pady=12)
        self.license_text.grid(row=0, column=0, sticky="nsew")

        actions = tk.Frame(right, bg=WHITE)
        actions.grid(row=3, column=0, sticky="ew", padx=20, pady=(0, 12))
        actions.columnconfigure(0, weight=1)
        actions.columnconfigure(1, weight=1)
        ttk.Button(actions, text="Copy Key", style="Primary.TButton", command=self.copy_key).grid(row=0, column=0, sticky="ew", padx=(0, 5))
        ttk.Button(actions, text="Save .txt", style="Secondary.TButton", command=self.save_key).grid(row=0, column=1, sticky="ew", padx=(5, 0))

        self.meta = tk.Label(right, text="No license generated yet.", bg=WHITE, fg=AIRI_MUTED,
                             font=("Segoe UI", 9), justify="left", anchor="w", wraplength=400)
        self.meta.grid(row=4, column=0, sticky="ew", padx=20, pady=(0, 16))

        statusbar = tk.Frame(root, bg=AIRI_BLUE)
        statusbar.grid(row=4, column=0, columnspan=2, sticky="ew", pady=(16, 0))
        statusbar.columnconfigure(0, weight=1)
        tk.Label(statusbar, textvariable=self.status, bg=AIRI_BLUE, fg=WHITE,
                 font=("Segoe UI Semibold", 9), padx=12, pady=8).grid(row=0, column=0, sticky="w")
        self.history_label = tk.Label(statusbar, text="", bg=AIRI_BLUE, fg="#DCEEFF",
                                      font=("Segoe UI", 9), padx=12, pady=8)
        self.history_label.grid(row=0, column=1, sticky="e")

    def _field(self, parent, label: str, variable: tk.StringVar, row: int):
        tk.Label(parent, text=label, bg=WHITE, fg=AIRI_NAVY, font=("Segoe UI Semibold", 9)).grid(row=row, column=0, sticky="w", padx=20, pady=(8, 5))
        ttk.Entry(parent, textvariable=variable).grid(row=row + 1, column=0, sticky="ew", padx=20)

    def _refresh_validity_fields(self):
        mode = self.validity.get()
        if mode == "Custom Days":
            self.custom_frame.grid()
        else:
            self.custom_frame.grid_remove()
        if mode == "Expiry Date":
            self.expiry_frame.grid()
        else:
            self.expiry_frame.grid_remove()

    def select_key(self):
        p = filedialog.askopenfilename(title="Select AIRI private signing key", filetypes=[("PEM private key", "*.pem"), ("All files", "*.*")])
        if not p:
            return
        self.key_path.set(p)
        try:
            private_key = serialization.load_pem_private_key(Path(p).read_bytes(), password=None)
            fp = public_fingerprint(private_key)
            ok = fp == EXPECTED_PUBLIC_FINGERPRINT
            self.fingerprint.set(("✓ MATCH  " if ok else "✕ WRONG KEY  ") + fp)
            self.status.set("AIRI signing key verified" if ok else "Wrong signing key — do not issue licenses")
        except Exception as exc:
            self.fingerprint.set("Could not read signing key")
            messagebox.showerror(APP_TITLE, str(exc))

    def generate(self):
        try:
            license_key, payload, fp = issue_license(
                Path(self.key_path.get()), self.customer.get(), self.machine.get(), self.edition.get(),
                self.validity.get(), self.custom_days.get(), self.expiry_date.get()
            )
        except Exception as exc:
            messagebox.showerror("Cannot generate license", str(exc))
            self.status.set("License generation failed")
            return

        self.license_text.delete("1.0", "end")
        self.license_text.insert("1.0", license_key)
        expiry = int(payload["expiry"])
        expiry_label = "Lifetime" if expiry == 0 else dt.datetime.fromtimestamp(expiry, dt.timezone.utc).strftime("%Y-%m-%d UTC")
        self.meta.configure(text=(
            f"Customer: {payload['customer']}\n"
            f"Edition: {payload['edition']}  •  Expiry: {expiry_label}\n"
            f"Machine: {payload['machine']}\n"
            f"Signer fingerprint: {fp[:16]}…"
        ))
        self._append_history(payload, license_key)
        self._load_history_count()
        self.status.set("License generated and signed successfully")

    def copy_key(self):
        key = self.license_text.get("1.0", "end").strip()
        if not key:
            messagebox.showinfo(APP_TITLE, "Generate a license first.")
            return
        self.clipboard_clear()
        self.clipboard_append(key)
        self.update()
        self.status.set("License key copied — send only this key to the buyer")

    def save_key(self):
        key = self.license_text.get("1.0", "end").strip()
        if not key:
            messagebox.showinfo(APP_TITLE, "Generate a license first.")
            return
        customer = "".join(c if c.isalnum() or c in "-_" else "_" for c in self.customer.get().strip()) or "customer"
        p = filedialog.asksaveasfilename(title="Save AIRI license", defaultextension=".txt",
                                         initialfile=f"AIRI-License-{customer}.txt", filetypes=[("Text file", "*.txt")])
        if p:
            Path(p).write_text(key + "\n", encoding="utf-8")
            self.status.set(f"License saved: {p}")

    def _append_history(self, payload: dict, license_key: str):
        history = []
        if self.history_path.exists():
            try:
                history = json.loads(self.history_path.read_text(encoding="utf-8"))
            except Exception:
                history = []
        history.append({
            "issued_at": dt.datetime.now(dt.timezone.utc).isoformat(),
            "customer": payload["customer"],
            "edition": payload["edition"],
            "machine": payload["machine"],
            "expiry": payload["expiry"],
            "license_preview": license_key[:24] + "…",
        })
        self.history_path.write_text(json.dumps(history[-1000:], ensure_ascii=False, indent=2), encoding="utf-8")

    def _load_history_count(self):
        count = 0
        if self.history_path.exists():
            try:
                count = len(json.loads(self.history_path.read_text(encoding="utf-8")))
            except Exception:
                pass
        self.history_label.configure(text=f"{count} license(s) issued on this PC")


if __name__ == "__main__":
    LicenseManagerApp().mainloop()
