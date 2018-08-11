package com.reactive.fun;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class FunApplicationTests {

    @Test
    public void withPublisher() throws InterruptedException {
        Flux<Integer> integerFlux = Flux.range(0, 1000).share().publish().autoConnect(2);
        subscribe(integerFlux);
    }




    @Test
    public void withReguler() throws InterruptedException {
        Flux<Integer> integerFlux = Flux.range(0, 1000);
        subscribe(integerFlux);

    }

    private void subscribe(Flux<Integer> integerFlux) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        integerFlux
                .delayElements(Duration.ofMillis(1))
                .publishOn(Schedulers.elastic())
                .subscribe(integer -> System.out.println("first subscriber got " + integer + " on thread" + Thread.currentThread())
                        ,exceptionHandler()
                        ,latch::countDown);

        integerFlux
                .buffer(10)
                .delayElements(Duration.ofSeconds(1))
                .publishOn(Schedulers.elastic())
                .subscribe(list-> System.out.println("second subscriber got " + list + " on thread" + Thread.currentThread())
                        ,exceptionHandler()
                        ,latch::countDown);

        latch.await();
    }

    Consumer<Throwable> exceptionHandler(){
        return (throwable -> System.out.println(throwable));
    }

}
