package com.lolsearcher.databatch.entity.match;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class PerkStats {
    @Id
    private Integer id;

    private Short defense;
    private Short flex;
    private Short offense;

}
