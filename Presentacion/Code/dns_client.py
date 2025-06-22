import socket

UDP_IP = "127.0.0.1"
UDP_PORT = 8053

dominio = input("Introduce el dominio que quieres consultar: ")

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.sendto(dominio.encode("utf-8"), (UDP_IP, UDP_PORT))

data, _ = sock.recvfrom(1024)
respuesta = data.decode("utf-8")
print(f"La IP correspondiente a {dominio} es: {respuesta}")