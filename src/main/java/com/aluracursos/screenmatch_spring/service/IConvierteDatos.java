package com.aluracursos.screenmatch_spring.service;

public interface IConvierteDatos {

    <T> T obtenerDatos(String json, Class<T> clase);

}
