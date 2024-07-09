package com.aluracursos.screenmatch_spring.principal;

import com.aluracursos.screenmatch_spring.model.*;
import com.aluracursos.screenmatch_spring.repository.SerieRepository;
import com.aluracursos.screenmatch_spring.service.ConsumoAPI;
import com.aluracursos.screenmatch_spring.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi= new ConsumoAPI();
    private final String URL_BASE = "http://www.omdbapi.com/?t=";
    private final String URL_KEY = "&apikey=";
    private final String API_KEY = System.getenv("OMDB_APIKEY");
    private final String SEASON = "&Season=";
    private ConvierteDatos conversor = new ConvierteDatos();

    private List<DatosSerie> datosSeries = new ArrayList<>();
    private List<Serie> series;

    private Optional<Serie> serieBuscada;

    private SerieRepository repositorio;





    public Principal(SerieRepository repository) {
        this.repositorio = repository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar Series 
                    2 - Buscar Episodios
                    3 - Mostrar Series Buscadas
                    4 - Buscar Series Por Título
                    5 - Top 5 Mejores Series
                    6 - Buscar Series Por Género
                    7 - Filtrar Series
                    8 - Buscar Episodios Por Título
                    9 - Top 5 Episodios Por Serie                                 
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriesPorTitulo();
                    break;
                case 5:
                    buscarTop5Series();
                    break;
                case 6:
                    buscarSeriesPorCategoria();
                    break;
                case 7:
                    filtrarSeriesPorTemporadaYEvaluacion();
                    break;
                case 8:
                    buscarEpisodiosPorTitulo();
                    break;
                case 9:
                    buscarTop5Episodios();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }

    }

    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + URL_KEY + API_KEY);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        System.out.println(json);
        return datos;
    }
    private void buscarEpisodioPorSerie() {
        //DatosSerie datosSerie = getDatosSerie();

        mostrarSeriesBuscadas();
        System.out.println("Escribe el nombre de la serie para ver sus episodios: ");
        var nombreSerie = teclado.nextLine();

        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();

        if(serie.isPresent()){
            var serieEncontrada = serie.get();

            List<DatosTemporadas> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoApi.obtenerDatos(URL_BASE + serieEncontrada.getTitulo().replace(" ", "+") + SEASON + i + URL_KEY + API_KEY);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }

            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);

        }


    }
    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        Serie serie = new Serie(datos);
        repositorio.save(serie);
        //datosSeries.add(datos);
        System.out.println(datos);
    }

    private void mostrarSeriesBuscadas() {

        series = repositorio.findAll();


//        List<Serie> series = new ArrayList<>();
//        series = datosSeries.stream()
//                .map(d -> new Serie(d))
//                .collect(Collectors.toList());

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);

    }

    private void buscarSeriesPorTitulo() {

        System.out.println("Escribe el título de la serie: ");
        var nombreSerie = teclado.nextLine();

        serieBuscada = repositorio.findByTituloContainsIgnoreCase(nombreSerie);

        if(serieBuscada.isPresent()){
            System.out.println("La serie buscada es " + serieBuscada.get());
        }else {
            System.out.println("Serie no encontrada.");
        }

    }

    private void buscarTop5Series() {

        List<Serie> topSeries = repositorio.findTop5ByOrderByEvaluacionDesc();
        topSeries.forEach(s ->
                System.out.println("Serie: " + s.getTitulo() + " Evaluación: " + s.getEvaluacion()));

    }

    private void buscarSeriesPorCategoria() {

        System.out.println("Escriba el género de la serie: ");
        var genero = teclado.nextLine();
        var categoria = Categoria.fromEspanol(genero);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Las series del género " + genero + " son: ");
        seriesPorCategoria.forEach(System.out::println);

    }

    private void filtrarSeriesPorTemporadaYEvaluacion() {

        System.out.println("¿Filtrar series con cuántas temporadas? ");
        var totalTemporadas = teclado.nextInt();
        teclado.nextLine();
        System.out.println("¿A partir de qué valor? ");
        var evaluacion = teclado.nextDouble();
        teclado.nextLine();
        //List<Serie> filtroSeries = repositorio.findByTotalTemporadasLessThanEqualAndEvaluacionGreaterThanEqual(totalTemporadas, evaluacion);
        List<Serie> filtroSeries = repositorio.seriesPorTemporadaYEvalucion(totalTemporadas, evaluacion);
        System.out.println("*** Series filtradas ***");
        filtroSeries.forEach(s ->
                System.out.println(s.getTitulo() + "  - evaluacion: " + s.getEvaluacion()));

    }

    private void buscarEpisodiosPorTitulo() {

        System.out.println("Escribe el nombre del episodio: ");
        var nombreEpisodio = teclado.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorNombre(nombreEpisodio);
        episodiosEncontrados.forEach(e ->
                System.out.printf("Serie: %s \nTemporada %s Episodio %s Evaluación %s\n",
                        e.getSerie(), e.getTemporada(), e.getNumeroEpisodio(), e.getEvaluacion()));

    }

    private void buscarTop5Episodios() {

        buscarSeriesPorTitulo();

        if(serieBuscada.isPresent()){
            Serie serie = serieBuscada.get();
            List<Episodio> topEpisodios = repositorio.top5Episodios(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Serie: %s \n- Temporada %s - Episodio %s - Evaluación %s\n",
                            e.getSerie(), e.getTemporada(), e.getTitulo(), e.getEvaluacion()));
        }

    }



}




