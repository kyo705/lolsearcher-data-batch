package com.lolsearcher.databatch.entity.match;

import lombok.Data;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static com.lolsearcher.databatch.constant.LolSearcherConstants.THE_NUMBER_OF_TEAM_MEMBER;

@Data
@Entity
public class Team {

    @Id
    private Long id;
    @Column(scale = 3)
    private byte gameResult;    /*  0 : win,  1 : loss,  2 : draw  */
    private short teamPositionId; /* 100 : red, 200 : blue */

    @ManyToOne
    @JoinColumn(name = "matchId", referencedColumnName = "id")
    private Match matchId;

    @BatchSize(size = 200)
    @OneToMany(mappedBy = "team", fetch = FetchType.EAGER)
    private List<SummaryMember> members = new ArrayList<>(THE_NUMBER_OF_TEAM_MEMBER);

    public void setMatchId(Match matchId) throws IllegalAccessException {

        if(matchId.getTeams().size() >= 2){
            throw new IllegalAccessException("이미 연관관계 설정이 된 Match 객체입니다.");
        }
        this.matchId = matchId;
        matchId.getTeams().add(this);
    }
}
