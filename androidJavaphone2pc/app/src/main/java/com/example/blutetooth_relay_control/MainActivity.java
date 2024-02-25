package com.example.blutetooth_relay_control;



import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.Manifest.permission;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {




    private EditText ipAddress;
    private Button connectButton, captureButton;
    private String currentKeyboard;

    private boolean isSendingFrames = false; // Flag to control the frame sending process
    private int frameRate = 4;
    private TextView messageTextView;
    private FrameLayout colorFrame;

    private ArrowView arrowView;

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private ImageReader imageReader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.connectBtn:
                ReceiveCommandThread connectTask = new ReceiveCommandThread();
                connectTask.start();
                break;
            case R.id.captureBtn:
                openCamera();
                sendFrames();
//                sendCameraCapture();
                break;
            default: break;
        }
    }

    private void initView(){

        ipAddress = findViewById(R.id.ipEditText);
        messageTextView = findViewById(R.id.messageText);
        colorFrame = findViewById(R.id.ColorFrame);
        colorFrame.setVisibility(View.GONE);

        arrowView = findViewById(R.id.arrowView);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);


        String iptext = preferences.getString("IpAddress", "");
        if (!iptext.isEmpty()) {
            ipAddress.setText(iptext);
        }


        connectButton = findViewById(R.id.connectBtn);
        captureButton = findViewById(R.id.captureBtn);

        connectButton.setOnClickListener((View.OnClickListener) this);
        captureButton.setOnClickListener((View.OnClickListener) this);


    }

    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            if (ActivityCompat.checkSelfPermission(this, permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{ permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    createCameraPreviewSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    cameraDevice = null;
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceView surfaceView = findViewById(R.id.camera_preview);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.setFixedSize(640, 480);

            imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1);
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = reader.acquireLatestImage();
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    image.close();
                    sendCameraCapture(data);
                }
            }, null);

            Surface surface = surfaceHolder.getSurface();
            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    cameraCaptureSession = session;
                    try {
                        CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        captureRequestBuilder.addTarget(surface);
                        captureRequestBuilder.addTarget(imageReader.getSurface());

                        session.setRepeatingRequest(captureRequestBuilder.build(), null, null);

                        isSendingFrames = true;

                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(MainActivity.this, "Failed to configure camera", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void stopSendingFrames() {
        isSendingFrames = false;
    }
    private void sendFrames() {
        if (cameraDevice != null && isSendingFrames) {
            try {
                CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureRequestBuilder.addTarget(imageReader.getSurface());

                CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                        Toast.makeText(MainActivity.this, "Image captured", Toast.LENGTH_SHORT).show();

                        // Delay before capturing the next frame
                        try {
                            Thread.sleep(1000 / frameRate);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // Continue sending frames
//                        sendFrames();
                    }
                };

                cameraCaptureSession.capture(captureRequestBuilder.build(), captureCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendCameraCapture() {
        if (cameraDevice != null) {
            try {
                CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureRequestBuilder.addTarget(imageReader.getSurface());

                CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                        Toast.makeText(MainActivity.this, "Image captured", Toast.LENGTH_SHORT).show();
                    }
                };

                cameraCaptureSession.capture(captureRequestBuilder.build(), captureCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendCameraCapture(byte[] data) {

        String ip = ipAddress.getText().toString();
        SendImageThread sendImageTask = new SendImageThread(ip, data);
        sendImageTask.start();
    }

    private class SendImageThread extends Thread {
        private String ipAddress;
        private byte[] data;

        public SendImageThread(String ipAddress, byte[] data) {
            this.ipAddress = ipAddress;
            this.data = data;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(ipAddress, 12346);
                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                DataOutputStream dos = new DataOutputStream(bos);

                // Send the image data size
                int imageSize = data.length;
                dos.writeInt(imageSize);

                // Send the image data
                dos.write(data);
                dos.flush();// Close the streams and socket
                dos.close();
                bos.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ReceiveCommandThread extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        @Override
        public void run() {
            try {
                String ip = ipAddress.getText().toString();
                SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("IpAddress", ip);
                editor.apply();

                showToast(ip);

                socket = new Socket(ip, 12345);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String message;
                while ((message = in.readLine()) != null) {
                    publishProgress(message);

                    out.println("Try");
                    out.flush();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void publishProgress(String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    colorFrame.setVisibility(View.VISIBLE);
                    if (message.equals("red")) {
                        colorFrame.setBackgroundColor(Color.parseColor("#ff0000"));
                    } else if (message.equals("green")) {
                        colorFrame.setBackgroundColor(Color.parseColor("#00ff00"));
                    } else if (message.equals("blue")) {
                        colorFrame.setBackgroundColor(Color.parseColor("#0000ff"));
                    } else if (message.equals("white")) {
                        colorFrame.setBackgroundColor(Color.parseColor("#ffffff"));
                    } else if (message.equals("black")) {
                        colorFrame.setBackgroundColor(Color.parseColor("#000000"));
                    } else if (message.contains("arrow")) {

                        String pattern = "\\d+"; // Regular expression pattern to match one or more digits

                        Pattern compiledPattern = Pattern.compile(pattern);
                        Matcher matcher = compiledPattern.matcher(message);

                        if (matcher.find()) {
                            String numberString = matcher.group();
                            int number = Integer.parseInt(numberString);
                            arrowView.setSecondAngle(number);
//                            System.out.println("Extracted number: " + number);
                        }

                    }
                }
            });
        }
    }


    private class SendReceiveThread extends AsyncTask<byte[], String, Void> {
        private String ipAddress;
        private Socket socket;
        private PrintWriter out;
        private BufferedOutputStream bos;
        private DataOutputStream dos;
        private BufferedReader in;

        public SendReceiveThread(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        @Override
        protected Void doInBackground(byte[]... params) {
            byte[] data = params[0];

            try {
                socket = new Socket(ipAddress, 12346);
                out = new PrintWriter(socket.getOutputStream(), true);
                bos = new BufferedOutputStream(socket.getOutputStream());
                dos = new DataOutputStream(bos);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Send the image data size
                int imageSize = data.length;
                dos.writeInt(imageSize);
//                // Send the image data
                dos.write(data);
                dos.flush();

                bos.close();
                dos.close();

                socket.shutdownOutput();

                // Receive and process incoming commands
                String message;
                while ((message = in.readLine()) != null) {
                    publishProgress(message);
                    out.println("Try");
                    out.flush();
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(bos != null){
                    try {
                        bos.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(dos != null){
                    try{
                        dos.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (out != null) {

                    out.close();
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String message = values[0];
            colorFrame.setVisibility(View.VISIBLE);
            if (message.equals("red")) {
                colorFrame.setBackgroundColor(Color.parseColor("#ff0000"));
            } else if (message.equals("green")) {
                colorFrame.setBackgroundColor(Color.parseColor("#00ff00"));
            } else if (message.equals("blue")) {
                colorFrame.setBackgroundColor(Color.parseColor("#0000ff"));
            } else if (message.equals("white")) {
                colorFrame.setBackgroundColor(Color.parseColor("#ffffff"));
            } else if (message.equals("black")) {
                colorFrame.setBackgroundColor(Color.parseColor("#000000"));
            }
        }
    }


    @Override
    protected void onPause() {
        closeCamera();
        super.onPause();
    }

    private void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }




    private void receiveTextFromPC() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    String ip = ipAddress.getText().toString();
                    SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("IpAddress", ip);
                    editor.apply();
                    // Connect to the phone's IP address and port
                    Socket socket = new Socket(ip, 12346);

                    // Create input and output streams for sending and receiving data
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    // Send the text content
                    String textContent = "Hello, phone!";
                    out.println(textContent);

                    // Receive and process response from the server
                    String response;
                    while ((response = in.readLine()) != null) {
                        showToast("textContent");
                        // Print the response
                        System.out.println("Response from server: " + response);
                    }

                    // Close the streams and socket
                    out.close();
                    in.close();
                    socket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle any exceptions that may occur
                }
            }
        }).start(); }

    private void sendTextToPC(final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    showToast("1");

                    String ip = ipAddress.getText().toString();
                    SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("IpAddress", ip);
                    editor.apply();

                    Socket socket = new Socket(ip, 12346);
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    byte[] data = text.getBytes(StandardCharsets.UTF_8);
                    outputStream.write(data);
                    outputStream.flush();
                    outputStream.close();

                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

     public void showToast(final String Text){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,
                        Text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}