//Code from the first two courses:
//System.out.println("Escribe el nombre de la serie: ");
//
//        //Busca los datos generales de las series:
//        var nombreSerie = teclado.nextLine();
//        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
//        var datos = conversor.obtenerDatos(json, DatosSerie.class);
//        System.out.println(datos);
//
//        //Busca los datos de todas las temporadas:
//
//        List<DatosTemporadas> temporadas = new ArrayList<>();
//        for (int i = 1; i <= datos.totalDeTemporadas() ; i++) {
//            json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + SEASON + i + API_KEY);
//            var datosTemporadas = conversor.obtenerDatos(json, DatosTemporadas.class);
//            temporadas.add(datosTemporadas);
//        }

//temporadas.forEach(System.out::println);

//Mostrar solo el título de los episodios para las temporadas:
//        for (int i = 0; i < datos.totalDeTemporadas(); i++) {
//
//            List<DatosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
//
//            for (int j = 0; j < episodiosTemporada.size(); j++) {
//                System.out.println(episodiosTemporada.get(j).titulo());
//            }
//        }
//temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

//Convertir todas las informaciones a una lista de tipo DatosEpisodio
//        List<DatosEpisodio> datosEpisodios = temporadas.stream()
//                .flatMap(t -> t.episodios().stream())
//                .collect(Collectors.toList());

//Top 5 episodios
//        System.out.println("Top 5 episodios:");
//        datosEpisodios.stream()
//                .filter(e -> !e.evaluacion().equalsIgnoreCase("N/A"))
//                .peek(e -> System.out.println("Primer filtro (N/A)" + e))
//                .sorted(Comparator.comparing(DatosEpisodio::evaluacion).reversed())
//                .peek(e -> System.out.println("Segundo filtro ordenación (M>m)" + e))
//                .map(e -> e.titulo().toUpperCase())
//                .peek(e -> System.out.println("Tercer filtro Mayúsculas (m>M)" + e))
//                .limit(5)
//                .forEach(System.out::println);

//Convirtiendo los datos a una lista del tipo episodio:

//        List<Episodio> episodios = temporadas.stream()
//                .flatMap(t -> t.episodios().stream()
//                        .map(d -> new Episodio(t.numero(), d)))
//                .collect(Collectors.toList());

//episodios.forEach(System.out::println);

//Búsqueda de episodios a partir de cierto año:
//        System.out.println("Indique el año de emisión: ");
//        var fecha = teclado.nextInt();
//        teclado.nextLine();

//        LocalDate fechaBusqueda = LocalDate.of(fecha, 1, 1);
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

//        episodios.stream()
//                .filter(e -> e.getFechaDeLanzamiento() != null && e.getFechaDeLanzamiento().isAfter(fechaBusqueda))
//                .forEach(e -> System.out.println(
//                        "Temporada " + e.getTemporada() +
//                                " Episodio " + e.getTitulo() +
//                                " Fecha de Lanzamiento " + e.getFechaDeLanzamiento().format(dtf)
//                ));


//Busca episodios por parte del título:
//        System.out.println("Escribe el título del episodio: ");
//        var parteTitulo = teclado.nextLine();
//        Optional<Episodio> episodioBuscado = episodios.stream()
//                .filter(e -> e.getTitulo().toUpperCase().contains(parteTitulo.toUpperCase()))
//                .findFirst();
//
//        if(episodioBuscado.isPresent()){
//            System.out.println("Episodio encontrado!");
//            System.out.println("Los datos son " + episodioBuscado.get());
//        }else {
//            System.out.println("Episodio no encontrado, vuelva a buscar.");
//        }

//        Map<Integer, Double> evaluacionesPorTemporada = episodios.stream()
//                .filter(e -> e.getEvaluacion() > 0.0)
//                .collect(Collectors.groupingBy(Episodio::getTemporada,
//                        Collectors.averagingDouble(Episodio::getEvaluacion)));
//
//        System.out.println("Evaluación por temporada: " + evaluacionesPorTemporada);
//
//        DoubleSummaryStatistics est = episodios.stream()
//                .filter(e -> e.getEvaluacion() > 0.0)
//                .collect(Collectors.summarizingDouble(Episodio::getEvaluacion));
//
//        System.out.println("Media de las evaluaciones: " + est.getAverage());
//        System.out.println("Episodio mejor evaluado: " + est.getMax());
//        System.out.println("Episodio peor evaluado: " + est.getMin());

