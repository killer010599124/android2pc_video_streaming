import socket
import struct
import cv2
import numpy as np
import threading

# Set up server socket
PORT = 12346
BUFFER_SIZE = 4096

server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
print('Socket created')

try:
    HOST = '192.168.130.145'
    server_socket.bind((HOST, PORT))
except socket.error as err:
    print('Bind failed. Error Code: {}'.format(err))
    exit()

# Listen for incoming connections
server_socket.listen(1)
print(f"Server listening on {HOST}:{PORT}...")

def receive_images(client_socket):
    while True:
        # Receive the image data size
        data_size = client_socket.recv(4)
        if not data_size:
            break

        # Unpack the image data size
        image_size = struct.unpack('!i', data_size)[0]

        # Receive the image data
        image_data = b""
        # print(image_data)
        while len(image_data) < image_size:
            remaining_size = image_size - len(image_data)
            chunk_size = min(BUFFER_SIZE, remaining_size)
            chunk = client_socket.recv(chunk_size)
            if not chunk:
                break
            image_data += chunk

        # Convert the received image data to a NumPy array
        image_array = np.frombuffer(image_data, dtype=np.uint8)
        # print(image_array)
        # Decode the image array
        image = cv2.imdecode(image_array, cv2.IMREAD_COLOR)

       
        # Display the received image in a separate thread
        display_thread = threading.Thread(target=display_image, args=(image,))
        display_thread.start()

def display_image(image):
    cv2.imshow("Received Image", image)
    cv2.waitKey(1)
    cv2.destroyAllWindows()

def send_messages(client_socket):
    while True:
        message = input("Enter message: ")
        if message == "":
            break
        client_socket.send(bytes(message + "\r\n", 'UTF-8'))
        print("Message sent")

while True:
# Accept a client connection
    client_socket, client_address = server_socket.accept()
    print(f"Connected to client: {client_address}")

    # Create two threads for receiving images and sending messages
    receive_thread = threading.Thread(target=receive_images, args=(client_socket,))
    send_thread = threading.Thread(target=send_messages, args=(client_socket,))

    # Start the threads
    receive_thread.start()
    send_thread.start()

    # Wait for the threads to finish
    receive_thread.join()
    send_thread.join()

    # Close the client socket
    client_socket.close()
    # server_socket.close()
    # Close the server socket
