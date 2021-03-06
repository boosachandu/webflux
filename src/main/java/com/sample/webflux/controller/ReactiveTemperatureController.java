package com.sample.webflux.controller;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

public class ReactiveTemperatureController {

    private int temperature;
    private String weather;

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route(RequestPredicates.GET("/temperature"), this::temperature).
                andRoute(RequestPredicates.POST("/temperature"), this::postTemperature);
    }

    public Mono<ServerResponse> postTemperature(ServerRequest req) {
        return req.bodyToMono(Temperature.class)
                .flatMap(temperature1 -> {
                    this.temperature = temperature1.getTemperature();
                    this.weather = temperature1.getWeather();
                    return ServerResponse.ok().body(Mono.just(temperature1), Temperature.class);
                }).switchIfEmpty(ServerResponse.badRequest().build());
    }


    public Mono<ServerResponse> temperature(ServerRequest req) {
        Stream<Integer> stream = Stream.iterate(0, i -> i + 1);
        Flux<Temperature> mapFlux = Flux.fromStream(stream).zipWith(Flux.interval(Duration.ofSeconds(1)))
                .map(i -> {
                    Temperature temperature = new Temperature();
                    temperature.setTemperature(this.temperature);
                    temperature.setWeather(weather);
                    return temperature;
                });

        return ok().contentType(MediaType.APPLICATION_STREAM_JSON).body(mapFlux,
                Temperature.class);
    }
}
