package com.lolsearcher.databatch.constant.enumeration;

import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum GameResultStatus {

    WIN("wins", 0),
    LOSS("losses", 1),
    DRAW("draw", 2);

    private final String name;
    private final int code;

    GameResultStatus(String name, int code) {
        this.name = name;
        this.code = code;
    }

    private static final Map<Integer, GameResultStatus> BY_NUMBER =
            Stream.of(values()).collect(Collectors.toMap(GameResultStatus::getCode, e -> e));

    public static GameResultStatus valueOfCode(int code){
        return BY_NUMBER.get(code);
    }
}
