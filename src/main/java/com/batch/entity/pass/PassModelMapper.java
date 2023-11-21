package com.batch.entity.pass;

import com.batch.entity.pass.bulkpass.BulkPassEntity;
import com.batch.entity.pass.bulkpass.BulkPassStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

// ReportingPolicy.IGNORE: 일치하지 않은 필드를 무시합니다.
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PassModelMapper {
    //매퍼 인스턴스 뽑아 쓸수있다 ㅎㅎ
    PassModelMapper INSTANCE = Mappers.getMapper(PassModelMapper.class);

    @Mapping(target = "status", qualifiedByName = "defaultStatus")
    @Mapping(target = "remainingCount", source = "bulkPassEntity.count")
    PassEntity toPassEntity(BulkPassEntity bulkPassEntity, String userId);

    // BulkPassStatus와 관계 없이 PassStatus값을 설정합니다.
    @Named("defaultStatus")
    default PassStatus status(BulkPassStatus status) {
        return PassStatus.READY;
    }

}