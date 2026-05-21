package network;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class AuctionClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // IP của Server. Nếu chạy chung 1 máy tính để test thì dùng 127.0.0.1
    private final String SERVER_ADDRESS = "127.0.0.1";
    private final int SERVER_PORT = 8080;

    // 1. Hàm khởi tạo kết nối
    public void connect() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println(">> Đã kết nối thành công tới Server!");

            // KHỞI TẠO LUỒNG GỬI (OUT) TRƯỚC LUỒNG NHẬN (IN) ĐỂ TRÁNH DEADLOCK
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Mở luồng lắng nghe ngầm
            startListening();

        } catch (IOException e) {
            System.err.println("Không thể kết nối tới Server: " + e.getMessage());
        }
    }

    // 2. Hàm thiết lập Luồng lắng nghe ngầm (Background Thread)
    private void startListening() {
        Thread listenThread = new Thread(() -> {
            try {
                while (true) {
                    Object receivedData = in.readObject();
                    System.out.println("[Server gửi về]: " + receivedData.toString());
                }
            } catch (Exception e) {
                System.out.println(">> Đã mất kết nối với Server.");
            } finally {
                disconnect();
            }
        });

        // Thiết lập Daemon để luồng tự chết khi tắt ứng dụng
        listenThread.setDaemon(true);
        listenThread.start();
    }

    // 3. Hàm gửi dữ liệu đi
    public void sendData(Object data) {
        try {
            if (out != null) {
                out.writeObject(data);
                out.flush(); // Đẩy dữ liệu đi ngay
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi gửi dữ liệu: " + e.getMessage());
        }
    }

    // 4. Dọn dẹp kết nối
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ======================================================
    // 5. HÀM MAIN ĐỂ CHẠY TEST THỬ NGHIỆM ĐỘC LẬP
    // ======================================================
    public static void main(String[] args) {
        System.out.println("=== BẮT ĐẦU TEST KHÁCH HÀNG (CLIENT) ===");
        AuctionClient testClient = new AuctionClient();

        testClient.connect();

        System.out.println("Đang gửi tin nhắn...");
        testClient.sendData("Xin chào Server, tôi là Thành viên 1 đang test mạng!");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        testClient.disconnect();
        System.out.println("=== KẾT THÚC BÀI TEST ===");
    }
}
