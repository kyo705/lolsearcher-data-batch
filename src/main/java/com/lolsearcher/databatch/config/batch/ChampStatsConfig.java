package com.lolsearcher.databatch.config.batch;

import com.lolsearcher.databatch.batch.writer.ChampStatsJpaItemWriter;
import com.lolsearcher.databatch.batch.writer.RedisSortedSetItemWriter;
import com.lolsearcher.databatch.entity.champion.ChampBanStats;
import com.lolsearcher.databatch.entity.match.Match;
import com.lolsearcher.databatch.service.score.CompositeChampStatsScoreService;
import com.lolsearcher.databatch.service.stat.JpaChampStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lolsearcher.databatch.constant.BatchConstants.*;

@RequiredArgsConstructor
@Configuration
public class ChampStatsConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final List<JpaChampStatsService> champStatsServices;
    private final CompositeChampStatsScoreService compositeChampStatsScoreService;

    @Bean
    public Job champStatsJob() {

        return jobBuilderFactory.get(CHAMP_STATS_JOB_NAME)
                .start(creatingChampStatsStep())
                .on(ExitStatus.STOPPED.getExitCode()).stopAndRestart(creatingChampStatsStep())
                .from(creatingChampStatsStep())
                .on("*").to(uploadingChampStatsStep())
                .end()
                .build();
    }

    /* 1단계 스텝 관련 Bean */

    @Bean
    public Step creatingChampStatsStep() {

        return stepBuilderFactory.get(CHAMP_STATS_CREATING_STEP_NAME)
                .<Match, Match>chunk(MATCH_CHUNK_SIZE)
                .reader(matchJpaPagingItemReader(null, null, null))
                .processor(validatingMatchItemProcessor())
                .writer(championStatsJpaItemWriter())
                .build();
    }

    @StepScope
    @Bean
    public ItemReader<Match> matchJpaPagingItemReader(
            @Value("#{jobParameters['gameVersion']}") String gameVersion,
            @Value("#{jobParameters['currentTimestamp']}") Long currentTimestamp,
            @Value("#{jobParameters['queueId']}") Long queueId
    ) {
        long startTimeStamp = currentTimestamp - INTERVAL_BATCH_TIME;
        long endTimeStamp = currentTimestamp;

        String query = "SELECT DISTINCT m FROM Match " +
                "WHERE m.gameEndTimeStamp BETWEEN :startTimeStamp AND :endTimeStamp " +
                "AND m.queueId = :queueId " +
                "AND m.version = :gameVersion";

        Map<String, Object> paramValues = new HashMap<>();
        paramValues.put("startTimeStamp", startTimeStamp);
        paramValues.put("endTimeStamp", endTimeStamp);
        paramValues.put("gameVersion", gameVersion);
        paramValues.put("queueId", queueId);

        return new JpaPagingItemReaderBuilder<Match>()
                .name(CHAMP_STATS_CREATING_STEP_READER_NAME)
                .entityManagerFactory(entityManagerFactory)
                .pageSize(MATCH_CHUNK_SIZE)
                .queryString(query)
                .parameterValues(paramValues)
                .build();
    }

    @Bean
    public ItemProcessor<Match, Match> validatingMatchItemProcessor() {

        return new BeanValidatingItemProcessor<>();
    }

    @Bean
    public ItemWriter<Match> championStatsJpaItemWriter() {

        ChampStatsJpaItemWriter champStatsJpaItemWriter = new ChampStatsJpaItemWriter();
        champStatsJpaItemWriter.setChampStatsService(champStatsServices);
        champStatsJpaItemWriter.setEntityManagerFactory(entityManagerFactory);

        return champStatsJpaItemWriter;
    }


    /* 2단계 스텝 관련 Bean */

    @Bean
    public Step uploadingChampStatsStep() {

        return stepBuilderFactory.get(CHAMP_STATS_UPLOADING_STEP_NAME)
                .<ChampBanStats, ChampBanStats>chunk(CHAMP_STATS_CHUNK_SIZE)
                .reader(champStatsItemReader(null, null))
                .processor(validatingChampStatsProcessor())
                .writer(redisSortedSetItemWriter())
                .build();
    }

    @StepScope
    @Bean
    public ItemReader<ChampBanStats> champStatsItemReader(
            @Value("#{jobParameters['gameVersion']}") String gameVersion,
            @Value("#{jobParameters['queueId']}") Long queueId
    ) {

        String query = "SELECT s FROM ChampBanStats s WHERE s.gameVersion = :gameVersion AND s.queueId = :queueId";

        Map<String, Object> paramValues = new HashMap<>();
        paramValues.put("gameVersion", gameVersion);
        paramValues.put("queueId", queueId);

        return new JpaPagingItemReaderBuilder<ChampBanStats>()
                .name(CHAMP_STATS_UPLOADING_STEP_READER_NAME)
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHAMP_STATS_CHUNK_SIZE)
                .queryString(query)
                .parameterValues(paramValues)
                .build();
    }

    @Bean
    public ItemProcessor<ChampBanStats, ChampBanStats> validatingChampStatsProcessor(){

        return new ValidatingItemProcessor<>();
    }

    @Bean
    public ItemWriter<ChampBanStats> redisSortedSetItemWriter() {

        RedisSortedSetItemWriter writer = new RedisSortedSetItemWriter();
        writer.setScoreService(compositeChampStatsScoreService);

        return writer;
    }

}
