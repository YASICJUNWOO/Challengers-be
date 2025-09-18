package com.habitchallenge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class HabitChallengeApplication

fun main(args: Array<String>) {
    runApplication<HabitChallengeApplication>(*args)
}