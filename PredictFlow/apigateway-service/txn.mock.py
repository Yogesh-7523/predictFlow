# ...existing code...
from flask import Flask, request, jsonify
app = Flask(__name__)

@app.route('/txn/ping', methods=['GET'])
@app.route('/api/txn/ping')
def ping():
    return 'txn-service OK', 200

# ...existing code...
@app.route('/txn/echo', methods=['POST','GET'])
@app.route('/api/txn/echo', methods=['POST','GET'])   # <-- added
def echo():
    return jsonify({
        "path": request.path,
        "method": request.method,
        "headers": dict(request.headers),
        "body": request.get_json(silent=True)
    }), 200

# ...existing code...
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8082)