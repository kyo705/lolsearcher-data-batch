package com.lolsearcher.databatch.entity.champion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;

import static com.lolsearcher.databatch.constant.LolSearcherConstants.MAX_CHAMPION_COUNT;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(indexes = {@Index(columnList = "champBanStatsId, positionId", unique = true)})
public class ChampPositionStats {

	@Min(value = 0, message = "유효하지 않는 id 값입니다.")
	@GeneratedValue
	@Id
	private long id;
	@JoinColumn(name = "champBanStatsId", referencedColumnName = "id")
	@ManyToOne
	private ChampBanStats champBanStatsId;
	private int positionId;
	private long wins;
	private long losses;

	@BatchSize(size = MAX_CHAMPION_COUNT)
	@OneToMany(mappedBy = "champPositionStatsId", fetch = FetchType.EAGER)
	private List<ChampItemStats> champItemStats = new ArrayList<>();

	@BatchSize(size = MAX_CHAMPION_COUNT)
	@OneToMany(mappedBy = "champPositionStatsId", fetch = FetchType.EAGER)
	private List<ChampEnemyStats> champEnemyStats = new ArrayList<>();

	public void setChampBanStatsId(ChampBanStats champBanStats){
		if(this.champBanStatsId != null){
			throw new IllegalArgumentException("이미 매핑된 champPositionStatsId 값이 있습니다.");
		}
		for(ChampPositionStats champPositionStats : champBanStats.getChampPositionStats()){
			if(champPositionStats.getPositionId() == this.positionId){
				throw new IllegalArgumentException("ChampBanStats 에 이미 현재 positionId 값이 존재합니다.");
			}
		}
		this.champBanStatsId = champBanStats;
		this.champBanStatsId.getChampPositionStats().add(this);
	}
}
