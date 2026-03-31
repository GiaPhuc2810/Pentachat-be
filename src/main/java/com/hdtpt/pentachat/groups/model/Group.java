package com.hdtpt.pentachat.groups.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import com.hdtpt.pentachat.util.BaseEntity;

/**
 * Thực thể Nhóm Chat (Group)
 */
@Entity
@Table(name = "chat_groups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long creatorId;

    /**
     * Danh sách ID của các thành viên trong nhóm
     * Thêm FetchType.EAGER để tránh lỗi LazyInitialization khi chuyển đổi sang JSON
     */
    @ElementCollection(fetch = FetchType.EAGER) // SỬA Ở ĐÂY
    @CollectionTable(name = "group_members", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "user_id")
    private List<Long> memberIds;
}