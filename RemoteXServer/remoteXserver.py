import socket
import pyautogui
import json
import zlib
import tkinter as tk
from tkinter import scrolledtext

def control_cursor(data):
    try:
        decompressed_data = zlib.decompress(data).decode('utf-8')
        data_dict = json.loads(decompressed_data)
        dx = data_dict.get('dx', 0)
        dy = data_dict.get('dy', 0)

        if dx != 0 or dy != 0:
            current_x, current_y = pyautogui.position()
            new_x = current_x + dx
            new_y = current_y + dy

            screen_width, screen_height = pyautogui.size()
            new_x = max(10, min(new_x, screen_width - 10))
            new_y = max(10, min(new_y, screen_height - 10))

            pyautogui.moveTo(new_x, new_y)
    except Exception as e:
        log_message(f"Błąd podczas przetwarzania danych: {e}")

def log_message(message):
    log_area.insert(tk.END, message + "\n")
    log_area.see(tk.END)  # Przewiń do końca

def start_server():
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    server_socket.bind(('0.0.0.0', 5000))

    log_message("Serwer UDP uruchomiony, oczekiwanie na połączenie...")
    log_message("Naciśnij 'Zamknij', aby wyłączyć serwer.")

    connected_clients = set()

    def listen_for_data():
        try:
            while True:
                data, client_address = server_socket.recvfrom(1024)
                client_ip = client_address[0]

                if client_ip not in connected_clients:
                    log_message(f"Nowe połączenie z urządzeniem: {client_ip}")
                    connected_clients.add(client_ip)

                control_cursor(data)
        except Exception as e:
            log_message(f"Błąd: {e}")

    # Uruchom nasłuchiwanie w osobnym wątku
    import threading
    threading.Thread(target=listen_for_data, daemon=True).start()

def stop_server():
    root.destroy()

# Tworzenie interfejsu okienkowego
root = tk.Tk()
root.title("RemoteX Serwer")

log_area = scrolledtext.ScrolledText(root, wrap=tk.WORD, width=50, height=20)
log_area.pack(padx=10, pady=10)

start_button = tk.Button(root, text="Uruchom serwer", command=start_server)
start_button.pack(pady=5)

stop_button = tk.Button(root, text="Zamknij", command=stop_server)
stop_button.pack(pady=5)
root.mainloop()