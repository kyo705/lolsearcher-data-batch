package com.lolsearcher.databatch.constant.enumeration;

import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum GamePosition {

    NONE("NONE", 0),
    TOP("TOP", 1),
    JUNGLE("JUNGLE", 2),
    MIDDLE("MIDDLE", 3),
    BOTTOM("BOTTOM", 4),
    UTILITY("UTILITY", 5);


    private final String name;
    private final int code;

    GamePosition(String name, int code){
        this.name = name;
        this.code = code;
    }

    private static final Map<Integer, GamePosition> BY_CODE =
            Stream.of(values()).collect(Collectors.toMap(GamePosition::getCode, e -> e));

    public static GamePosition valueOfCode(int code){
        return BY_CODE.get(code);
    }
}
