import socket

PORT = 12345
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
print('Socket created')

try:
    HOST = '192.168.130.145' #input("Enter IP address: ")
    s.bind((HOST, PORT))
except socket.error as err:
    print('Bind failed. Error Code: {}'.format(err))
    exit()

s.listen(10)
print("Socket Listening")
conn, addr = s.accept()

while True:
    message = input("Enter message: ")  # Wait for user input
    if message == "":
        break  # Exit the loop if the user presses enter without entering a message
    conn.send(bytes(message + "\r\n", 'UTF-8'))
    print("Message sent")
    # data = conn.recv(1024)
    # print(data.decode(encoding='UTF-8'))