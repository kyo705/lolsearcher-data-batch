package com.lolsearcher.databatch.entity.champion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;

@Builder
@AllArgsConstructor
@Data
@Entity
@Table(indexes = {@Index(columnList = "champPositionStatsId, itemId")})
public class ChampItemStats {

	@GeneratedValue
	@Id
	private long id;
	@JoinColumn(name = "champPositionStatsId", referencedColumnName = "id")
	@ManyToOne
	private ChampPositionStats champPositionStatsId;
	private int itemId;
	private long wins;
	private long losses;

	public void setChampPositionStatsId(ChampPositionStats champPositionStats){
		if(this.champPositionStatsId != null){
			throw new IllegalArgumentException("이미 매핑된 champPositionStatsId 값이 있습니다.");
		}
		for(ChampItemStats champItemStats : champPositionStats.getChampItemStats()){
			if(champItemStats.getItemId() == this.itemId){
				throw new IllegalArgumentException("ChampPositionStats 객체에 이미 현재 itemId 값이 존재합니다.");
			}
		}
		champPositionStatsId = champPositionStats;
		champPositionStatsId.getChampItemStats().add(this);
	}
}
