package pl.guz.rxjavaexamples.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;


public class Sync_VS_Async_Calls {

    private Logger logger = LoggerFactory.getLogger(Sync_VS_Async_Calls.class);

    private static WireMockServer wireMockServer;

    @BeforeClass
    public static void start() {
        wireMockServer = new WireMockServer(wireMockConfig().port(8080));
        wireMockServer.start();
        stubServer();
    }

    @AfterClass
    public static void stop() {
        wireMockServer.shutdown();
    }

    private OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .readTimeout(1010, TimeUnit.MINUTES)
            .writeTimeout(200, TimeUnit.MINUTES)
            .build();

    @Test
    public void should_async_call() throws InterruptedException {
        Scheduler scheduler = Schedulers.from(Executors.newFixedThreadPool(1));
        Retrofit retrofit = retrofitAsyncFactory();
        Api api = retrofit.create(Api.class);


        Flowable.just(1000, 200, 200)
                .doOnNext(x -> logger.info("Before call: {}", x))
                .flatMap(delay -> api
                        .asyncCall(delay)
                        .observeOn(scheduler))
                .doOnNext(x -> logger.info("After call: {}", x))
                .doOnSubscribe(x -> logger.info("Subscribe"))
                .subscribeOn(scheduler)
                .test()
                .await()
                .assertValueSequence(Arrays.asList(new Wait(200), new Wait(200), new Wait(1000)));
    }

    @Test
    public void should_sync_call() throws InterruptedException {
        Scheduler scheduler = Schedulers.from(Executors.newFixedThreadPool(1));
        Retrofit retrofit = retrofitSycnFactory(scheduler);
        Api api = retrofit.create(Api.class);

        Flowable.just(1000, 200, 200)
                .doOnNext(x -> logger.info("Before call: {}", x))
                .flatMap(x -> Flowable.fromCallable(() -> api.call(x).execute().body()))
                .doOnNext(x -> logger.info("After call: {}", x))
                .doOnSubscribe(x -> logger.info("Subscribe"))
                .subscribeOn(scheduler)
                .test()
                .await()
                .assertValueSequence(Arrays.asList(new Wait(1000), new Wait(200), new Wait(200)));
    }


    private Retrofit retrofitAsyncFactory() {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl("http://localhost:8080")
                .build();
    }

    private Retrofit retrofitSycnFactory(Scheduler scheduler) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(scheduler))
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl("http://localhost:8080")
                .build();
    }

    private static void stubServer() {
        wireMockServer
                .stubFor(
                        get(urlEqualTo("/waits/1000"))
                                .willReturn(aResponse().withFixedDelay(1000)
                                        .withBody("{\"time\": 1000}")
                                )
                );
        wireMockServer
                .stubFor(
                        get(urlEqualTo("/waits/200"))
                                .willReturn(aResponse().withFixedDelay(200)
                                        .withBody("{\"time\": 200}"))
                );
    }

    interface Api {

        @GET("waits/{time}")
        Call<Wait> call(@Path("time") Integer time);

        @GET("waits/{time}")
        Flowable<Wait> asyncCall(@Path("time") Integer time);
    }
}