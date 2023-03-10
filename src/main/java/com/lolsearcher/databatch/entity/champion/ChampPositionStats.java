package com.lolsearcher.databatch.entity.champion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;

@Builder
@AllArgsConstructor
@Data
@Entity
@Table(indexes = {@Index(columnList = "queueId, gameVersion, positionId")})
public class ChampPositionStats {

	@Id
	private long id;
	private String gameVersion;
	private int queueId;
	private int positionId;
	private int championId;
	private long wins;
	private long losses;
	private long bans;

}
