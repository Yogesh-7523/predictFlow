import joblib
import numpy as np
import pandas as pd
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.compose import ColumnTransformer
from sklearn.linear_model import LogisticRegression


def make_data(n=3000):
    rng = np.random.RandomState(42)
    merchants = [
        "Amazon","Walmart","Apple","Flipkart","Meta","Twitter","Twi",
        "Amex","Tower Research","SpaceX","MicroChina","QUANT","RazorPay","Paypal","unknown"
    ]
    rows = []
    for _ in range(n):
        merchant = rng.choice(merchants)
        amount = float(abs(rng.normal(loc=5000, scale=20000)))
        retry = int(rng.poisson(0.7))
        base = 0.7 - (amount / 200000.0) - (retry * 0.18)
        merchant_penalty = 0.0
        if merchant in ("MicroChina", "Tower Research"):
            merchant_penalty += 0.15
        prob = max(0.01, min(0.99, base - merchant_penalty))
        label = 1 if rng.rand() < prob else 0
        rows.append({"amount": amount, "retryCount": retry, "merchant": merchant, "label": label})
    return pd.DataFrame(rows)


def train_and_save(path="model.pkl"):
    df = make_data(3000)
    X = df[["amount", "retryCount", "merchant"]]
    y = df["label"]

    numeric_features = ["amount", "retryCount"]
    categorical_features = ["merchant"]

    numeric_transformer = StandardScaler()
    categorical_transformer = OneHotEncoder(handle_unknown='ignore')

    preprocessor = ColumnTransformer(
        transformers=[
            ('num', numeric_transformer, numeric_features),
            ('cat', categorical_transformer, categorical_features),
        ])

    clf = Pipeline(steps=[('pre', preprocessor),
                          ('clf', LogisticRegression(max_iter=400))])
    clf.fit(X, y)
    joblib.dump(clf, path)
    print("Saved model to", path)


if __name__ == "__main__":
    train_and_save()
