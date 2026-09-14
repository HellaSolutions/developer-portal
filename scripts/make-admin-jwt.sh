#!/bin/bash
# Signs a local RS256 JWT with `scope: portal:admin` using private.pem (the key matching
# src/main/resources/jwt-public.pem). Only valid against the `dev` profile, which trusts
# that public key directly instead of doing OIDC issuer discovery.
#
# Usage: scripts/make-admin-jwt.sh [path-to-private.pem]
set -euo pipefail

PRIVATE_KEY="${1:-private.pem}"

b64url() {
  openssl base64 -A | tr '+/' '-_' | tr -d '='
}

NOW=$(date +%s)
EXP=$((NOW + 3600))

HEADER='{"alg":"RS256","typ":"JWT"}'
PAYLOAD=$(printf '{"sub":"local-admin","scope":"portal:admin","iat":%d,"exp":%d}' "$NOW" "$EXP")

HEADER_B64=$(printf '%s' "$HEADER" | b64url)
PAYLOAD_B64=$(printf '%s' "$PAYLOAD" | b64url)
SIGNING_INPUT="${HEADER_B64}.${PAYLOAD_B64}"
SIGNATURE=$(printf '%s' "$SIGNING_INPUT" | openssl dgst -sha256 -sign "$PRIVATE_KEY" | b64url)

echo "${SIGNING_INPUT}.${SIGNATURE}"
