#!/usr/bin/env python3
"""
Mock standalone (sin dependencias externas, solo libreria estandar) de la API externa de
Fraud Check descrita en contracts/openapi-fraudcheck.yaml.

Este es el segundo contrato del proyecto (lado cliente): bank-creditcard-service lo consume
a traves de FraudCheckClient (andes-api-client-spring-boot-starter, cliente nombrado
"fraudCheck"). Dentro del mismo proceso Java ya existe un simulador equivalente
(FraudCheckSimulatorController), pero ese esta envuelto por AndesResponseBodyAdvice
(andes-api-server), que agrega el sobre {success, data, error, metadata} a TODAS las
respuestas del proceso. Este mock, en cambio, corre en un proceso totalmente independiente
y devuelve el JSON "plano" tal como lo define el contrato -util para probar el cliente en
aislamiento, como lo haria un servicio externo real de terceros.

Logica de riesgo (igual a FraudCheckSimulatorController, para que ambos simuladores sean
intercambiables sin cambiar el comportamiento esperado por los tests/demos):
    amount > 3000  -> riskScore 90 (alto)
    amount > 1000  -> riskScore 50 (medio)
    en otro caso   -> riskScore 10 (bajo)

Uso:
    python3 scripts/mock_fraudcheck_server.py [--port 9090]

Luego, para que bank-creditcard-service use este mock en vez del simulador interno, cambia
en application.yml (o via variable de entorno) la base-url del cliente "fraudCheck":
    andes.api.client.clients.fraudCheck.base-url=http://localhost:9090

Prueba manual:
    curl -s -X POST http://localhost:9090/internal/fraud-check \\
         -H 'Content-Type: application/json' \\
         -d '{"cardId": 1, "merchant": "Tienda XYZ", "amount": 3500}'
    # -> {"riskScore": 90}
"""
import argparse
import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

HIGH_RISK_AMOUNT = 3000
MEDIUM_RISK_AMOUNT = 1000

DEFAULT_PORT = 9090


def compute_risk_score(amount: float) -> int:
    if amount > HIGH_RISK_AMOUNT:
        return 90
    if amount > MEDIUM_RISK_AMOUNT:
        return 50
    return 10


class FraudCheckHandler(BaseHTTPRequestHandler):
    # Silencia el logging por defecto de BaseHTTPRequestHandler (ruidoso); se reemplaza
    # por mensajes propios mas legibles en log_message().
    def log_message(self, format, *args):  # noqa: A002 (nombre fijado por la clase base)
        print(f"[mock-fraudcheck] {self.address_string()} - {format % args}")

    def _send_json(self, status_code: int, payload: dict):
        body = json.dumps(payload).encode("utf-8")
        self.send_response(status_code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _send_error(self, status_code: int, error_code: str, message: str):
        self._send_json(status_code, {"errorCode": error_code, "errorMessage": message})

    def do_POST(self):  # noqa: N802 (nombre fijado por BaseHTTPRequestHandler)
        if self.path != "/internal/fraud-check":
            self._send_error(404, "NOT_FOUND", f"No existe el recurso: {self.path}")
            return

        length = int(self.headers.get("Content-Length", 0))
        raw_body = self.rfile.read(length) if length else b""
        try:
            request = json.loads(raw_body or b"{}")
        except json.JSONDecodeError:
            self._send_error(400, "INVALID_JSON", "El cuerpo de la solicitud no es JSON valido")
            return

        missing = [field for field in ("cardId", "merchant", "amount") if field not in request]
        if missing:
            self._send_error(
                400,
                "MISSING_FIELDS",
                f"Faltan campos requeridos: {', '.join(missing)}",
            )
            return

        try:
            amount = float(request["amount"])
        except (TypeError, ValueError):
            self._send_error(400, "INVALID_AMOUNT", "El campo amount debe ser numerico")
            return

        risk_score = compute_risk_score(amount)
        self._send_json(200, {"riskScore": risk_score})


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--port",
        type=int,
        default=DEFAULT_PORT,
        help=f"Puerto donde escuchar (default: {DEFAULT_PORT}, igual al contrato)",
    )
    args = parser.parse_args()

    server = ThreadingHTTPServer(("0.0.0.0", args.port), FraudCheckHandler)
    print(
        f"[mock-fraudcheck] Escuchando en http://localhost:{args.port}"
        " (POST /internal/fraud-check) - Ctrl+C para detener"
    )
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\n[mock-fraudcheck] Detenido.")
    finally:
        server.server_close()


if __name__ == "__main__":
    main()
