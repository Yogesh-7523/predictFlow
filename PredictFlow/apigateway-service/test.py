import time
import jwt
import base64

# Same secret from application.yml
JWT_SECRET_BASE64 = "KljaodO4XFKpB7ACmZ1wCObKFSFbbAMDRC4hZ57nehw="

# DECODE to raw bytes like Spring does
JWT_SECRET_BYTES = base64.b64decode(JWT_SECRET_BASE64)

payload = {
    "sub": "tester",
    "iat": int(time.time()),
    "exp": int(time.time()) + 3600
}

token = jwt.encode(payload, JWT_SECRET_BYTES, algorithm="HS256")
if isinstance(token, bytes):
    token = token.decode()
print(token)
