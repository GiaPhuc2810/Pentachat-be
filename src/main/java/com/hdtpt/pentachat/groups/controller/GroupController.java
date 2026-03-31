package com.hdtpt.pentachat.groups.controller;

import com.hdtpt.pentachat.dto.response.ApiResponse;
import com.hdtpt.pentachat.exception.AppException;
import com.hdtpt.pentachat.groups.model.Group;
import com.hdtpt.pentachat.groups.service.GroupService;
import com.hdtpt.pentachat.security.SessionManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/my") // PHẢI CÓ DÒNG NÀY
    public ResponseEntity<ApiResponse> getMyGroups(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Session-Id") String sessionId) {

        // Xác thực session
        SessionManager.SessionInfo sessionInfo = SessionManager.getUserSession(userId);
        if (sessionInfo == null || !sessionInfo.sessionId.equals(sessionId)) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid session.");
        }

        List<Group> myGroups = groupService.getUserGroups(userId);
        return ResponseEntity.ok(ApiResponse.builder().success(true).data(myGroups).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createGroup(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Session-Id") String sessionId,
            @RequestBody CreateGroupRequest request) {

        // Xác thực session
        SessionManager.SessionInfo sessionInfo = SessionManager.getUserSession(userId);
        if (sessionInfo == null || !sessionInfo.sessionId.equals(sessionId)) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid session.");
        }

        Group newGroup = groupService.createGroup(request.getName(), userId, request.getMemberIds());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builder().success(true).message("Thành công!").data(newGroup).build());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateGroupRequest {
        private String name;
        private List<Long> memberIds;
    }
}