import socket

# Diccionario de ejemplo: dominio -> IP
dns_records = {
    "example.com": "93.184.216.34",
    "midominio.com": "192.168.1.10",
    "google.com": "142.250.190.78"
}

# Configuración del servidor
UDP_IP = "127.0.0.1"
UDP_PORT = 8053  # Puerto típico de DNS es 53, pero usamos 8053 para pruebas

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind((UDP_IP, UDP_PORT))

print(f"Servidor DNS escuchando en {UDP_IP}:{UDP_PORT}...")

while True:
    data, addr = sock.recvfrom(1024)
    domain = data.decode("utf-8")
    print(f"Consulta recibida: {domain} de {addr}")

    ip = dns_records.get(domain, "No encontrado")
    sock.sendto(ip.encode("utf-8"), addr)