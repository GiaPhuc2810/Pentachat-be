# Tóm Tắt Chức Năng Message

## 1. Mục tiêu

Chức năng `Message` dùng để nhắn tin cá nhân 1-1, kết hợp:

- `REST API` để lưu và truy vấn dữ liệu
- `WebSocket` để cập nhật realtime

---

## 2. Các chức năng chính

- Gửi tin nhắn:
  - `POST /api/messages/send`

- Lấy inbox:
  - `GET /api/messages/inbox/{userId}`

- Lấy lịch sử trò chuyện giữa 2 người:
  - `GET /api/messages/conversation/{userId1}/{userId2}`

- Đánh dấu đã đọc:
  - `POST /api/messages/read/{userId}/{messageId}`

- Xóa tin nhắn:
  - `DELETE /api/messages/{userId}/{messageId}`

- Kiểm tra online/offline:
  - `GET /api/messages/status/{userId}`

- WebSocket gửi realtime:
  - gửi vào `/app/chat.send`
  - subscribe `/topic/messages`
  - service đang push theo user qua `/topic/messages/{userId}`

---

## 3. File quan trọng

- [MessageController.java](/d:/Clone%20git/Chat/Pentachat-be/src/main/java/com/hdtpt/pentachat/message/controller/MessageController.java)
  nhận request REST

- [MessageService.java](/d:/Clone%20git/Chat/Pentachat-be/src/main/java/com/hdtpt/pentachat/message/service/MessageService.java)
  xử lý logic gửi, lấy, đọc, xóa, push realtime

- [MessageRepository.java](/d:/Clone%20git/Chat/Pentachat-be/src/main/java/com/hdtpt/pentachat/message/repository/MessageRepository.java)
  truy vấn database

- [WebSocketConfig.java](/d:/Clone%20git/Chat/Pentachat-be/src/main/java/com/hdtpt/pentachat/websocket/WebSocketConfig.java)
  cấu hình `/ws`, `/app`, `/topic`, `/queue`

- [WebSocketController.java](/d:/Clone%20git/Chat/Pentachat-be/src/main/java/com/hdtpt/pentachat/websocket/WebSocketController.java)
  nhận message STOMP từ client

---

## 4. Luồng xử lý ngắn gọn

### Gửi tin nhắn

1. Frontend gọi `POST /api/messages/send`
2. Controller nhận request
3. Service validate dữ liệu
4. Lưu `Message` vào database
5. Convert sang `MessageResponse`
6. Push realtime qua WebSocket cho:
   - người nhận
   - người gửi

### Xem hội thoại

1. Frontend gọi `GET /conversation/{userId1}/{userId2}`
2. Backend truy vấn tin nhắn 2 chiều
3. Trả về list để render khung chat

### Đánh dấu đã đọc

1. Frontend gọi `POST /read/{userId}/{messageId}`
2. Backend kiểm tra đúng người nhận
3. Set `isRead = true`

---

## 5. Công nghệ sử dụng

### REST API

Dùng cho:

- gửi dữ liệu
- lấy dữ liệu
- cập nhật trạng thái
- xóa dữ liệu

### WebSocket + STOMP

Dùng cho realtime:

- server chủ động đẩy tin nhắn mới xuống client
- không cần refresh

### SockJS

Fallback cho WebSocket khi môi trường không hỗ trợ tốt.

### SimpMessagingTemplate

Công cụ Spring dùng để push message realtime ra topic.

### JPA Repository

Dùng để lưu và truy vấn message từ database.

---

## 6. Cách nói ngắn khi thuyết trình

> Chức năng Message của hệ thống được xây dựng theo mô hình kết hợp giữa REST và WebSocket. REST dùng để lưu trữ, truy vấn và thao tác dữ liệu tin nhắn trong database. WebSocket dùng để cập nhật thời gian thực, giúp người nhận thấy tin nhắn mới ngay mà không cần tải lại trang.

> Khi người dùng gửi tin nhắn, backend sẽ lưu tin nhắn vào database trước, sau đó dùng WebSocket đẩy dữ liệu tới người gửi và người nhận. Khi người dùng mở đoạn chat, frontend gọi API conversation để lấy toàn bộ lịch sử nhắn tin hai chiều.

---

## 7. Điểm cần lưu ý

- Phần realtime 1-1 hiện dùng tốt nhất theo:
  - `/topic/messages/{userId}`

- Inbox hiện tại đang thiên về danh sách tin nhắn nhận được, chưa phải conversation summary tối ưu như app chat lớn.

- Hệ thống đã có:
  - gửi tin nhắn
  - xem hội thoại
  - đánh dấu đã đọc
  - xóa tin nhắn
  - kiểm tra online

---

## 8. File đầy đủ

Nếu cần bản chi tiết hơn, xem tại:

[MESSAGE_FEATURE_GUIDE.md](/d:/Clone%20git/Chat/Pentachat-be/MESSAGE_FEATURE_GUIDE.md)

