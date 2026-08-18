# AIRI License Manager — Admin Edition

Private administration tooling for **AIRI Technology**. These files are **not** bundled with the customer installer.

**Founder:** Akhbar Arianda

## AIRI License Manager v1.1

The Windows admin application is the recommended way to issue and manage licenses.

v1.1 provides:

- Signed AIRI license generation from a buyer Machine ID
- Pro / Business / Enterprise editions
- Lifetime, 30-day, 1-year, custom-day and explicit expiry licenses
- RSA private-key fingerprint validation before signing
- Local SQLite customer/license database at `%APPDATA%\AIRI Technology\License Manager\licenses.db`
- Searchable license history
- Copy an existing license key for resend
- Load an existing customer for renewal/reissue
- CSV export for administration/reporting
- Automatic migration from the earlier `licenses.json` history

## Recommended admin workflow

1. Customer opens **License & Trial** in AIRI Download Manager.
2. Customer sends the displayed Machine ID, for example `AIRI-XXXX-XXXX-XXXX-XXXX-XXXX-XXXX`.
3. Open **AIRI License Manager — Admin Edition**.
4. Select the AIRI Technology private signing key.
5. Enter customer name and Machine ID.
6. Choose edition and validity.
7. Click **Generate License**.
8. Click **Copy Key** and send only the generated `AIRI1....` key to the customer.
9. Customer pastes the key into **License & Trial → Activate License**.

The admin GUI checks that the loaded private key matches the production public key embedded in AIRI Download Manager before it can issue a license.

## Security rule

The RSA private signing key must remain offline/private. Never commit `.pem`, `.pfx`, `.p12`, or `.key` files to GitHub, cloud drives shared with customers, support tickets, or public messaging channels.

The private key is **not embedded** in the Admin Edition executable. The application loads it from a file that only AIRI Technology controls.

Production public-key fingerprint expected by the admin app:

`00F12E8BC33FCBD1495168F3F54800B1C80AD041F1FBC5D75C49707C6D4891CE`

## License history

Each successfully issued license is stored locally in `licenses.db`. Open **History** to search by customer, Machine ID or edition, resend a key, load a customer for renewal, or export the records to CSV.

The database contains customer/license records but does **not** contain the AIRI private signing key.

## Command-line fallback

The original command-line issuer remains available for recovery or automation:

```powershell
py -m pip install cryptography
py commercial-tools\generate_license.py `
  --private-key C:\AIRI-PRIVATE\airi-license-private.pem `
  --customer "Customer Name" `
  --machine "AIRI-XXXX-XXXX-XXXX-XXXX-XXXX-XXXX" `
  --edition Pro
```

Add `--days 365` for a one-year license, or omit it for a lifetime license.

## Public launch evolution

Offline signed licensing is appropriate for controlled early sales. Before a larger public launch, add an AIRI Technology activation API so payment confirmation, device limits, revocation, subscription expiry, refunds, license recovery, reseller controls, and activation analytics can be managed centrally.
