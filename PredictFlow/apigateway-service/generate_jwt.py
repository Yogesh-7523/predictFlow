import time
import base64
import jwt
import os

secret_b64 = os.environ.get("JWT_SECRET")
if not secret_b64:
    raise SystemExit("JWT_SECRET environment variable not set")

# Fix padding + URL-safe decode (CRITICAL)
secret_b64 += '=' * (-len(secret_b64) % 4)
secret_bytes = base64.urlsafe_b64decode(secret_b64)

payload = {
    "sub": "tester",
    "iat": int(time.time()),
    "exp": int(time.time()) + 3600
}

token = jwt.encode(payload, secret_bytes, algorithm="HS256")
print(token.decode() if isinstance(token, bytes) else token)
