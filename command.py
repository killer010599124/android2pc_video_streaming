import socket
from threading import Thread

PORT = 5000  # Use the same port number as in the Android app

def receiveCommand(client_socket):
    try:
        received_text = client_socket.recv(1024).decode()
        print("Received text:", received_text)
        # received_text = received_text[1:]  # Remove the emoji

        client_socket.close()
    except Exception as e:
        print("Error:", str(e))

def sendCommand(client_socket):
    try:
        while True:
            command = input("Enter your command: ").strip()
            if command:
                client_socket.sendall(command.encode())
            else:
                break

        client_socket.close()
    except Exception as e:
        print("Error:", str(e))

def main():
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind(('', PORT))
    server_socket.listen(12346)
    print("Server is listening for connections...")

    while True:
        client_socket, client_address = server_socket.accept()
        client_thread = Thread(target=receiveCommand, args=(client_socket,))
        client_thread.start()


if __name__ == '__main__':
    main()