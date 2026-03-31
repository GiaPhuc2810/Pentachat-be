package com.hdtpt.pentachat.groups.service;

import com.hdtpt.pentachat.groups.repository.GroupRepository;
import com.hdtpt.pentachat.groups.model.Group;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    /**
     * Tạo một nhóm chat mới
     */
    public Group createGroup(String name, Long creatorId, List<Long> memberIds) {
        // 1. Validate dữ liệu đầu vào
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Tên nhóm không được để trống");
        }

        // 2. Đảm bảo người tạo luôn có tên trong danh sách thành viên
        List<Long> finalMembers = new ArrayList<>(memberIds != null ? memberIds : new ArrayList<>());
        if (!finalMembers.contains(creatorId)) {
            finalMembers.add(creatorId);
        }

        // 3. Khởi tạo đối tượng Group
        Group newGroup = Group.builder()
                .name(name)
                .creatorId(creatorId)
                .memberIds(finalMembers)
                .build();

        log.info("Đang tạo nhóm mới: {} bởi user {}", name, creatorId);

        // 4. LƯU VÀO DATABASE
        return groupRepository.save(newGroup);
    }

    /**
     * Lấy danh sách các nhóm mà user tham gia
     */
    public List<Group> getUserGroups(Long userId) {
        log.info("Đang lấy danh sách nhóm cho user: {}", userId);

        // 5. Lấy dữ liệu từ Repo
        return groupRepository.findByMemberId(userId);
    }

    /**
     * Save or update a group (from JpaDataApiImpl)
     * 
     * @param group Group entity to save
     * @return Saved group entity
     */
    public Group saveGroup(Group group) {
        return groupRepository.save(group);
    }

    public Group findGroupById(Long groupId) {
        return groupRepository.findById(groupId).orElse(null);
    }

    public List<Group> findGroupsByUserId(Long userId) {
        return groupRepository.findByMemberId(userId);
    }
}