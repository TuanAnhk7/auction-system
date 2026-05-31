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
import java.time.format.DateTimeFormatter;

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
                } else if (receivedData instanceof AutoBidRequest request) {
                    handleAutoBidRequest(request);
                } else if (receivedData instanceof BalanceUpdateRequest request) {
                    handleBalanceUpdateRequest(request);
                } else if (receivedData instanceof AdminAuctionActionRequest request) {
                    handleAdminAuctionActionRequest(request);
                } else if (receivedData instanceof CreateAuctionRequest request) {
                    handleCreateAuctionRequest(request);
                } else if (receivedData instanceof UpdateItemRequest request) {
                    handleUpdateItemRequest(request);
                } else if (receivedData instanceof DeleteAuctionRequest dar) {
                    handleDeleteItemRequest(new DeleteItemRequest(dar.getAuctionId()));
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

    public synchronized void send(Object response) {
        try {
            if (out != null) {
                out.writeObject(response);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            System.err.println("Khong gui duoc goi tin den client.");
        }
    }

    private void handleLoginRequest(LoginRequest request) {
        UserAccount user = userManager.authenticate(request.getUsername(), request.getPassword());
        if (user == null) {
            send(new LoginResponse(false, "Sai tài khoản hoặc mật khẩu.", null));
            return;
        }

        authenticatedUser = user;
        send(new LoginResponse(true, "Đăng nhập thành công.", user.getRole(), user.getAccountBalance()));
    }

    private void handleRegisterRequest(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()
                || request.getRole() == null) {
            send(new RegisterResponse(false, "Thông đăng ký không hợp lệ."));
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
            ensureAuthenticated();
            UserAccount user = authenticatedUser;
            Auction auction = auctionManager.findById(request.getAuctionId())
                    .orElseThrow(() -> new InvalidBidException("Không tìm thấy phiên đấu giá."));
            LocalDateTime previousEndTime = auction.getEndTime();

            auctionManager.placeBid(request.getAuctionId(), user.getUsername(), request.getBidAmount());
            refreshAuthenticatedUserBalance();
            Auction updatedAuction = auctionManager.findById(request.getAuctionId())
                    .orElseThrow(() -> new InvalidBidException("Không tìm thấy phiên đấu giá sau khi cập nhật."));

            broadcastAuctionExtensionIfNeeded(updatedAuction, previousEndTime);
            server.persistAuctionSnapshot(updatedAuction);
            server.persistAuctionBidHistory(updatedAuction);

            server.broadcast(new BidResponse(
                    true,
                    String.format("%s đã đặt giá %.2f USD", user.getUsername(), request.getBidAmount()),
                    toAuctionView(updatedAuction),
                    user.getUsername(),
                    authenticatedUser.getAccountBalance()
            ));
        } catch (AuctionException e) {
            send(new BidResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            send(new BidResponse(false, "Lỗi hệ thống khi đặt giá: " + e.getMessage(), null));
            e.printStackTrace();
        }
    }

    private void handleAutoBidRequest(AutoBidRequest request) {
        try {
            ensureAuthenticated();
            UserAccount user = authenticatedUser;
            Auction auction = auctionManager.findById(request.getAuctionId())
                    .orElseThrow(() -> new InvalidBidException("Không tìm thấy phiên đấu giá."));
            LocalDateTime previousEndTime = auction.getEndTime();
            double previousHighestBid = auction.getCurrentHighestBid();

            auctionManager.registerAutoBid(request.getAuctionId(), user.getUsername(), request.getMaxBid(), request.getIncrement());

            Auction updatedAuction = auctionManager.findById(request.getAuctionId())
                    .orElseThrow(() -> new InvalidBidException("Không tìm thấy phiên đấu giá."));

            server.persistAuctionSnapshot(updatedAuction);
            server.persistAuctionBidHistory(updatedAuction);
            send(new AutoBidResponse(true, "Đăng ký tự động thầu thành công."));
            broadcastAuctionExtensionIfNeeded(updatedAuction, previousEndTime);
            if (Double.compare(updatedAuction.getCurrentHighestBid(), previousHighestBid) != 0) {
                server.broadcast(new BidResponse(
                        true,
                        String.format("%s đã đặt giá tự động %.2f USD", user.getUsername(), updatedAuction.getCurrentHighestBid()),
                        toAuctionView(updatedAuction),
                        "Hệ thống"
                ));
            }
        } catch (AuctionException e) {
            send(new AutoBidResponse(false, e.getMessage()));
        } catch (Exception e) {
            send(new AutoBidResponse(false, "Lỗi hệ thống khi đăng ký tự động thầu: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private void handleBalanceUpdateRequest(BalanceUpdateRequest request) {
        try {
            ensureAuthenticated();
            if (request.getAmount() <= 0) {
                throw new AuctionException("Số tiền nạp phải lớn hơn 0.");
            }

            userManager.updateAccountBalance(authenticatedUser.getUsername(), request.getAmount());
            refreshAuthenticatedUserBalance();
            send(new BalanceUpdateResponse(
                    true,
                    String.format("Đã nạp thêm %.2f USD vào tài khoản.", request.getAmount()),
                    authenticatedUser.getAccountBalance()
            ));
        } catch (AuctionException e) {
            send(new BalanceUpdateResponse(
                    false,
                    e.getMessage(),
                    authenticatedUser != null ? authenticatedUser.getAccountBalance() : 0.0
            ));
        } catch (Exception e) {
            send(new BalanceUpdateResponse(
                    false,
                    "Lỗi hệ thống khi nạp tiền: " + e.getMessage(),
                    authenticatedUser != null ? authenticatedUser.getAccountBalance() : 0.0
            ));
            e.printStackTrace();
        }
    }

    private void broadcastAuctionExtensionIfNeeded(Auction updatedAuction, LocalDateTime previousEndTime) {
        if (updatedAuction == null || previousEndTime == null) {
            return;
        }

        LocalDateTime newEndTime = updatedAuction.getEndTime();
        if (newEndTime == null || !newEndTime.isAfter(previousEndTime)) {
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        server.broadcast(new AuctionExtendedResponse(
                true,
                String.format("Phiên đấu giá '%s' đã được gia hạn đến %s.",
                        updatedAuction.getItem().getName(),
                        newEndTime.format(formatter)),
                updatedAuction.getId(),
                newEndTime,
                toAuctionView(updatedAuction)
        ));
    }

    private void handleAuctionListRequest() {
        try {
            ensureAuthenticated();
            send(buildAuctionListResponse());
        } catch (AuctionException e) {
            System.err.println("Khong gui duoc danh sach dau gia hien tai.");
        }
    }

    private void refreshAuthenticatedUserBalance() {
        if (authenticatedUser == null) {
            return;
        }

        UserAccount latestAccount = userManager.findByUsername(authenticatedUser.getUsername());
        if (latestAccount != null) {
            authenticatedUser.setAccountBalance(latestAccount.getAccountBalance());
        }
    }

    private void handleCreateAuctionRequest(CreateAuctionRequest request) {
        try {
            UserAccount user = requireRole(Role.SELLER);
            LocalDateTime startTime = request.getStartTime();
            LocalDateTime endTime = startTime.plusMinutes(request.getDurationMinutes());

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
            server.persistAuctionSnapshot(auction);
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
            Auction auctionToUpdate = auctionManager.findById(request.getAuctionId())
                    .orElseThrow(() -> new AuctionException("Không tìm thấy phiên đấu giá để cập nhật."));

            if (!auctionToUpdate.getSellerUsername().equals(user.getUsername())) {
                throw new AuctionException("Bạn không có quyền sửa phiên đấu giá này.");
            }

            auctionManager.updateItem(
                    request.getAuctionId(),
                    request.getNewName(),
                    request.getNewPrice(),
                    request.getNewDescription()
            );

            server.persistAuctionSnapshot(auctionToUpdate);
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
            Auction auctionToDelete = auctionManager.findById(request.getAuctionId())
                    .orElseThrow(() -> new AuctionException("Không tìm thấy phiên đấu giá để xóa."));

            if (!auctionToDelete.getSellerUsername().equals(user.getUsername())) {
                throw new AuctionException("Bạn không có quyền xóa phiên đấu giá này.");
            }

            server.deleteAuctionSnapshot(auctionToDelete);
            auctionManager.removeAuction(request.getAuctionId());
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
            Auction auctionToUpdate = auctionManager.findById(request.getAuctionId())
                    .orElseThrow(() -> new AuctionException("Không tìm thấy phiên đấu giá."));

            if (!auctionToUpdate.getSellerUsername().equals(user.getUsername())) {
                throw new AuctionException("Bạn không có quyền thay đổi trạng thái phiên đấu giá này.");
            }

            auctionManager.updateAuctionStatus(request.getAuctionId(), request.getNewStatus().name());
            server.persistAuctionSnapshot(auctionToUpdate);
            server.persistAuctionBidHistory(auctionToUpdate);
            server.broadcastAuctionList(buildAuctionListResponse());
        } catch (AuctionException e) {
        }
    }

    private void handleAdminAuctionActionRequest(AdminAuctionActionRequest request) {
        try {
            requireRole(Role.ADMIN);
            Auction auction = auctionManager.updateAuctionStatus(request.getAuctionId(), request.getAction());
            AuctionView updatedAuction = toAuctionView(auction);
            server.persistAuctionSnapshot(auction);
            server.persistAuctionBidHistory(auction);
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
        if (creatorName == null || creatorName.isBlank()) creatorName = auction.getSellerUsername();
        if (creatorName == null || creatorName.isBlank()) creatorName = "Không rõ";

        java.util.List<String> historyList = new java.util.ArrayList<>(
                auction.getBidHistory().stream()
                        .map(this::formatBidHistory)
                        .toList()
        );

        String statusStr = auction.getStatus().name();
        if ("FINISHED".equalsIgnoreCase(statusStr)) {
            if (auction.getHighestBidder() != null && auction.getHighestBidder().getUsername() != null) {
                historyList.add(String.format("🏆 [HỆ THỐNG] Phiên đấu giá kết thúc! Người chiến thắng: %s với mức giá %.2f USD", auction.getHighestBidder().getUsername(), auction.getCurrentHighestBid()));
            } else {
                historyList.add("❌ [HỆ THỐNG] Phiên đấu giá kết thúc mà không có người tham gia đặt giá.");
            }
        } else if ("CANCELED".equalsIgnoreCase(statusStr)) {
            historyList.add("🚫 [HỆ THỐNG] Phiên đấu giá này đã bị hủy bỏ bởi Ban quản trị.");
        }

        return new AuctionView(
                auction.getId(), item.getId(), item.getName(), item.getDescription(),
                creatorName, auction.getSellerUsername(), item.getCategory(),
                item.getStartingPrice(), item.getCurrentPrice(),
                auction.getHighestBidder() == null ? null : auction.getHighestBidder().getUsername(),
                auction.getEndTime(), statusStr, historyList
        );
    }

    private String formatBidHistory(BidTransaction transaction) {
        return String.format("[%s] %s đặt %.2f USD", transaction.getCreatedAt(), transaction.getBidderUsername(), transaction.getAmount());
    }

    public synchronized void sendAuctionList(GetAuctionListResponse response) {
        send(response);
    }
}
