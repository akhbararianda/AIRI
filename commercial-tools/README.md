# AIRI Commercial Licensing Tools

These tools are for **AIRI Technology** administration and are not bundled into the customer installer.

## Security rule

The RSA private signing key must remain offline/private. Never commit `.pem`, `.pfx`, `.p12`, or `.key` files to GitHub. The repository `.gitignore` blocks these common key formats, but operational discipline is still required.

## Issue a license

Install the Python dependency:

```powershell
py -m pip install cryptography
```

Ask the customer to open **License & Trial** inside AIRI and send the displayed Machine ID.

Generate a lifetime Pro license:

```powershell
py commercial-tools\generate_license.py `
  --private-key C:\AIRI-PRIVATE\airi-license-private.pem `
  --customer "Customer Name" `
  --machine "AIRI-XXXX-XXXX-XXXX-XXXX-XXXX-XXXX" `
  --edition Pro
```

Generate a one-year license:

```powershell
py commercial-tools\generate_license.py `
  --private-key C:\AIRI-PRIVATE\airi-license-private.pem `
  --customer "Customer Name" `
  --machine "AIRI-XXXX-XXXX-XXXX-XXXX-XXXX-XXXX" `
  --edition Pro `
  --days 365
```

The output is an `AIRI1....` license key. Send only that license key to the customer. Do **not** send the private signing key.

## Recommended production evolution

Offline signed licensing is suitable for controlled early sales. For a larger public launch, add an AIRI Technology activation API so purchases, device limits, revocation, subscription expiry, refunds, and license recovery can be managed centrally.
