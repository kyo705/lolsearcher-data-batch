package com.lolsearcher.databatch.service.stat;

import com.lolsearcher.databatch.constant.enumeration.GamePosition;
import com.lolsearcher.databatch.constant.enumeration.GameResultStatus;
import com.lolsearcher.databatch.dto.stat.ChampionEnemyStatsDto;
import com.lolsearcher.databatch.dto.stat.ChampionItemStatsDto;
import com.lolsearcher.databatch.dto.stat.ChampionStatsDto;
import com.lolsearcher.databatch.entity.champion.ChampBanStats;
import com.lolsearcher.databatch.entity.champion.ChampEnemyStats;
import com.lolsearcher.databatch.entity.champion.ChampPositionStats;
import com.lolsearcher.databatch.entity.match.Match;
import com.lolsearcher.databatch.entity.match.Team;
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
public class JpaChampEnemyStatsService implements JpaChampStatsService {

    private final Map<String /*  gameVersion + queueId + championId + positionId + enemyId */, int[] /* idx:gameResult, value:count */>
            champEnemyStats = new ConcurrentHashMap<>();

    private final Map<GamePosition, int[] /* 0:blueTeamChampionId, 1:redTeamChampionId */> positionChampions =
            new ConcurrentHashMap<>(
                    Map.of(GamePosition.TOP, new int[2],
                            GamePosition.JUNGLE, new int[2],
                            GamePosition.MIDDLE, new int[2],
                            GamePosition.BOTTOM, new int[2],
                            GamePosition.UTILITY, new int[2])
            );

    @Override
    public void recordChampStats(Match match) {

        Team blueTeam = match.getTeams().get(0);
        Team redTeam = match.getTeams().get(1);

        if((blueTeam.getGameResult() == GameResultStatus.DRAW.getCode()) || (redTeam.getGameResult() == GameResultStatus.DRAW.getCode())){
            log.info("게임 결과가 무승부입니다. 기록할 필요가 없습니다.");
            return;
        }

        for(int i=0;i<5;i++){
            blueTeam.getMembers()
                    .forEach(member -> {
                        GamePosition gamePosition = GamePosition.valueOfCode(member.getPositionId());
                        positionChampions.get(gamePosition)[0] = member.getPickChampionId();
                    });

            redTeam.getMembers()
                    .forEach(member -> {
                        GamePosition gamePosition = GamePosition.valueOfCode(member.getPositionId());
                        positionChampions.get(gamePosition)[1] = member.getPickChampionId();
                    });
        }

        positionChampions.forEach((key, value) -> {
            int positionId = key.getCode();
            int championId1 = value[0];
            int championId2 = value[1];

            String champEnemyStatsKey1 = match.getVersion() + ":" + match.getQueueId() + ":" +
                    championId1 + ":" + positionId + ":" + championId2;

            String champEnemyStatsKey2 = match.getVersion() + ":" + match.getQueueId() + ":" +
                    championId2 + ":" + positionId + ":" + championId1;

            int[] champItemCount1 = champEnemyStats.getOrDefault(champEnemyStatsKey1, new int[2]);
            int[] champItemCount2 = champEnemyStats.getOrDefault(champEnemyStatsKey2, new int[2]);

            if (blueTeam.getGameResult() == GameResultStatus.WIN.getCode()) {
                champItemCount1[0]++;
                champItemCount2[1]++;
            } else {
                champItemCount1[1]++;
                champItemCount2[0]++;
            }
        });
    }

