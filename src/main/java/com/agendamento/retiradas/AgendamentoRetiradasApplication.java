package com.agendamento.retiradas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AgendamentoRetiradasApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgendamentoRetiradasApplication.class, args);
    }
}
