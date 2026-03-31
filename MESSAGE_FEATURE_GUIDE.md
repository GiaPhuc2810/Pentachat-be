# Tài Liệu Chức Năng 3. Nhắn Tin & Trò Chuyện (Message)

## 1. Mục tiêu chức năng

Chức năng `Message` dùng để hỗ trợ nhắn tin cá nhân 1-1 theo 2 cách song song:

- `REST API` để gửi, lấy dữ liệu, đánh dấu đã đọc, xóa tin nhắn
- `WebSocket/STOMP` để cập nhật thời gian thực khi có tin nhắn mới

Ý tưởng cốt lõi:

- `REST` đảm nhiệm phần `lưu trữ + truy vấn + thao tác dữ liệu`
- `WebSocket` đảm nhiệm phần `realtime UI`, giúp người nhận thấy tin nhắn mới ngay mà không cần refresh

---

## 2. Phạm vi chức năng 1-1 hiện có

Các chức năng chính đã có trong code:

1. Gửi tin nhắn qua `REST API`
   - `POST /api/messages/send`

2. Gửi/broadcast tin nhắn qua `WebSocket`
   - client gửi vào `/app/chat.send`
   - server có thể broadcast qua `/topic/messages`

3. Lấy danh sách inbox của một user
   - `GET /api/messages/inbox/{userId}`

4. Lấy lịch sử trò chuyện giữa 2 user
   - `GET /api/messages/conversation/{userId1}/{userId2}`

5. Đánh dấu tin nhắn đã đọc
   - `POST /api/messages/read/{userId}/{messageId}`

6. Xóa tin nhắn
   - `DELETE /api/messages/{userId}/{messageId}`

7. Kiểm tra trạng thái online
   - `GET /api/messages/status/{userId}`

---

## 3. Các file quan trọng

### Backend REST

- [MessageController.java](/d:/Clone%20git/Chat/Pentachat-be/src/main/java/com/hdtpt/pentachat/message/controller/MessageController.java)
  Vai trò:
  nhận request từ frontend và trả về `ApiResponse`

- [MessageService.java](/d:/Clone%20git/Chat/Pentachat-be/src/main/java/com/hdtpt/pentachat/message/service/MessageService.java)
  Vai trò:
  xử lý logic gửi/lấy/xóa/đọc tin nhắn và push realtime qua WebSocket

- [MessageRepository.java](/d:/Clone%20git/Chat/Pentachat-be/src/main/java/com/hdtpt/pentachat/message/repository/MessageRepository.java)
  Vai trò:
  truy vấn dữ liệu message từ database

- [Message.java](/d:/Clone%20git/Chat/Pentachat-be/src/main/java/com/hdtpt/pentachat/message/model/Message.java)
  Vai trò:
  entity đại diện cho bản ghi tin nhắn trong DB

- [MessageRequest.java](/d:/Clone%20git/Chat/Pentachat-be/src/main/java/com/hdtpt/pentachat/message/dto/request/MessageRequest.java)
  Vai trò:
  DTO đầu vào khi gửi tin nhắn

- [MessageResponse.java](/d:/Clone%20git/Chat/Pentachat-be/src/main/java/com/hdtpt/pentachat/message/dto/response/MessageResponse.java)
  Vai trò:
  DTO đầu ra cho frontend

### Backend WebSocket

- [WebSocketConfig.java](/d:/Clone%20git/Chat/Pentachat-be/src/main/java/com/hdtpt/pentachat/websocket/WebSocketConfig.java)
  Vai trò:
  cấu hình endpoint `/ws`, broker `/topic`, `/queue`, prefix `/app`, `/user`

- [WebSocketController.java](/d:/Clone%20git/Chat/Pentachat-be/src/main/java/com/hdtpt/pentachat/websocket/WebSocketController.java)
  Vai trò:
  nhận message STOMP từ client ở `/app/chat.send` và broadcast tới `/topic/messages`

### Thành phần liên quan

- `SessionManager`
  dùng để kiểm tra user có online hay không

- `SimpMessagingTemplate`
  dùng để server chủ động đẩy tin nhắn realtime ra topic WebSocket

---

## 4. Kiến trúc tổng quan