    @Override
    public List<ChampionStatsDto> extractChampStats() {

        return champEnemyStats.entrySet()
                .stream()
                .map(entry -> {
                    String[] str = entry.getKey().split(":");
                    String gameVersion = str[0];
                    int queueId = Integer.parseInt(str[1]);
                    int championId = Integer.parseInt(str[2]);
                    int positionId = Integer.parseInt(str[3]);
                    int enemyChampionId = Integer.parseInt(str[4]);

                    return ChampionEnemyStatsDto.builder()
                            .gameVersion(gameVersion)
                            .queueId(queueId)
                            .championId(championId)
                            .positionId(positionId)
                            .enemyChampionId(enemyChampionId)
                            .wins(entry.getValue()[0])
                            .losses(entry.getValue()[1])
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public void clearRepository() {
        champEnemyStats.clear();
        positionChampions.values().forEach(value -> {
            value[0] = 0;
            value[1] = 0;
        });
    }

    @Override
    public void persistChampStats(EntityManager em, ChampionStatsDto championStatsDto) {

        if(!(championStatsDto instanceof ChampionEnemyStatsDto)) {
            log.error("파라미터 타입이 올바르지 못함. 예상 값 : {}, 실제 값 : {}", ChampionItemStatsDto.class, championStatsDto.getClass());
            throw new IllegalArgumentException("championStatsDto 객체는 ChampionPositionStatsDto 타입이 아님");
        }
        if(em == null){
            throw new DataAccessResourceFailureException("Entity Manager가 존재하지 않음");
        }

        ChampionEnemyStatsDto dto = (ChampionEnemyStatsDto) championStatsDto;

        ChampBanStats champBanStats = findChampBanStats(em, dto);
        if(champBanStats == null){
            champBanStats = createChampBanStats(dto);
            em.persist(champBanStats);
        }

        ChampPositionStats champPositionStats = findChampPositionStats(champBanStats, dto);
        if(champPositionStats == null){
            champPositionStats = createChampPositionStats(champBanStats, dto);
            em.persist(champPositionStats);
        }

        ChampEnemyStats champEnemyStats = findChampEnemyStats(champPositionStats, dto);
        if(champEnemyStats == null){
            em.persist(createChampEnemyStats(champPositionStats, dto));
        }else{
            updateChampEnemyStats(champEnemyStats, dto);
        }
    }

    private ChampBanStats findChampBanStats(EntityManager em, ChampionEnemyStatsDto dto) {

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


    private ChampBanStats createChampBanStats(ChampionEnemyStatsDto dto) {

        return ChampBanStats.builder()
                .gameVersion(dto.getGameVersion())
                .queueId(dto.getQueueId())
                .championId(dto.getChampionId())
                .build();
    }

    private ChampPositionStats findChampPositionStats(ChampBanStats champBanStats, ChampionEnemyStatsDto dto) {

        return champBanStats.getChampPositionStats()
                .stream()
                .filter(champPositionStat -> champPositionStat.getPositionId() == dto.getPositionId())
                .findFirst()
                .orElse(null);
    }

    private ChampPositionStats createChampPositionStats(ChampBanStats champBanStats, ChampionEnemyStatsDto dto) {

        return ChampPositionStats.builder()
                .champBanStatsId(champBanStats)
                .positionId(dto.getPositionId())
                .build();
    }

    private ChampEnemyStats findChampEnemyStats(ChampPositionStats champPositionStats, ChampionEnemyStatsDto dto) {

        return champPositionStats.getChampEnemyStats()
                .stream()
                .filter(champEnemyStat -> champEnemyStat.getEnemyChampionId() == dto.getEnemyChampionId())
                .findFirst()
                .orElse(null);
    }

    private ChampEnemyStats createChampEnemyStats(ChampPositionStats champPositionStats, ChampionEnemyStatsDto dto) {

        return ChampEnemyStats.builder()
                .champPositionStatsId(champPositionStats)
                .enemyChampionId(dto.getEnemyChampionId())
                .wins(dto.getWins())
                .losses(dto.getLosses())
                .build();
    }

    private void updateChampEnemyStats(ChampEnemyStats champEnemyStats, ChampionEnemyStatsDto dto) {

        champEnemyStats.setWins(champEnemyStats.getWins() + dto.getWins());
        champEnemyStats.setLosses(champEnemyStats.getLosses() + dto.getLosses());
    }
}
