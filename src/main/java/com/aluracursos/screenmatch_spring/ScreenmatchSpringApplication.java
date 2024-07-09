package com.aluracursos.screenmatch_spring;

import com.aluracursos.screenmatch_spring.principal.EjemploStreams;
import com.aluracursos.screenmatch_spring.principal.Principal;
import com.aluracursos.screenmatch_spring.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScreenmatchSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchSpringApplication.class, args);
	}

}

