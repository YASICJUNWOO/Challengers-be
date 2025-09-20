package com.habitchallenge.presentation.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("/health-check")
class HealthController {

    @RequestMapping
    fun healthCheck(): String {
        return "OK"
    }

}