```text
Frontend
  |-- REST API ----------------------> MessageController
  |                                     |
  |                                     v
  |                                 MessageService
  |                                     |
  |                                     v
  |                                 MessageRepository
  |                                     |
  |                                     v
  |                                   Database
  |
  |-- WebSocket STOMP (/ws) --------> WebSocketConfig / WebSocketController
                                        |
                                        v
                                SimpMessagingTemplate
                                        |
                                        v
                             /topic/messages/{userId}
```

---

## 5. Luồng xử lý chính

## 5.1. Luồng gửi tin nhắn cá nhân qua REST

### Endpoint

`POST /api/messages/send`

### Input mẫu

```json
{
  "from": 1,
  "to": 2,
  "content": "Chao ban"
}
```

### Luồng xử lý

1. Frontend gọi `POST /api/messages/send`
2. `MessageController.sendMessage()` nhận request
3. Controller gọi `messageService.pushToUser(from, to, content)`
4. `MessageService` validate dữ liệu
5. Tạo `Message` mới và lưu vào DB qua `messageRepository.save(...)`
6. Convert entity sang `MessageResponse`
7. Gọi `notifyUserNewMessage(toUserId, response)`
8. Gọi tiếp `notifyUserNewMessage(fromUserId, response)`
9. `SimpMessagingTemplate` push message ra:
   - `/topic/messages/{recipientId}`
   - `/topic/messages/{senderId}`
10. Frontend hai phía nhận được update realtime nếu đang subscribe đúng topic

### Ý nghĩa nghiệp vụ

- Tin nhắn được lưu trước, rồi mới push realtime
- Điều này giúp:
  - không mất dữ liệu nếu frontend reload
  - conversation vẫn lấy lại được từ DB
  - realtime chỉ là lớp cập nhật giao diện

---

## 5.2. Luồng lấy inbox

### Endpoint

`GET /api/messages/inbox/{userId}`

### Luồng xử lý

1. Frontend truyền `userId`
2. Controller gọi `messageService.getUserInbox(userId)`
3. Service gọi `messageRepository.findByToUserId(userId)`
4. Service convert danh sách `Message` sang `MessageResponse`
5. Trả dữ liệu về frontend

### Mục đích

Inbox dùng để hiển thị:

- các tin nhắn mà user đang nhận được
- danh sách hội thoại mới nhất phía người nhận

Lưu ý:

- với code hiện tại, inbox đang lấy theo `toUserId`
- nghĩa là đây nghiêng về danh sách tin nhắn nhận được, chưa phải conversation list tối ưu kiểu Messenger/Zalo

---

## 5.3. Luồng lấy conversation giữa 2 user

### Endpoint

`GET /api/messages/conversation/{userId1}/{userId2}`

### Luồng xử lý

1. Frontend chọn 2 user đang chat
2. Controller gọi `messageService.getConversation(userId1, userId2)`
3. Service truy vấn 2 chiều:
   - `user1 -> user2`
   - `user2 -> user1`
4. Kết quả được convert sang `MessageResponse`
5. Frontend render thành lịch sử chat

### Ý nghĩa

Đây là API quan trọng nhất để dựng khung chat 1-1.

Nó trả về:

- ai gửi
- ai nhận
- nội dung
- thời gian tạo
- trạng thái đã đọc

---

## 5.4. Luồng đánh dấu đã đọc

### Endpoint

`POST /api/messages/read/{userId}/{messageId}`

### Luồng xử lý

1. Frontend gọi API khi user mở đoạn chat hoặc đọc tin nhắn
2. Service tìm message theo `messageId`
3. Service kiểm tra:
   - message có tồn tại không
   - `toUserId` có đúng là `userId` không
4. Nếu hợp lệ:
   - set `isRead = true`
   - save lại DB

### Ý nghĩa bảo vệ dữ liệu

Chỉ người nhận mới được đánh dấu tin nhắn là đã đọc.

---

## 5.5. Luồng xóa tin nhắn

### Endpoint

`DELETE /api/messages/{userId}/{messageId}`

### Luồng xử lý

1. Frontend gửi `userId` và `messageId`
2. Service tìm message
3. Service kiểm tra user hiện tại có phải:
   - người gửi
   - hoặc người nhận
4. Nếu đúng thì cho phép xóa
5. Nếu không đúng thì từ chối

### Ý nghĩa bảo vệ dữ liệu

Không cho user lạ xóa tin nhắn không thuộc về họ.

---

## 5.6. Luồng kiểm tra trạng thái online

### Endpoint

`GET /api/messages/status/{userId}`

### Luồng xử lý

