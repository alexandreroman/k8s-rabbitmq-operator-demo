/*
 * Copyright (c) 2019 Pivotal Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.alexandreroman.demos.k8srmq.consumer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.OffsetDateTime;
import java.util.Map;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@Controller
@RequiredArgsConstructor
@Slf4j
class IndexController {
    private final Counter receivedRequestsCounter;

    @GetMapping("/")
    public String index(Map<String, Object> model) {
        // Get counter value from metrics: a real-world application
        // should use a persistence backend service to store this state.
        model.put("counter", (int) receivedRequestsCounter.count());
        return "index";
    }
}

@RequiredArgsConstructor
@EnableBinding(Sink.class)
@Slf4j
class CounterListener {
    private final Counter receivedRequestsCounter;

    @StreamListener(Sink.INPUT)
    public void onCounterMessage(IncrementRequestMessage msg) {
        log.info("Received increment request");
        receivedRequestsCounter.increment();
    }
}

@Configuration
class MicrometerConfig {
    @Bean
    Counter receivedRequestsCounter(MeterRegistry registry) {
        return io.micrometer.core.instrument.Counter
                .builder("k8srmq.requests.received")
                .description("Number of received increment requests")
                .register(registry);
    }
}

@Data
class IncrementRequestMessage {
    private OffsetDateTime timestamp = OffsetDateTime.now();
}
