package com.lolsearcher.databatch.dto.stat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class ChampionEnemyStatsDto implements ChampionStatsDto {

    private final String gameVersion;
    private final int queueId;
    private final int championId;
    private final int positionId;
    private final int enemyChampionId;
    private final long wins;
    private final long losses;
}