1. Controller gọi `SessionManager.isUserOnline(userId)`
2. Nếu online:
   - trả về `SessionInfo`
3. Nếu offline:
   - trả về chuỗi `"User is offline"`

### Ý nghĩa

API này có thể dùng để:

- hiển thị chấm xanh online/offline
- kiểm tra khả năng nhận update tức thời
- hỗ trợ UX trước khi chat/call

---

## 5.7. Luồng WebSocket

### Cấu hình

Trong [WebSocketConfig.java](/d:/Clone%20git/Chat/Pentachat-be/src/main/java/com/hdtpt/pentachat/websocket/WebSocketConfig.java):

- endpoint kết nối:
  - `/ws`

- application destination prefix:
  - `/app`

- broker prefix:
  - `/topic`
  - `/queue`

- user destination prefix:
  - `/user`

### Kênh đang dùng trong code

1. WebSocket controller broadcast chung:
   - client gửi vào `/app/chat.send`
   - server broadcast ra `/topic/messages`

2. MessageService push riêng theo user:
   - `/topic/messages/{userId}`

### Ý nghĩa thực tế

Trong phần Message hiện tại, luồng realtime quan trọng hơn đang nằm ở `MessageService`:

- khi lưu thành công message
- server dùng `messagingTemplate.convertAndSend("/topic/messages/" + userId, message)`

Điều này phù hợp hơn với chat 1-1, vì mỗi user có topic riêng.

---

## 6. Lý thuyết cơ bản về công nghệ sử dụng

## 6.1. REST API là gì

`REST API` là cách client gọi HTTP request đến server để thao tác dữ liệu.

Ví dụ trong module Message:

- `POST` để gửi tin nhắn
- `GET` để lấy inbox, conversation, status
- `DELETE` để xóa tin nhắn

### Ưu điểm

- dễ debug bằng Postman
- chuẩn, dễ mở rộng
- phù hợp với thao tác đọc/ghi dữ liệu bền vững

### Nhược điểm

- không realtime theo bản chất
- muốn cập nhật liên tục phải polling

---

## 6.2. WebSocket là gì

`WebSocket` là giao thức giữ kết nối hai chiều liên tục giữa client và server.

Khác với REST:

- REST: client phải chủ động gọi lại
- WebSocket: server có thể chủ động đẩy dữ liệu xuống client

### Ứng dụng trong chat

Khi có tin nhắn mới:

- server push ngay tới người nhận
- giao diện cập nhật tức thì
- không cần refresh

---

## 6.3. STOMP là gì

`STOMP` là giao thức nhắn tin chạy trên WebSocket, giúp tổ chức message theo kiểu:

- `send`
- `subscribe`
- `topic`
- `queue`

Nó giúp code chat dễ quản lý hơn raw WebSocket.

Trong dự án này:

- client `send` tới `/app/chat.send`
- client `subscribe` tới `/topic/messages` hoặc `/topic/messages/{userId}`

---

## 6.4. SockJS là gì

`SockJS` là fallback cho WebSocket.

Nếu môi trường nào đó không hỗ trợ WebSocket tốt, SockJS giúp client vẫn kết nối được bằng cơ chế thay thế.

Trong dự án:

- endpoint `/ws` được đăng ký với `.withSockJS()`

---

## 6.5. SimpMessagingTemplate là gì

`SimpMessagingTemplate` là công cụ của Spring để server chủ động gửi message tới topic WebSocket.

Ví dụ thực tế trong [MessageService.java](/d:/Clone%20git/Chat/Pentachat-be/src/main/java/com/hdtpt/pentachat/message/service/MessageService.java):

```java
messagingTemplate.convertAndSend("/topic/messages/" + userId, message);
```

Ý nghĩa:

- sau khi lưu DB xong
- server đẩy luôn message sang client đang subscribe

---

## 6.6. JPA Repository là gì

`JPA Repository` là lớp giúp truy vấn DB theo entity.

Trong dự án:

- `MessageRepository`

Nó giúp:

- save message
- tìm inbox
- tìm conversation
- tìm theo group

---

## 7. Kịch bản demo để thuyết trình

## 7.1. Kịch bản ngắn gọn 1-1

### Bước demo

1. User A đăng nhập
2. User B đăng nhập
3. A gửi tin nhắn cho B bằng `POST /api/messages/send`
4. Server lưu tin nhắn vào DB
5. Server push realtime qua WebSocket tới topic của B
6. B thấy tin nhắn hiện ngay
7. B mở conversation giữa A và B
8. B gọi API đọc tin nhắn
9. B gọi API đánh dấu đã đọc
10. A hoặc B có thể xóa một tin nhắn nếu muốn

