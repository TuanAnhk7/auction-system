package auction.server.network;

import auction.common.exception.AuctionException;
import auction.common.exception.AuctionClosedException;
import auction.common.exception.InvalidBidException;
import auction.common.model.auction.Auction;
import auction.common.model.auction.AuctionManager;
import auction.common.model.auction.BidTransaction;
import auction.common.model.item.Art;
import auction.common.model.item.Item;
import auction.common.model.network.*;
import auction.common.model.user.Bidder;
import auction.server.auth.UserManager;
import auction.common.model.auction.AuctionStatus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final AuctionServer server;
    private final AuctionManager auctionManager;
    private final UserManager userManager = UserManager.getInstance();
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private UserAccount authenticatedUser;

    public ClientHandler(Socket socket, AuctionServer server, AuctionManager auctionManager) {
        this.socket = socket;
        this.server = server;
        this.auctionManager = auctionManager;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Object receivedData = in.readObject();
                if (receivedData instanceof LoginRequest request) {
                    handleLoginRequest(request);
                } else if (receivedData instanceof RegisterRequest request) {
                    handleRegisterRequest(request);
                } else if (receivedData instanceof BidRequest request) {
                    handleBidRequest(request);
                } else if (receivedData instanceof AdminAuctionActionRequest request) {
                    handleAdminAuctionActionRequest(request);
                } else if (receivedData instanceof CreateAuctionRequest request) {
                    handleCreateAuctionRequest(request);
                } else if (receivedData instanceof UpdateItemRequest request) {
                    handleUpdateItemRequest(request);
                } else if (receivedData instanceof DeleteItemRequest request) {
                    handleDeleteItemRequest(request);
                } else if (receivedData instanceof ChangeStatusRequest request) {
                    handleChangeStatusRequest(request);
                } else if (receivedData instanceof GetAuctionListRequest) {
                    handleAuctionListRequest();
                }
            }
        } catch (Exception e) {
            System.out.println(">> Client " + socket.getInetAddress() + " đã ngắt kết nối.");
        } finally {
            server.unregister(this);
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void send(BidResponse response) {
        try {
            if (out != null) {
                out.writeObject(response);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            System.err.println("Khong gui duoc goi tin realtime den client.");
        }
    }

    public synchronized void send(CreateAuctionResponse response) {
        try {
            if (out != null) {
                out.writeObject(response);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            System.err.println("Khong gui duoc ket qua tao phien den client.");
        }
    }

    public synchronized void send(AdminAuctionActionResponse response) {
        try {
            if (out != null) {
                out.writeObject(response);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            System.err.println("Khong gui duoc ket qua quan tri phien den client.");
        }
    }

    public synchronized void send(LoginResponse response) {
        try {
            if (out != null) {
                out.writeObject(response);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            System.err.println("Khong gui duoc ket qua dang nhap den client.");
        }
    }

    public synchronized void send(RegisterResponse response) {
        try {
            if (out != null) {
                out.writeObject(response);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            System.err.println("Khong gui duoc ket qua dang ky den client.");
        }
    }

    public synchronized void send(UpdateItemResponse response) {
        try {
            if (out != null) {
                out.writeObject(response);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            System.err.println("Khong gui duoc ket qua cap nhat san pham den client.");
        }
    }

    public synchronized void send(DeleteItemResponse response) {
        try {
            if (out != null) {
                out.writeObject(response);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            System.err.println("Khong gui duoc ket qua xoa san pham den client.");
        }
    }

    public synchronized void send(AuctionEndedResponse response) {
        try {
            if (out != null) {
                out.writeObject(response);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            System.err.println("Khong gui duoc thong bao ket thuc dau gia.");
        }
    }

    private void handleLoginRequest(LoginRequest request) {
        UserAccount user = userManager.authenticate(request.getUsername(), request.getPassword());
        if (user == null) {
            send(new LoginResponse(false, "Sai tài khoản hoặc mật khẩu.", null));
            return;
        }

        authenticatedUser = user;
        send(new LoginResponse(true, "Đăng nhập thành công.", user.getRole()));
    }

    private void handleRegisterRequest(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()
                || request.getRole() == null) {
            send(new RegisterResponse(false, "Thông tin đăng ký không hợp lệ."));
            return;
        }

        boolean created = userManager.register(
                request.getUsername().trim(),
                request.getPassword(),
                request.getRole()
        );

        if (!created) {
            send(new RegisterResponse(false, "Tên đăng nhập đã tồn tại."));
            return;
        }

        send(new RegisterResponse(true, "Đăng ký thành công. Bạn có thể đăng nhập ngay."));
    }

    private void handleBidRequest(BidRequest request) {
        try {
            UserAccount user = requireRole(Role.BIDDER);
            auctionManager.placeBidByItemId(request.getItemId(), user.getUsername(), request.getBidAmount());

            Auction updatedAuction = auctionManager.findByItemId(request.getItemId())
                    .orElseThrow(() -> new InvalidBidException("Khong tim thay phien dau gia sau khi cap nhat."))
                    ;

            server.broadcast(new BidResponse(
                    true,
                    "Dat gia thanh cong.",
                    toAuctionView(updatedAuction),
                    user.getUsername()
            ));
        } catch (AuctionException e) {
            send(new BidResponse(false, e.getMessage(), null));
        }
    }

    private void handleAuctionListRequest() {
        try {
            ensureAuthenticated();
            sendAuctionList(buildAuctionListResponse());
        } catch (AuctionException e) {
            System.err.println("Khong gui duoc danh sach dau gia hien tai.");
        }
    }

    private void handleCreateAuctionRequest(CreateAuctionRequest request) {
        try {
            UserAccount user = requireRole(Role.SELLER);
            LocalDateTime startTime = request.getStartTime();
            LocalDateTime endTime = startTime.plusMinutes(request.getDurationMinutes()); //Tính tgian kết thúc

            Auction auction = auctionManager.createAuction(
                    user.getUsername(),
                    request.getItemType(),
                    request.getItemName(),
                    request.getDescription(),
                    request.getStartingPrice(),
                    startTime,
                    endTime,
                    request.getSpecificProp1(),
                    request.getSpecificProp2()
            );
            if (auction == null) {
                send(new CreateAuctionResponse(false, "Loại vật phẩm chưa được hỗ trợ.", null));
                return;
            }
            AuctionView createdAuction = toAuctionView(auction);
            send(new CreateAuctionResponse(true, "Đăng ký đấu giá thành công. Phiên đang chờ Admin mở.", createdAuction));
            server.broadcastAuctionList(buildAuctionListResponse());
        } catch (Exception e) {
            send(new CreateAuctionResponse(false, "Không tạo được phiên đấu giá: " + e.getMessage(), null));
        }
    }

    private void handleUpdateItemRequest(UpdateItemRequest request) {
        try {
            UserAccount user = requireRole(Role.SELLER);
            Auction auctionToUpdate = auctionManager.findById(request.getItemId())
                    .orElseThrow(() -> new AuctionException("Không tìm thấy phiên đấu giá để cập nhật."));

            if (!auctionToUpdate.getSellerUsername().equals(user.getUsername())) {
                throw new AuctionException("Bạn không có quyền sửa phiên đấu giá này.");
            }

            auctionManager.updateItem(
                    request.getItemId(),
                    request.getNewName(),
                    request.getNewPrice(),
                    request.getNewDescription()
            );

            send(new UpdateItemResponse(true, "Cập nhật sản phẩm thành công.", null));
            server.broadcastAuctionList(buildAuctionListResponse());
        } catch (AuctionException e) {
            send(new UpdateItemResponse(false, "Không thể cập nhật sản phẩm: " + e.getMessage(), null));
        } catch (Exception e) {
            send(new UpdateItemResponse(false, "Lỗi không xác định khi cập nhật sản phẩm: " + e.getMessage(), null));
            e.printStackTrace();
        }
    }

    private void handleDeleteItemRequest(DeleteItemRequest request) {
        try {
            UserAccount user = requireRole(Role.SELLER);
            Auction auctionToDelete = auctionManager.findById(request.getItemId())
                    .orElseThrow(() -> new AuctionException("Không tìm thấy phiên đấu giá để xóa."));

            if (!auctionToDelete.getSellerUsername().equals(user.getUsername())) {
                throw new AuctionException("Bạn không có quyền xóa phiên đấu giá này.");
            }
            
            if (auctionToDelete.getStatus() != AuctionStatus.PENDING) {
                throw new AuctionException("Chỉ có thể xóa phiên đấu giá đang ở trạng thái PENDING.");
            }

            auctionManager.removeAuction(request.getItemId());
            send(new DeleteItemResponse(true, "Xóa sản phẩm thành công."));
            server.broadcastAuctionList(buildAuctionListResponse());
        } catch (AuctionException e) {
            send(new DeleteItemResponse(false, "Không thể xóa sản phẩm: " + e.getMessage()));
        } catch (Exception e) {
            send(new DeleteItemResponse(false, "Lỗi không xác định khi xóa sản phẩm: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private void handleChangeStatusRequest(ChangeStatusRequest request) {
        try {
            UserAccount user = requireRole(Role.SELLER);
            Auction auctionToUpdate = auctionManager.findById(request.getItemId())
                    .orElseThrow(() -> new AuctionException("Không tìm thấy phiên đấu giá."));

            if (!auctionToUpdate.getSellerUsername().equals(user.getUsername())) {
                throw new AuctionException("Bạn không có qu-yền thay đổi trạng thái phiên đấu giá này.");
            }

            auctionManager.updateAuctionStatus(request.getItemId(), request.getNewStatus().name());
            server.broadcastAuctionList(buildAuctionListResponse());
        } catch (AuctionException e) {
            // Gửi lại lỗi cho client
        }
    }

    private void handleAdminAuctionActionRequest(AdminAuctionActionRequest request) {
        try {
            requireRole(Role.ADMIN);
            Auction auction = auctionManager.updateAuctionStatus(request.getAuctionId(), request.getAction());
            AuctionView updatedAuction = toAuctionView(auction);
            send(new AdminAuctionActionResponse(true, "Cập nhật trạng thái thành công.", updatedAuction));
            server.broadcastAuctionList(buildAuctionListResponse());
        } catch (AuctionException e) {
            send(new AdminAuctionActionResponse(false, e.getMessage(), null));
        }
    }

    private void ensureAuthenticated() throws AuctionException {
        if (authenticatedUser == null) {
            throw new AuctionException("Bạn chưa đăng nhập.");
        }
    }

    private UserAccount requireRole(Role expectedRole) throws AuctionException {
        ensureAuthenticated();
        if (authenticatedUser.getRole() != expectedRole) {
            throw new AuctionException("Tài khoản của bạn không có quyền thực hiện thao tác này.");
        }
        return authenticatedUser;
    }

    private GetAuctionListResponse buildAuctionListResponse() {
        return new GetAuctionListResponse(
                auctionManager.getActiveAuctions().stream()
                        .map(this::toAuctionView)
                        .toList()
        );
    }

    private AuctionView toAuctionView(Auction auction) {
        Item item = auction.getItem();
        String creatorName = item.getDisplayCreator();
        if (creatorName == null || creatorName.isBlank()) {
            creatorName = auction.getSellerUsername();
        }
        if (creatorName == null || creatorName.isBlank()) {
            creatorName = "Không rõ";
        }

        return new AuctionView(
                auction.getId(),
                item.getId(),
                item.getName(),
                item.getDescription(),
                creatorName,
                auction.getSellerUsername(),
                item.getCategory(),
                item.getStartingPrice(),
                item.getCurrentPrice(),
                auction.getHighestBidder() == null ? null : auction.getHighestBidder().getUsername(),
                auction.getEndTime(),
                auction.getStatus().name(),
                auction.getBidHistory().stream()
                        .map(this::formatBidHistory)
                        .toList()
        );
    }

    private String formatBidHistory(BidTransaction transaction) {
        return String.format(
                "[%s] %s đặt %.2f USD",
                transaction.getCreatedAt(),
                transaction.getBidderUsername(),
                transaction.getAmount()
        );
    }

    public synchronized void sendAuctionList(GetAuctionListResponse response) {
        try {
            if (out != null) {
                out.writeObject(response);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            System.err.println("Khong gui duoc danh sach dau gia hien tai.");
        }
    }

    private synchronized void sendAuctionListLegacy(GetAuctionListResponse response) throws IOException {
        if (out != null) {
            out.writeObject(response);
            out.flush();
            out.reset();
        }
    }
}