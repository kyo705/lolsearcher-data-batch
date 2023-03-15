package com.lolsearcher.databatch.service.stat;

import com.lolsearcher.databatch.dto.stat.ChampionStatsDto;
import com.lolsearcher.databatch.entity.match.Match;

import java.util.List;

public interface ChampStatsService {

    void recordChampStats(Match match);

    List<ChampionStatsDto> extractChampStats();

    void clearRepository();
}
