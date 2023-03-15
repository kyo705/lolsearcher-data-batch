package com.lolsearcher.databatch.entity.champion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(indexes = {@Index(columnList = "champPositionStatsId, enemyChampionId")})
public class ChampEnemyStats {

	@Id
	private long id;
	@JoinColumn(name = "champPositionStatsId", referencedColumnName = "id")
	@ManyToOne
	private ChampPositionStats champPositionStatsId;
	private int enemyChampionId;
	private long wins;
	private long losses;

	public void setChampPositionStatsId(ChampPositionStats champPositionStats){
		if(this.champPositionStatsId != null){
			throw new IllegalArgumentException("이미 매핑된 champPositionStatsId 값이 있습니다.");
		}
		for(ChampEnemyStats champEnemyStats : champPositionStats.getChampEnemyStats()){
			if(champEnemyStats.getEnemyChampionId() == this.enemyChampionId){
				throw new IllegalArgumentException("ChampPositionStats 객체에 이미 현재 enemyChampionId 값이 존재합니다.");
			}
		}
		champPositionStatsId = champPositionStats;
		champPositionStatsId.getChampEnemyStats().add(this);
	}
}
