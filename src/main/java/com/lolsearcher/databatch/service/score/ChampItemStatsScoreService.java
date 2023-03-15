package com.lolsearcher.databatch.service.score;

import com.lolsearcher.databatch.dto.stat.ChampionItemStatsDto;
import com.lolsearcher.databatch.dto.stat.ChampionStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.lolsearcher.databatch.constant.LolSearcherConstants.REDIS_CHAMP_ITEM_STAT_KEY;

@RequiredArgsConstructor
@Service
public class ChampItemStatsScoreService implements ChampStatsScoreService {

    private final RedisTemplate<String, ChampionItemStatsDto> championItemStatsRedisTemplate;

    @Override
    public void saveChampionStats(ChampionStatsDto championStatsDto) {

        if(!(championStatsDto instanceof ChampionItemStatsDto)){
            throw new IllegalArgumentException("현재 파라미터는 ChampionPositionStatsDto 타입이 아닙니다.");
        }
        ChampionItemStatsDto dto = (ChampionItemStatsDto) championStatsDto;

        long wins = dto.getWins();
        long losses = dto.getLosses();
        double score = 1.1*wins + 0.9*losses;

        championItemStatsRedisTemplate.opsForZSet().removeRange(REDIS_CHAMP_ITEM_STAT_KEY, 0, -1);
        championItemStatsRedisTemplate.opsForZSet().add(REDIS_CHAMP_ITEM_STAT_KEY, dto, score);
    }
}
