package com.batch.job.pass.addPasses;

import com.batch.entity.pass.PassEntity;
import com.batch.entity.pass.PassModelMapper;
import com.batch.entity.pass.bulkpass.BulkPassEntity;
import com.batch.entity.pass.bulkpass.BulkPassStatus;
import com.batch.entity.usergroup.UserGroupMappingEntity;
import com.batch.repository.pass.PassRepository;
import com.batch.repository.pass.bulkpass.BulkPassRepository;
import com.batch.repository.usergroup.UserGroupMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AddPassesTasklet implements Tasklet {
    private final PassRepository passRepository;
    private final BulkPassRepository bulkPassRepository;
    private final UserGroupMappingRepository userGroupMappingRepository;

    public AddPassesTasklet(PassRepository passRepository, BulkPassRepository bulkPassRepository, UserGroupMappingRepository userGroupMappingRepository) {
        this.passRepository = passRepository;
        this.bulkPassRepository = bulkPassRepository;
        this.userGroupMappingRepository = userGroupMappingRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final LocalDateTime startedAt =LocalDateTime.now().minusDays(1);
        //어제날짜
        final List<BulkPassEntity> bulkPassEntities = bulkPassRepository.findByStatusAndStartedAtGreaterThan(BulkPassStatus.READY, startedAt);

        int count = 0;
        for (BulkPassEntity bulkPassEntity : bulkPassEntities) {
            List<String> userIds = userGroupMappingRepository.findByUserGroupId(bulkPassEntity.getUserGroupId())
                    .stream().map(UserGroupMappingEntity::getUserId).collect(Collectors.toList());

            count += addPasses(bulkPassEntity, userIds);

            bulkPassEntity.setStatus(BulkPassStatus.EXPIRED);
        }
        log.info("AddPassesTasklet - execute: 이용권 {}건 추가 완료, startedAt={}",count, startedAt);
        return RepeatStatus.FINISHED;
    }

    private int addPasses(BulkPassEntity bulkPassEntity, List<String> userIds) {
        List<PassEntity> passEntities = new ArrayList<>();
        for (String userId : userIds) {
            PassEntity passEntity = PassModelMapper.INSTANCE.toPassEntity(bulkPassEntity, userId);
            passEntities.add(passEntity);
        }

        return passRepository.saveAll(passEntities).size();
    }
}
