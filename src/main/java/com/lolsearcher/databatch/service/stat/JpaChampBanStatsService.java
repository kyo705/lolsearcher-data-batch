package com.lolsearcher.databatch.service.stat;

import com.lolsearcher.databatch.dto.stat.ChampionPositionStatsDto;
import com.lolsearcher.databatch.dto.stat.ChampionStatsDto;
import com.lolsearcher.databatch.entity.champion.ChampBanStats;
import com.lolsearcher.databatch.entity.match.Match;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JpaChampBanStatsService implements JpaChampStatsService {

    Map<String /* version + queueId + banned championId */, Integer /* count */> champBanStats = new ConcurrentHashMap<>();
    @Override
    public void recordChampStats(Match match) {

        match.getBannedChampionIds()
                .stream()
                .map(bannedChampion -> match.getVersion() + ":" + match.getQueueId() + ":" + bannedChampion.getChampionId())
                .forEach(key -> champBanStats.put(key, champBanStats.getOrDefault(key, 0) + 1));
    }

    @Override
    public List<ChampionStatsDto> extractChampStats() {

        return champBanStats.entrySet()
                .stream()
                .map(entry -> {
                    String[] str = entry.getKey().split(":");

                    String gameVersion = str[0];
                    int queueId = Integer.parseInt(str[1]);
                    int bannedChampionId = Integer.parseInt(str[2]);
                    int banCount = entry.getValue();

                    return ChampionPositionStatsDto.builder()
                            .gameVersion(gameVersion)
                            .queueId(queueId)
                            .championId(bannedChampionId)
                            .bans(banCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public void clearRepository() {

        champBanStats.clear();
    }

    @Override
    public void persistChampStats(EntityManager em, ChampionStatsDto championStatsDto) {

        if(!(championStatsDto instanceof ChampionPositionStatsDto)) {
            log.error("파라미터 타입이 올바르지 못함. 예상 값 : {}, 실제 값 : {}", ChampionPositionStatsDto.class, championStatsDto.getClass());
            throw new IllegalArgumentException("championStatsDto 객체는 ChampionPositionStatsDto 타입이 아님");
        }
        if(em == null){
            throw new DataAccessResourceFailureException("Entity Manager가 존재하지 않음");
        }

        ChampionPositionStatsDto dto = (ChampionPositionStatsDto) championStatsDto;

        ChampBanStats champBanStats = findChampBanStats(em, dto);
        if(champBanStats == null){
            em.persist(createChampPositionStats(dto));
        }else{
            updateChampPositionStats(champBanStats, dto);
        }
    }

    private ChampBanStats findChampBanStats(EntityManager em, ChampionPositionStatsDto dto) {

        String query = "SELECT c FROM ChampBanStats c " +
                "WHERE c.gameVersion = :gameVersion AND c.queueId = :queueId AND c.championId = :championId";

        List<ChampBanStats> result = em.createQuery(query, ChampBanStats.class)
                .setParameter("queueId", dto.getQueueId())
                .setParameter("gameVersion", dto.getGameVersion())
                .setParameter("championId", dto.getChampionId())
                .getResultList();

        if(result.size() >= 2){
            throw new ValidationException("통계 데이터는 반드시 하나만 존재해야함");
        }
        if(result.size() == 0){
            return null;
        }
        return result.get(0);
    }

    private ChampBanStats createChampPositionStats(ChampionPositionStatsDto dto) {

        return ChampBanStats.builder()
                .gameVersion(dto.getGameVersion())
                .queueId(dto.getQueueId())
                .championId(dto.getChampionId())
                .bans(dto.getBans())
                .build();
    }

    private void updateChampPositionStats(ChampBanStats champBanStats, ChampionPositionStatsDto dto) {

        champBanStats.setBans(champBanStats.getBans() + dto.getBans());
    }

}
