package com.lolsearcher.databatch.service.score;

import com.lolsearcher.databatch.dto.stat.ChampionStatsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompositeChampStatsScoreService implements ChampStatsScoreService {

    private final List<ChampStatsScoreService> champStatsScoreServices;

    public void saveChampionStats(ChampionStatsDto championStatsDto){

        for(ChampStatsScoreService service : champStatsScoreServices){
            try{
                service.saveChampionStats(championStatsDto);
                return;
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        String message = String.format("입력 파라미터 타입 : %s 에 적절한 ChampStatsScoreService가 없음", championStatsDto.getClass());
        log.error(message);
        throw new IllegalArgumentException(message);
    }
}
