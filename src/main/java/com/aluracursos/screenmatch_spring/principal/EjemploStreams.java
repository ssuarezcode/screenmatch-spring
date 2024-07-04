package com.aluracursos.screenmatch_spring.principal;

import java.util.Arrays;
import java.util.List;

public class EjemploStreams {

    public void muestraEjemplo(){
        List<String> nombres = Arrays.asList("Rei", "Reiko", "Reini", "Reikorena", "Reinines");

        nombres.stream()
                .sorted()
                .limit(4)
                .filter(n -> n.startsWith("R"))
                .map(n -> n.toUpperCase())
                .forEach(System.out::println);
    }

}
