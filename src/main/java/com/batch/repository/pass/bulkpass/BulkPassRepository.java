package com.batch.repository.pass.bulkpass;

import com.batch.entity.pass.bulkpass.BulkPassEntity;
import com.batch.entity.pass.bulkpass.BulkPassStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BulkPassRepository extends JpaRepository<BulkPassEntity,Integer> {

    List<BulkPassEntity> findByStatusAndStartedAtGreaterThan(BulkPassStatus status, LocalDateTime localDateTime);
}
