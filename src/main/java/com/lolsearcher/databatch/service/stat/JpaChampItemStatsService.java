package com.lolsearcher.databatch.service.stat;

import com.lolsearcher.databatch.constant.enumeration.GameResultStatus;
import com.lolsearcher.databatch.dto.stat.ChampionItemStatsDto;
import com.lolsearcher.databatch.dto.stat.ChampionStatsDto;
import com.lolsearcher.databatch.entity.champion.ChampBanStats;
import com.lolsearcher.databatch.entity.champion.ChampItemStats;
import com.lolsearcher.databatch.entity.champion.ChampPositionStats;
import com.lolsearcher.databatch.entity.match.Match;
import com.lolsearcher.databatch.entity.match.SummaryMember;
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
public class JpaChampItemStatsService implements JpaChampStatsService {

    private final Map<String /*  gameVersion + queueId + championId + positionId + itemId */, int[] /* idx:gameResult, value:count */>
            champItemStats = new ConcurrentHashMap<>();


    @Override
    public void recordChampStats(Match match) {

        for(Team team : match.getTeams()){
            GameResultStatus gameResult = GameResultStatus.valueOfCode(team.getGameResult());
            if(gameResult == GameResultStatus.DRAW){
                log.info("게임 결과가 무승부입니다. 기록할 필요가 없습니다.");
                return;
            }
            for(SummaryMember member : team.getMembers()){

                List.of(member.getItem0(), member.getItem1(), member.getItem2(), member.getItem3(), member.getItem4(), member.getItem5())
                        .forEach(item -> {
                            String champItemKey = match.getVersion() + ":" + match.getQueueId() + ":" +
                                    member.getPickChampionId() + ":" + member.getPositionId() + ":" +item;

                            int[] champItemCount = champItemStats.getOrDefault(champItemKey, new int[2]);
                            if(gameResult == GameResultStatus.WIN) {
                                champItemCount[0]++;
                            } else{
                                champItemCount[1]++;
                            }
                        });
            }
        }
    }

    @Override
    public List<ChampionStatsDto> extractChampStats() {

        return champItemStats.entrySet()
                .stream()
                .map(entry -> {
                    String[] str = entry.getKey().split(":");
                    String gameVersion = str[0];
                    int queueId = Integer.parseInt(str[1]);
                    int championId = Integer.parseInt(str[2]);
                    int positionId = Integer.parseInt(str[3]);
                    int itemId = Integer.parseInt(str[4]);

                    return ChampionItemStatsDto.builder()
                            .gameVersion(gameVersion)
                            .queueId(queueId)
                            .championId(championId)
                            .positionId(positionId)
                            .itemId(itemId)
                            .wins(entry.getValue()[0])
                            .losses(entry.getValue()[1])
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public void clearRepository() {
        champItemStats.clear();
    }

    @Override
    public void persistChampStats(EntityManager em, ChampionStatsDto championStatsDto) {

        if(!(championStatsDto instanceof ChampionItemStatsDto)) {
            log.error("파라미터 타입이 올바르지 못함. 예상 값 : {}, 실제 값 : {}", ChampionItemStatsDto.class, championStatsDto.getClass());
            throw new IllegalArgumentException("championStatsDto 객체는 ChampionPositionStatsDto 타입이 아님");
        }
        if(em == null){
            throw new DataAccessResourceFailureException("Entity Manager가 존재하지 않음");
        }

        ChampionItemStatsDto dto = (ChampionItemStatsDto) championStatsDto;

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

        ChampItemStats champItemStats = findChampItemStats(champPositionStats, dto);
        if(champItemStats == null){
            em.persist(createChampItemStats(champPositionStats, dto));
        }else{
            updateChampItemStats(champItemStats, dto);
        }
    }

    private ChampBanStats findChampBanStats(EntityManager em, ChampionItemStatsDto dto) {

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


    private ChampBanStats createChampBanStats(ChampionItemStatsDto dto) {

        return ChampBanStats.builder()
                .gameVersion(dto.getGameVersion())
                .queueId(dto.getQueueId())
                .championId(dto.getChampionId())
                .build();
    }

    private ChampPositionStats findChampPositionStats(ChampBanStats champBanStats, ChampionItemStatsDto dto) {

        return champBanStats.getChampPositionStats()
                .stream()
                .filter(champPositionStat -> champPositionStat.getPositionId() == dto.getPositionId())
                .findFirst()
                .orElse(null);
    }

    private ChampPositionStats createChampPositionStats(ChampBanStats champBanStats, ChampionItemStatsDto dto) {

        ChampPositionStats positionStat = new ChampPositionStats();
        positionStat.setPositionId(dto.getPositionId());
        positionStat.setChampBanStatsId(champBanStats);

        return positionStat;
    }

    private ChampItemStats findChampItemStats(ChampPositionStats champPositionStats, ChampionItemStatsDto dto) {

        return champPositionStats.getChampItemStats()
                .stream()
                .filter(champItemStat -> champItemStat.getItemId() == dto.getItemId())
                .findFirst()
                .orElse(null);
    }

    private ChampItemStats createChampItemStats(ChampPositionStats champPositionStats, ChampionItemStatsDto dto) {

        ChampItemStats itemStat = new ChampItemStats();
        itemStat.setChampPositionStatsId(champPositionStats);
        itemStat.setItemId(dto.getItemId());
        itemStat.setWins(dto.getWins());
        itemStat.setLosses(dto.getLosses());

        return itemStat;
    }

    private void updateChampItemStats(ChampItemStats champItemStats, ChampionItemStatsDto dto) {

        champItemStats.setWins(champItemStats.getWins() + dto.getWins());
        champItemStats.setLosses(champItemStats.getLosses() + dto.getLosses());
    }
}
