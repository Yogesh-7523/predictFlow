import os
import base64
import jwt
import time

print("=== JWT Verification ===")

# Load secret from environment
s = os.environ.get("JWT_SECRET", "")
print(f"JWT_SECRET loaded: {len(s)} chars ({s[:8]}...)")

# Fix padding for URL-safe base64 (JWT standard)
s += '=' * (-len(s) % 4)
print(f"After padding: {len(s)} chars")

# Decode secret to bytes using URL-SAFE base64 (CRITICAL FIX)
key = base64.urlsafe_b64decode(s)
print(f"Secret bytes: {len(key)} bytes")

# Read token
with open("token.txt", "r") as f:
    t = f.read().strip()
print(f"Token: {t[:50]}...")

try:
    # Verify JWT signature
    payload = jwt.decode(t, key, algorithms=["HS256"])
    print("? VERIFIED SUCCESSFULLY!")
    print("Payload:", payload)
    
    # Check expiration
    if payload.get("exp") < time.time():
        print("??  Token EXPIRED")
    else:
        print("? Token VALID (not expired)")
        
except jwt.ExpiredSignatureError:
    print("? TOKEN EXPIRED")
except jwt.InvalidTokenError as e:
    print(f"? INVALID TOKEN: {e}")
except Exception as e:
    print(f"? VERIFY FAILED: {e}")
