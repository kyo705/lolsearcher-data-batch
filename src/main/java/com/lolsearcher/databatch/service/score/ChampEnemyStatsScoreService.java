package com.lolsearcher.databatch.service.score;

import com.lolsearcher.databatch.dto.stat.ChampionEnemyStatsDto;
import com.lolsearcher.databatch.dto.stat.ChampionStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.lolsearcher.databatch.constant.LolSearcherConstants.REDIS_CHAMP_ENEMY_STAT_KEY;

@RequiredArgsConstructor
@Service
public class ChampEnemyStatsScoreService implements ChampStatsScoreService {

    private final RedisTemplate<String, ChampionEnemyStatsDto> championEnemyStatsRedisTemplate;

    @Override
    public void saveChampionStats(ChampionStatsDto championStatsDto) {

        if(!(championStatsDto instanceof ChampionEnemyStatsDto)){
            throw new IllegalArgumentException("현재 파라미터는 ChampionPositionStatsDto 타입이 아닙니다.");
        }
        ChampionEnemyStatsDto dto = (ChampionEnemyStatsDto) championStatsDto;

        long wins = dto.getWins();
        long losses = dto.getLosses();
        double score = 1.1*wins + 0.9*losses;

        championEnemyStatsRedisTemplate.opsForZSet().removeRange(REDIS_CHAMP_ENEMY_STAT_KEY, 0, -1);
        championEnemyStatsRedisTemplate.opsForZSet().add(REDIS_CHAMP_ENEMY_STAT_KEY, dto, score);
    }
}
