from fastapi import FastAPI
from pydantic import BaseModel
from typing import Optional
import joblib
import os
import numpy as np


class PredictRequest(BaseModel):
    txnId: Optional[int]
    userId: Optional[int]
    amount: float
    reason: Optional[str] = None
    retryCount: Optional[int] = 0
    merchant: Optional[str] = None


class PredictResponse(BaseModel):
    probSuccess: float
    recommendedDelay: int


app = FastAPI(title="PredictFlow ML Service")

MODEL_PATH = "model.pkl"
model = None


def load_model():
    global model
    if os.path.exists(MODEL_PATH):
        try:
            model = joblib.load(MODEL_PATH)
            print("Model loaded from", MODEL_PATH)
        except Exception as e:
            print("Failed to load model:", e)
            model = None
    else:
        print("No model file found at", MODEL_PATH)


@app.on_event("startup")
def startup():
    load_model()


def fallback_predict(req: PredictRequest) -> PredictResponse:
    # Simple heuristic fallback when model is not available
    base = 0.5
    if req.amount < 1000:
        base += 0.15
    if req.retryCount and req.retryCount == 0:
        base += 0.05
    if req.retryCount and req.retryCount > 2:
        base -= 0.2
    merchant_penalty = 0.0
    high_risk = {"MicroChina", "Tower Research"}
    if req.merchant in high_risk:
        merchant_penalty += 0.15
    prob = max(0.01, min(0.99, base - merchant_penalty))
    delay = int(max(5, (1.0 - prob) * 120))
    return PredictResponse(probSuccess=round(prob, 3), recommendedDelay=delay)


@app.post("/predict", response_model=PredictResponse)
def predict(req: PredictRequest):
    if model is None:
        return fallback_predict(req)
    try:
        # Prepare a single-row input expected by saved sklearn pipeline
        X = [{
            "amount": float(req.amount),
            "retryCount": int(req.retryCount or 0),
            "merchant": req.merchant or "unknown"
        }]
        # If model is a scikit-learn pipeline expecting DataFrame-like input
        try:
            proba = model.predict_proba(X)[0][1]
        except Exception:
            # Try transforming to numeric array if pipeline expects numpy
            import pandas as pd
            df = pd.DataFrame(X)
            proba = model.predict_proba(df)[0][1]
        delay = int(max(5, (1.0 - float(proba)) * 120))
        return PredictResponse(probSuccess=round(float(proba), 3), recommendedDelay=delay)
    except Exception as e:
        print("Prediction error:", e)
        return fallback_predict(req)


@app.get("/health")
def health():
    return {"status": "ok", "model_loaded": model is not None}
