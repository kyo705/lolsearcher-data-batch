package com.lolsearcher.databatch.entity.champion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;

@Builder
@AllArgsConstructor
@Data
@Entity
@Table(indexes = {@Index(columnList = "champPositionStatsId, enemyChampionId")})
public class ChampEnemyStats {

	@Id
	private long id;
	private int enemyChampionId;
	private long wins;
	private long losses;

	@JoinColumn(name = "champPositionStatsId", referencedColumnName = "id")
	@ManyToOne
	private ChampPositionStats champPositionStatsId;

	public void setChampPositionStatsId(ChampPositionStats champPositionStats){
		if(this.champPositionStatsId != null){
			throw new IllegalArgumentException("이미 매핑된 champPositionStatsId 값이 있습니다.");
		}
		champPositionStatsId = champPositionStats;
	}
}
