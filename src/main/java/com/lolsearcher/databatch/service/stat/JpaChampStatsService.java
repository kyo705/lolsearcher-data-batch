package com.lolsearcher.databatch.service.stat;

import com.lolsearcher.databatch.dto.stat.ChampionStatsDto;

import javax.persistence.EntityManager;

public interface JpaChampStatsService extends ChampStatsService {

    void persistChampStats(EntityManager em, ChampionStatsDto championStatsDto);
}
