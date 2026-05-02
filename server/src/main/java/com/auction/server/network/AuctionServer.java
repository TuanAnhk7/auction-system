package com.auction.server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AuctionServer {
    private static final int PORT = 8080; // Cổng kết nối mà Server sẽ lắng nghe

    public static void main(String[] args) {
        // Tạo ServerSocket lắng nghe ở PORT 8080
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("====== HỆ THỐNG ĐẤU GIÁ (SERVER) ======");
            System.out.println("Server đang chạy và lắng nghe trên port " + PORT + "...");

            // Vòng lặp vô hạn để đón nhiều khách (Multi-client)
            while (true) {
                // Code sẽ dừng ở dòng này để chờ cho đến khi có 1 Client kết nối vào
                Socket clientSocket = serverSocket.accept();
                System.out.println(">> Có Client mới kết nối từ địa chỉ: " + clientSocket.getInetAddress());

                // Giao Client này cho một luồng (Thread) riêng biệt xử lý
                ClientHandler handler = new ClientHandler(clientSocket);
                Thread thread = new Thread(handler);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Lỗi khởi động Server: " + e.getMessage());
        }
    }
}