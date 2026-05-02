package com.auction.server.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// Implements Runnable để class này có thể chạy trên một Thread (luồng) riêng
public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // Constructor nhận vào socket của Client vừa kết nối
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Mở luồng để gửi và nhận dữ liệu (Object)
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Vòng lặp liên tục lắng nghe tin nhắn từ Client này
            while (true) {
                Object receivedData = in.readObject();
                System.out.println("[Client " + socket.getInetAddress() + "] gửi tin: " + receivedData.toString());

                // Phản hồi lại Client báo là đã nhận được
                out.writeObject("Server xác nhận đã nhận: " + receivedData.toString());
            }
        } catch (Exception e) {
            System.out.println(">> Client " + socket.getInetAddress() + " đã ngắt kết nối.");
        } finally {
            // Dọn dẹp đóng kết nối để giải phóng bộ nhớ
            try {
                if (socket != null) socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}