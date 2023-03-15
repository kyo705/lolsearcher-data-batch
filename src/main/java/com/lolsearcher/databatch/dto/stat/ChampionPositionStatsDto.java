package com.lolsearcher.databatch.dto.stat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class ChampionPositionStatsDto implements ChampionStatsDto {

    private final String gameVersion;
    private final int queueId;
    private final int championId;
    private final int positionId;
    private final long wins;
    private final long losses;
    private final long bans;
}
