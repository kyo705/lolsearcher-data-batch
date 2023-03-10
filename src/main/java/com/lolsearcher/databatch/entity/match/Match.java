package com.lolsearcher.databatch.entity.match;

import lombok.Data;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static com.lolsearcher.databatch.constant.LolSearcherConstants.THE_NUMBER_OF_MATCH_MEMBER;
import static com.lolsearcher.databatch.constant.LolSearcherConstants.THE_NUMBER_OF_TEAM;

@Data
@Entity
@Table(name = "MATCHES", indexes = {@Index(columnList = "matchId", unique = true)})
public class Match {

	@Id
	private Long id;
	@Column(unique = true)
	private String matchId; /* REST API로 받아올 때 필요한 고유한 match id */
	private long gameDuration;
	private long gameEndTimestamp;
	private int queueId;
	private int seasonId;
	private String version;

	@BatchSize(size = 100)
	@OneToMany(mappedBy = "matchId", fetch = FetchType.EAGER)
	private List<Team> teams = new ArrayList<>(THE_NUMBER_OF_TEAM);

	@BatchSize(size = 100)
	@OneToMany(mappedBy = "matchId", fetch = FetchType.EAGER)
	private List<BannedChampion> bannedChampionIds = new ArrayList<>(THE_NUMBER_OF_MATCH_MEMBER);
}
