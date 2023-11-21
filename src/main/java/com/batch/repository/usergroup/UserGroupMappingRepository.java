package com.batch.repository.usergroup;

import com.batch.entity.usergroup.UserGroupMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserGroupMappingRepository extends JpaRepository<UserGroupMappingEntity,Integer> {
    List<UserGroupMappingEntity> findByUserGroupId(String usrGroupId);
}
