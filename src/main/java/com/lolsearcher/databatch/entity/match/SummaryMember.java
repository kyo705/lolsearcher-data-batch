package com.lolsearcher.databatch.entity.match;

import lombok.Data;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;

@Data
@Entity
@Table(indexes = {@Index(columnList = "summonerId")})
public class SummaryMember {

    @Id
    private Long id;
    private String summonerId;
    private int pickChampionId;
    private short positionId;
    private short championLevel; /* level : 1 ~ 18 */
    private short minionKills; /* lineMinionKills + NeutralMinionKills */
    private short kills;
    private short deaths;
    private short assists;
    private short item0;  /* item 리스트(item0 ~ item6)를 반정규화한 이유 : 아이템의 순서가 중요 */
    private short item1;
    private short item2;
    private short item3;
    private short item4;
    private short item5;
    private short item6;

    @JoinColumn(name = "teamId", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Team team;

    @BatchSize(size = 1000)
    @OneToOne(mappedBy = "summaryMember", fetch = FetchType.LAZY)
    private DetailMember detailMember;

    @JoinColumn(name = "perksId", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Perks perks;  /* 스펠, 룬 특성 */

    @JoinColumn(name = "perkStatsId", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private PerkStats perkStats;  /* 보조 스탯 */

    public void setTeam(Team team) throws IllegalAccessException {

        if(team.getMembers().size() >= 5){
            throw new IllegalAccessException("이미 연관관계 설정이 된 Team 객체입니다.");
        }
        this.team = team;
        team.getMembers().add(this);
    }
}
