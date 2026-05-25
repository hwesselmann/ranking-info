package de.hdawg.rankinginfo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableCaching
@EnableScheduling
class RankingInfoApplication

fun main(args: Array<String>) {
    runApplication<RankingInfoApplication>(*args)
}
