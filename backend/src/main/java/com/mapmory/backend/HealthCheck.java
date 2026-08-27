package com.mapmory.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheck {

    @GetMapping("/health")
    public String healthCheck() {
        return """
                <h2> Server is Running... </h2>
                <br/>
                <br/>
                <span style="font-size: 40px;">
                    <span style="color: red;">화</span>
                    <span style="color: orange;">이</span>
                    <span style="color: gold;">팅</span>
                    <span style="color: green;"> 우</span>
                    <span style="color: blue;">리</span>
                    <span style="color: navy;">팀</span>
                    <span style="color: purple;">~</span>
                    <span style="color: hotpink;">~</span>
                    <span style="color: red;">~</span>!
                </span>
                """;
    }
}