### Câu nói mẫu khi demo

> Ở đây em đang dùng REST để đảm bảo dữ liệu được lưu bền vững trong database, còn WebSocket để giao diện cập nhật ngay khi có tin nhắn mới. Hai lớp này bổ trợ cho nhau: REST để lưu và truy xuất, WebSocket để realtime.

---

## 7.2. Kịch bản nói về luồng gửi tin nhắn

> Khi người dùng bấm gửi, frontend gọi `POST /api/messages/send`. Backend vào `MessageController`, sau đó `MessageService` validate dữ liệu, tạo entity `Message` và lưu xuống database. Sau khi lưu thành công, backend dùng `SimpMessagingTemplate` đẩy tin nhắn tới topic của người nhận và cả người gửi để cả hai phía cập nhật giao diện theo thời gian thực.

---

## 7.3. Kịch bản nói về conversation

> Khi người dùng mở cửa sổ chat với một người khác, frontend gọi API conversation để lấy toàn bộ lịch sử nhắn tin hai chiều. Backend truy vấn cả chiều A sang B và B sang A, sau đó convert về `MessageResponse` để frontend render thành danh sách chat.

---

## 7.4. Kịch bản nói về trạng thái đã đọc

> Khi người nhận mở đoạn chat hoặc xem tin nhắn, frontend gọi API đánh dấu đã đọc. Backend chỉ cho phép đúng người nhận thực hiện thao tác này, tránh trường hợp user khác sửa trạng thái đọc của message không thuộc về mình.

---

## 7.5. Kịch bản nói về online status

> Hệ thống dùng `SessionManager` để biết user có đang online hay không. API status giúp frontend hiển thị trạng thái online/offline và cũng hỗ trợ giải thích vì sao một số realtime event có thể không tới ngay nếu user đã offline.

---

## 8. Điểm mạnh của thiết kế hiện tại

- Có đủ CRUD cơ bản cho chat 1-1
- Có realtime qua WebSocket
- Có bảo vệ quyền đánh dấu đọc và xóa tin nhắn
- Có tách lớp tương đối rõ:
  - controller
  - service
  - repository
  - DTO
  - websocket config

---

## 9. Hạn chế hiện tại cần biết khi trình bày

Đây là phần nên nói thật khi demo hoặc bảo vệ:

1. Endpoint `sendMessage` hiện chưa kiểm tra chặt chẽ session/auth ở mức controller
   - đang tin vào `from`, `to` từ request body
   - nếu muốn production-ready nên gắn user hiện tại từ session/token

2. Có 2 hướng WebSocket cùng tồn tại:
   - `/app/chat.send -> /topic/messages`
   - `MessageService -> /topic/messages/{userId}`
   Nên khi trình bày cần nói rõ:
   - luồng realtime thực tế cho 1-1 nên ưu tiên `topic theo user`

3. Inbox hiện lấy theo `toUserId`
   - phù hợp kiểu hộp thư nhận
   - chưa phải conversation summary tối ưu như sản phẩm chat lớn

4. Chưa thấy pagination cho conversation/inbox
   - nếu dữ liệu lớn sẽ ảnh hưởng hiệu năng

5. Chưa thấy cơ chế message delivery state sâu hơn
   - ví dụ `sent`, `delivered`, `seen`
   - hiện tại mới có `isRead`

---

## 10. Đề xuất nâng cấp nếu cần nói thêm

Nếu muốn nói về hướng phát triển tiếp:

1. Gắn xác thực thật cho API Message
2. Chuẩn hóa WebSocket topic riêng theo user
3. Thêm pagination cho inbox và conversation
4. Thêm trạng thái:
   - sent
   - delivered
   - seen
5. Thêm typing indicator
6. Thêm recall/edit message
7. Thêm search trong conversation

---

## 11. Mẫu kết luận để nói

> Chức năng Message của hệ thống được xây dựng theo mô hình kết hợp giữa REST và WebSocket. REST phụ trách lưu trữ và truy vấn dữ liệu, còn WebSocket phụ trách realtime. Nhờ đó hệ thống vừa đảm bảo tin nhắn được lưu bền vững trong database, vừa mang lại trải nghiệm chat tức thời cho người dùng.

