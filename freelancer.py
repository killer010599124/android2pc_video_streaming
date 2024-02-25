import numpy as np
import cv2
import time


class webcam:
    def __init__(self):
        self.img_black       = np.full(shape=(int(400), int(300), 3), fill_value=000, dtype=np.uint8)
        self.img_white       = np.full(shape=(int(400), int(300), 3), fill_value=255, dtype=np.uint8)
        
        # Here I need to capture the camera iamge from my phone
        self.cam = cv2.VideoCapture(0)
        if self.cam is None or not self.cam.isOpened():
            print("no cam")
            exit(1)

    def capture(self):
        _, self.frame = self.cam.read()
        return self.frame
        
    def show(self):
        cv2.imshow( 'cam1', self.frame )
        if cv2.waitKey(10) == 27:
            exit
            
    def phone_display(self):
        # Here I need a command make the phone change collor
        if (int(time.time()) % 4 == 1):
            cv2.imshow("phone", self.img_black)
        else:
            cv2.imshow("phone", self.img_white)        
        
if __name__ == '__main__':
    mywebcam = webcam()
    while(True):
        mywebcam.capture()
        mywebcam.show()
        mywebcam.phone_display()
