package com.batch.job.notification;

import com.batch.entity.booking.BookingEntity;
import com.batch.entity.notification.NotificationEntity;
import com.batch.entity.notification.NotificationEvent;
import com.batch.entity.notification.NotificationModelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SendNotificationBeforeClassJobConfig {
    private final int CHUNK_SIZE = 10;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final SendNotificationItemWriter sendNotificationItemWriter;

    //step이 2개인 것
    @Bean
    public Job sendNotificationBeforeClassJob(){
        return jobBuilderFactory.get("sendNotificationBeforeClassJob")
                .start(addNotificationStep())
                .next(sendNotificationStep())
                .build();

    }
    //청크기반의 스탭 ->멀티쓰레드 스텝으로 변경할것임
    @Bean
    public Step addNotificationStep() {
        return stepBuilderFactory.get("addNotificationStep")
                .<BookingEntity, NotificationEntity>chunk(CHUNK_SIZE)
                .reader(addNotificationItemReader())
                .processor(addNotificationItemProcessor())
                .writer(addNotificationItemWriter())
                .build();
    }

     /*JpaPagingItemReader: JPA에서 사용하는 페이징 기법
     * 쿼리당 pageSize만큼 가져오며 다른 PagingItemReader 와 마찬가지로 Thread-safe
     * 조회대상의 업데이트가 필요하지않아서 커서 사용안하고 페이징 기법 사용함*/
    @Bean
    public JpaPagingItemReader<BookingEntity> addNotificationItemReader() {
        //상태(status)가 준비 중이며 , 시작일시가 10 분후 시작하는 예약이  알람의 대상이 된다.
        return new JpaPagingItemReaderBuilder<BookingEntity>()
                .name("addNotificationItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)//한번에 청크사이즈만큼의 로우를 가져온다.
                .queryString("select b from BookingEntity b join fetch b.userEntity " +
                        "where b.status :status and b.startedAt <=:startedAt order by b.bookingSqe")
                .build();
    }
    @Bean
    public ItemProcessor<BookingEntity,NotificationEntity> addNotificationItemProcessor() {
        return bookingEntity-> NotificationModelMapper.INSTANCE.toNotificationEntity(bookingEntity,NotificationEvent.BEFORE_CLASS);
    }

    @Bean
    public JpaItemWriter<NotificationEntity> addNotificationItemWriter() {
        return new JpaItemWriterBuilder<NotificationEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step sendNotificationStep() {
        return stepBuilderFactory.get("sendNotificationStep")
                .<NotificationEntity,NotificationEntity>chunk(CHUNK_SIZE)
                .reader(sendNotificationItemReader())
                .writer(sendNotificationItemWriter)
                .taskExecutor(new SimpleAsyncTaskExecutor())//해당스텡비 비동기로 작동하도록
                .build();
    }

    /*SynchronizedItemStreamReader 로깜싸주면 리더는 순차적으로 실행-> 프로세서,라이터 부분은 전달받은 thread-safe한 객체로 멀티쓰레드로 동작된다.*/
    private SynchronizedItemStreamReader<NotificationEntity> sendNotificationItemReader() {
        // 이벤트(event)가 수업 전이며, 발송여부(sent)가 false인 알람이 조회대상이다.
        JpaCursorItemReader<NotificationEntity> itemReader = new JpaCursorItemReaderBuilder<NotificationEntity>()
                .name("sendNotificationItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select n from NotificationEntity n where n.event = :event and n.sent=:sent")
                .parameterValues(Map.of("event", NotificationEvent.BEFORE_CLASS, "sent", false))
                .build();

        return new SynchronizedItemStreamReaderBuilder<NotificationEntity>()
                .delegate(itemReader)
                .build();
    }



}

