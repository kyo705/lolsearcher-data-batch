package com.lolsearcher.databatch.entity.match;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(indexes = {@Index(columnList = "matchId", unique = true)})
@Entity
public class BannedChampion {

    @Id
    private long id;
    private int championId;

    @JoinColumn(name = "matchId", referencedColumnName = "id")
    @ManyToOne
    private Match matchId;

}
