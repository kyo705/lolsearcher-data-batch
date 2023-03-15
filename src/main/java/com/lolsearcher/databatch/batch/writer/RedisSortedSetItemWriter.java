package com.lolsearcher.databatch.batch.writer;

import com.lolsearcher.databatch.dto.stat.ChampionEnemyStatsDto;
import com.lolsearcher.databatch.dto.stat.ChampionItemStatsDto;
import com.lolsearcher.databatch.dto.stat.ChampionPositionStatsDto;
import com.lolsearcher.databatch.dto.stat.ChampionStatsDto;
import com.lolsearcher.databatch.entity.champion.ChampBanStats;
import com.lolsearcher.databatch.service.score.ChampStatsScoreService;
import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.List;

public class RedisSortedSetItemWriter implements ItemWriter<ChampBanStats> {

    private ChampStatsScoreService scoreService;

    @Override
    public void write(List<? extends ChampBanStats> items) {

        List<ChampionStatsDto> championStatsDtos = getAllChampionStats(items);

        championStatsDtos.forEach(dto-> scoreService.saveChampionStats(dto));
    }

    private List<ChampionStatsDto> getAllChampionStats(List<? extends ChampBanStats> champBanStats) {

        List<ChampionStatsDto> allChampionStats = new ArrayList<>();

        champBanStats.forEach(banStat ->
                        banStat.getChampPositionStats()
                                .forEach(positionStat -> {
                                    ChampionPositionStatsDto statDto = ChampionPositionStatsDto.builder()
                                        .gameVersion(banStat.getGameVersion())
                                        .queueId(banStat.getQueueId())
                                        .championId(banStat.getChampionId())
                                        .positionId(positionStat.getPositionId())
                                        .wins(positionStat.getWins())
                                        .losses(positionStat.getLosses())
                                        .bans(banStat.getBans())
                                        .build();

                                allChampionStats.add(statDto);

                                positionStat.getChampItemStats().forEach(itemStat -> {
                                    ChampionItemStatsDto itemStatDto = ChampionItemStatsDto.builder()
                                            .gameVersion(banStat.getGameVersion())
                                            .queueId(banStat.getQueueId())
                                            .championId(banStat.getChampionId())
                                            .positionId(positionStat.getPositionId())
                                            .itemId(itemStat.getItemId())
                                            .wins(positionStat.getWins())
                                            .losses(positionStat.getLosses())
                                            .build();

                                    allChampionStats.add(itemStatDto);
                                });

                                positionStat.getChampEnemyStats().forEach(enemyStat -> {
                                    ChampionEnemyStatsDto enemyStatDto = ChampionEnemyStatsDto.builder()
                                            .gameVersion(banStat.getGameVersion())
                                            .queueId(banStat.getQueueId())
                                            .championId(banStat.getChampionId())
                                            .positionId(positionStat.getPositionId())
                                            .enemyChampionId(enemyStat.getEnemyChampionId())
                                            .wins(positionStat.getWins())
                                            .losses(positionStat.getLosses())
                                            .build();

                                    allChampionStats.add(enemyStatDto);
                                });
                            })
        );

        return allChampionStats;
    }

    public void setScoreService(ChampStatsScoreService scoreService){
        this.scoreService = scoreService;
    }
}
