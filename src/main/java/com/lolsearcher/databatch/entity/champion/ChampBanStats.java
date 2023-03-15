package com.lolsearcher.databatch.entity.champion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static com.lolsearcher.databatch.constant.LolSearcherConstants.MAX_CHAMPION_COUNT;
import static com.lolsearcher.databatch.constant.LolSearcherConstants.THE_NUMBER_OF_GAME_POSITION;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(indexes = {@Index(columnList = "gameVersion, queueId, championId", unique = true)})
@Entity
public class ChampBanStats {

    @GeneratedValue
    @Id
    private long id;
    private String gameVersion;
    private int queueId;
    private int championId;
    private long bans;

    @BatchSize(size = MAX_CHAMPION_COUNT)
    @OneToMany(mappedBy = "champBanStatsId", fetch = FetchType.EAGER)
    private List<ChampPositionStats> champPositionStats = new ArrayList<>(THE_NUMBER_OF_GAME_POSITION);
}
