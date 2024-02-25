import socket
import threading
import cv2
import numpy

# Global variables for image display
image_display_lock = threading.Lock()
current_image = None

def receive_image_data():
    global current_image

    # Create a UDP socket
    udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    # Bind the socket to a specific IP address and port
    ip_address = '192.168.130.145'  # Use your desired IP address or '0.0.0.0' to bind to all available network interfaces
    port = 12346  # Use the same port number as in the Java code
    udp_socket.bind((ip_address, port))

    # Receive and process the image data in real-time
    buffer_size = 65536  # Adjust the buffer size as per your requirements
    while True:
        data, address = udp_socket.recvfrom(buffer_size)
        if not data:
            break
        
        # Process the received image data
        # Replace the following code with your own image processing logic
        image = cv2.imdecode(numpy.frombuffer(data, numpy.uint8), cv2.IMREAD_COLOR)

        # Acquire a lock to update the current image
        with image_display_lock:
            current_image = image.copy()

    # Close the socket
    udp_socket.close()

def display_image():
    global current_image

    while True:
        # Acquire a lock to read the current image
        with image_display_lock:
            if current_image is not None:
                cv2.imshow('Received Image', current_image)

        # Wait for the 'q' key to exit the image display
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    # Close the OpenCV windows
    cv2.destroyAllWindows()

# Create and start the image receiving thread
receive_thread = threading.Thread(target=receive_image_data)
receive_thread.start()

# Start the image display loop
display_image()

# Wait for the image receiving thread to finish
receive_thread.join()