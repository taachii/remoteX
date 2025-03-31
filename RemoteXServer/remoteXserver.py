import socket
import pyautogui
import json
import tkinter as tk
from tkinter import scrolledtext, font, messagebox
import threading

def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        local_ip = s.getsockname()[0]
        s.close()
        return local_ip
    except Exception as e:
        log_message(f"Błąd podczas pobierania adresu IP: {e}", "error")
        return "Nie można uzyskać adresu IP"

def control_cursor(data):
    try:
        data_dict = json.loads(data)

        if data_dict.get("action") == "media":
            handle_media_command(data_dict.get("command"))
        elif data_dict.get("action") == "tap":
            pyautogui.click()
        elif data_dict.get("action") == "right_click":
            pyautogui.rightClick()
        else:
            dx = data_dict.get('dx', 0)
            dy = data_dict.get('dy', 0)
            
            if dx != 0 or dy != 0:
                current_x, current_y = pyautogui.position()
                new_x = max(10, min(current_x + dx, pyautogui.size()[0] - 10))
                new_y = max(10, min(current_y + dy, pyautogui.size()[1] - 10))
                pyautogui.moveTo(new_x, new_y)
                
    except Exception as e:
        log_message(f"Błąd przetwarzania: {e}", "error")

def handle_media_command(command):
    if command == "play_pause":
        pyautogui.press('playpause')
    elif command == "next":
        pyautogui.press('nexttrack')
    elif command == "previous":
        pyautogui.press('prevtrack')
    elif command == "volume_up":
        pyautogui.press('volumeup')
    elif command == "volume_down":
        pyautogui.press('volumedown')

def log_message(message, message_type="info"):
    if message_type == "info":
        prefix = ">> "
        color = "blue"
    elif message_type == "error":
        prefix = "!! "
        color = "red"
    elif message_type == "success":
        prefix = "<< "
        color = "green"
    else:
        prefix = ">> "
        color = "black"

    log_area.tag_config(message_type, foreground=color)
    log_area.insert(tk.END, prefix + message + "\n", message_type)
    log_area.see(tk.END)

def start_server():
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    server_socket.bind(('0.0.0.0', 5000))

    local_ip = get_local_ip()
    ip_label.config(text=f"Adres IP: {local_ip}, Port: 5000")
    log_message(f"Serwer UDP uruchomiony. Adres IP serwera: {local_ip}, Port: 5000", "success")
    log_message("Oczekiwanie na połączenie z telefonem...", "info")
    log_message("Naciśnij 'Zamknij', aby wyłączyć serwer.", "info")

    start_button.pack_forget()

    connected_clients = set()

    def listen_for_data():
        try:
            while True:
                data, client_address = server_socket.recvfrom(1024)
                client_ip = client_address[0]

                if data == b"ping":
                    server_socket.sendto(b"pong", client_address)
                    continue

                if client_ip not in connected_clients:
                    log_message(f"Nowe połączenie z urządzeniem: {client_ip}", "success")
                    connected_clients.add(client_ip)

                control_cursor(data)
        except Exception as e:
            log_message(f"Błąd: {e}", "error")

    threading.Thread(target=listen_for_data, daemon=True).start()

def stop_server():
    if messagebox.askokcancel("Zamknij", "Czy na pewno chcesz zamknąć serwer?"):
        root.destroy()

root = tk.Tk()
root.title("RemoteX Server")
root.geometry("900x600")

custom_font = font.Font(family="Helvetica", size=12)

ip_label = tk.Label(root, text="Adres IP: Nieznany", font=custom_font, fg="black")
ip_label.pack(pady=5)

log_area = scrolledtext.ScrolledText(root, wrap=tk.WORD, width=80, height=25, font=custom_font)
log_area.pack(padx=10, pady=10)

button_frame = tk.Frame(root)
button_frame.pack(pady=10)

start_button = tk.Button(button_frame, text="Uruchom serwer", command=start_server, bg="green", fg="white", font=custom_font)
start_button.pack(side=tk.LEFT, padx=5)

stop_button = tk.Button(button_frame, text="Zamknij", command=stop_server, bg="red", fg="white", font=custom_font)
stop_button.pack(side=tk.LEFT, padx=5)

root.mainloop()