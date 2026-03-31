package com.hdtpt.pentachat.groups.service;

import com.hdtpt.pentachat.groups.model.Group;
import com.hdtpt.pentachat.groups.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("Group Service Tests")
class GroupServiceTest {

    @Autowired
    private GroupService groupService;

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private GroupRepository groupRepository;

    @BeforeEach
    void setUp() {
        groupRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create group successfully")
    void testCreateGroup() {
        List<Long> members = Arrays.asList(2L, 3L);
        Group group = groupService.createGroup("Test Group", 1L, members);

        assertNotNull(group);
        assertEquals("Test Group", group.getName());
        assertEquals(1L, group.getCreatorId());
        assertTrue(group.getMemberIds().contains(1L));
        assertTrue(group.getMemberIds().contains(2L));
        assertTrue(group.getMemberIds().contains(3L));
    }

    @Test
    @DisplayName("Should find groups user belongs to")
    void testGetUserGroups() {
        groupService.createGroup("Group A", 1L, Arrays.asList(2L));
        groupService.createGroup("Group B", 2L, Arrays.asList(1L));
        groupService.createGroup("Group C", 3L, Arrays.asList(4L));

        List<Group> user1Groups = groupService.getUserGroups(1L);
        assertEquals(2, user1Groups.size());
    }
}
