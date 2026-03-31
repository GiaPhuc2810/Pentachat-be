package com.hdtpt.pentachat.groups.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.hdtpt.pentachat.groups.model.Group;
import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    // Truy vấn này quét trong bảng phụ group_members cực nhanh và chính xác
    @Query("SELECT g FROM Group g WHERE :userId MEMBER OF g.memberIds")
    List<Group> findByMemberId(@Param("userId") Long userId);
}