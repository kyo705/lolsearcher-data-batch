package com.lolsearcher.databatch.entity.match;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class Perks {

    @Id
    private Long id;

    private short mainPerkStyle;
    private short subPerkStyle;

    private short mainPerk1;
    private short mainPerk2;
    private short mainPerk3;
    private short mainPerk4;
    private short subPerk1;
    private short subPerk2;
}
