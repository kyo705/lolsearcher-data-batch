package com.lolsearcher.databatch.config.redis;

import com.lolsearcher.databatch.dto.stat.ChampionEnemyStatsDto;
import com.lolsearcher.databatch.dto.stat.ChampionItemStatsDto;
import com.lolsearcher.databatch.dto.stat.ChampionPositionStatsDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${lolsearcher.redis.host}")
    private String host;
    @Value("${lolsearcher.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory(){

        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisTemplate<String, ChampionPositionStatsDto> championPositionStatsRedisTemplate
            (RedisConnectionFactory connectionFactory){

        RedisTemplate<String, ChampionPositionStatsDto> template = new RedisTemplate<>();

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChampionPositionStatsDto.class));
        template.setConnectionFactory(connectionFactory);

        return template;
    }

    @Bean
    public RedisTemplate<String, ChampionItemStatsDto> championItemStatsRedisTemplate
            (RedisConnectionFactory connectionFactory){

        RedisTemplate<String, ChampionItemStatsDto> template = new RedisTemplate<>();

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChampionItemStatsDto.class));
        template.setConnectionFactory(connectionFactory);

        return template;
    }

    @Bean
    public RedisTemplate<String, ChampionEnemyStatsDto> championEnemyStatsRedisTemplate
            (RedisConnectionFactory connectionFactory){

        RedisTemplate<String, ChampionEnemyStatsDto> template = new RedisTemplate<>();

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChampionEnemyStatsDto.class));
        template.setConnectionFactory(connectionFactory);

        return template;
    }
}
