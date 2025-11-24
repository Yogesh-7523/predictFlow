# ml-service (PredictFlow)

FastAPI-based ML microservice for PredictFlow.

Endpoints:
- `POST /predict` - body: `{ "txnId":.., "userId":.., "amount":.., "merchant":.., "retryCount":.. }` → returns `{ "probSuccess": float, "recommendedDelay": int }
- `GET /health` - basic health check

Setup (local, Windows PowerShell):

```powershell
cd d:\project\PredictFlow\ml-service
python -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
python train_model.py   # creates model.pkl
uvicorn app:app --reload --port 5000
```

Docker (optional):

```powershell
# build and run using docker
cd d:\project\PredictFlow\ml-service
docker build -t predictflow-ml:local .
docker run -p 5000:5000 --name predictflow_ml predictflow-ml:local
```

Notes:
- `train_model.py` generates `model.pkl` (scikit-learn pipeline). Place that `model.pkl` next to `app.py` before running the container to enable model-based predictions.
- If `model.pkl` is missing, the service uses a lightweight heuristic fallback.
- To integrate with the `retry-engine` running in Docker Compose, either run this ML service inside Docker Compose or run it locally and keep `ml.service.url` in `retry-engine` pointing to `http://host.docker.internal:5000/predict` (if retry-engine runs in Docker on Windows).
