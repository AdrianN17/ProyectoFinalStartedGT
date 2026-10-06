#!/usr/bin/env python3
import argparse
import json
from http.server import BaseHTTPRequestHandler, HTTPServer


class FraudCheckHandler(BaseHTTPRequestHandler):
    def _json_response(self, status: int, payload: dict):
        body = json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_POST(self):
        if self.path != "/api/v1/fraud-check":
            self._json_response(404, {"error": "not found"})
            return

        length = int(self.headers.get("Content-Length", "0"))
        raw = self.rfile.read(length)
        try:
            data = json.loads(raw.decode("utf-8"))
        except json.JSONDecodeError:
            self._json_response(400, {"error": "invalid json"})
            return

        card = str(data.get("cardNumber", "")).strip()
        document = str(data.get("documentNumber", "")).strip()

        # Regla simple de mock: si termina en 0000 se rechaza, caso contrario aprobado.
        rejected = card.endswith("0000") or document.endswith("9999")
        payload = {
            "approved": not rejected,
            "score": 92 if not rejected else 18,
            "reason": "APPROVED_BY_MOCK_RULE" if not rejected else "REJECTED_BY_MOCK_RULE"
        }
        self._json_response(200, payload)

    def log_message(self, fmt, *args):
        return


def main():
    parser = argparse.ArgumentParser(description="Mock backend for fraud-check API")
    parser.add_argument("--port", type=int, default=9090)
    args = parser.parse_args()

    server = HTTPServer(("0.0.0.0", args.port), FraudCheckHandler)
    print(f"Fraud-check mock server running on http://localhost:{args.port}")
    server.serve_forever()


if __name__ == "__main__":
    main()
