import socket
import struct
import cv2
import numpy as np
import threading
from queue import Queue

# Define a queue to store received images
image_queue = Queue()

def capture_image(client_socket):
    # Receive image data continuously
    while True:
        # Receive the image data size
        data_size = client_socket.recv(4)
        if not data_size:
            break

        # Unpack the image data size
        image_size = struct.unpack('!i', data_size)[0]

        # Receive the image data
        image_data = b""
      
        while len(image_data) < image_size:
            remaining_size = image_size - len(image_data)
            chunk_size = min(4096, remaining_size)
            chunk = client_socket.recv(chunk_size)
            if not chunk:
                break
            image_data += chunk
        
        # Convert the received image data to a NumPy array
        image_array = np.frombuffer(image_data, dtype=np.uint8)

        image = cv2.imdecode(image_array, cv2.IMREAD_COLOR)

        # Add the image array to the queue
        image_queue.put(image_array)



    # Close the client socket when the loop breaks
    client_socket.close()

def send_messages(client_socket):
    while True:
        message = input("Enter message: ")
        if message == "":
            break
        client_socket.send(bytes(message + "\r\n", 'UTF-8'))
        print("Message sent")

def display_images():
    while True:
        # Check if there are images in the queue
        if not image_queue.empty():
            # Get the next image array from the queue
            image_array = image_queue.get()

            # Decode the image array
            image = cv2.imdecode(image_array, cv2.IMREAD_COLOR)

            # Display the image using OpenCV
            cv2.imshow("Received Image", image)

        # Check if the 'q' key is pressed
        if cv2.waitKey(1) == ord('q'):
            break

    # Close the OpenCV window and cleanup
    cv2.destroyAllWindows()

def start_server(host, port):
    # Create a socket object
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # Bind the socket to a specific address and port
    server_socket.bind((host, port))

    # Listen for incoming connections
    server_socket.listen(1)
    print(f"Server listening on {host}:{port}...")

    if port == 12346:
        display_thread = threading.Thread(target=display_images)
        display_thread.start()

    while True:
        # Accept a client connection
        client_socket, client_address = server_socket.accept()

        # Create a new thread to handle the client
        if port == 12346:
            capture_thread = threading.Thread(target=capture_image, args=(client_socket,))
            capture_thread.start()
        else:
            command_thread = threading.Thread(target=send_messages, args=(client_socket,))
            command_thread.start()

def run_servers():
    # Set up server sockets
    host = input("Enter IP address: ") #'192.168.130.145'  # your IP address or hostname
    port1 = 12346  # the same port number used in the Android app
    port2 = 12345

    # Create threads for each server socket
    thread1 = threading.Thread(target=start_server, args=(host, port1))
    thread2 = threading.Thread(target=start_server, args=(host, port2))

    # Start the threads
    thread1.start()
    thread2.start()

    # Wait for the threads to finish
    thread1.join()
    thread2.join()

run_servers()