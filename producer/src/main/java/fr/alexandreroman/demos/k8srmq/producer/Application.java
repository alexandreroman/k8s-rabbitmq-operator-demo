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

package fr.alexandreroman.demos.k8srmq.producer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
@RequiredArgsConstructor
@EnableBinding(Source.class)
@Slf4j
class CounterController {
    private final Source source;
    private final Counter sentRequestsCounter;

    @PostMapping("/counter/increment")
    public String increment() {
        log.info("Sending increment request");
        sentRequestsCounter.increment();
        // Just send an increment request message: producer do not store any value.
        source.output().send(MessageBuilder.withPayload(new IncrementRequestMessage()).build());
        return "Increment request sent";
    }
}

@Configuration
class MicrometerConfig {
    @Bean
    Counter sentRequestsCounter(MeterRegistry registry) {
        return io.micrometer.core.instrument.Counter
                .builder("k8srmq.requests.sent")
                .description("Number of sent increment requests")
                .register(registry);
    }
}

@Data
class IncrementRequestMessage {
    // Include some data in the increment request message, but it's not really necessary.
    private OffsetDateTime timestamp = OffsetDateTime.now();
}
