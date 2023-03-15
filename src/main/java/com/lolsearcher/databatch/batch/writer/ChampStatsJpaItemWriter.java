package com.lolsearcher.databatch.batch.writer;


import com.lolsearcher.databatch.dto.stat.ChampionStatsDto;
import com.lolsearcher.databatch.entity.match.Match;
import com.lolsearcher.databatch.service.stat.ChampStatsService;
import com.lolsearcher.databatch.service.stat.JpaChampStatsService;
import org.springframework.batch.item.database.JpaItemWriter;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.List;

public class ChampStatsJpaItemWriter extends JpaItemWriter<Match> {

    private List<JpaChampStatsService> champStatsServices;

    @Override
    public void write(List<? extends Match> items) {

        super.write(items);

        for(JpaChampStatsService champStatsService : champStatsServices){
            champStatsService.clearRepository();
        }
    }

    @Override
    protected void doWrite(EntityManager entityManager, List<? extends Match> items) {

        for(Match item : items){
            for(ChampStatsService champStatsService : champStatsServices){
                champStatsService.recordChampStats(item);
            }
        }

        for(JpaChampStatsService champStatsService : champStatsServices){
            List<ChampionStatsDto> championStats = champStatsService.extractChampStats();
            championStats.forEach(dto -> champStatsService.persistChampStats(entityManager, dto));
        }
    }

    public void setChampStatsService(JpaChampStatsService champStatsService) {
        this.champStatsServices.add(champStatsService);
    }

    public void setChampStatsService(Collection<JpaChampStatsService> champStatsServices) {
        this.champStatsServices.addAll(champStatsServices);
    }
}
