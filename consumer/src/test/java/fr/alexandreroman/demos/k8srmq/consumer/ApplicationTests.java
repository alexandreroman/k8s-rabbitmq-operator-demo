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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTests {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private Sink sink;
    @Autowired
    private IndexController indexController;

    @Test
    public void contextLoads() {
    }

    @Test
    public void testPrometheus() {
        assertThat(restTemplate.getForEntity("/actuator/prometheus", String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testHealth() {
        assertThat(restTemplate.getForEntity("/actuator/health", String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testStreamListener() {
        sink.input().send(MessageBuilder.withPayload(new IncrementRequestMessage()).build());
        final Map<String, Object> model = new HashMap<>(1);
        final String view = indexController.index(model);
        assertThat(view).isEqualTo("index");
        assertThat(model).containsKey("counter");
        assertThat((int) model.get("counter")).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void testStreamListenerPrometheus() throws InterruptedException {
        sink.input().send(MessageBuilder.withPayload(new IncrementRequestMessage()).build());
        final ResponseEntity<String> resp = restTemplate.getForEntity("/actuator/prometheus", String.class);
        Thread.sleep(250);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        final String[] lines = resp.getBody().split("\n");
        final Optional<String> opt = Arrays.stream(lines).filter(line -> line.startsWith("k8srmq_requests_received_total ")).findFirst();
        assertThat(opt).isPresent();
        final String counterStr = opt.get().substring("k8srmq_requests_received_total ".length());
        final int counterValue = (int) Double.parseDouble(counterStr);
        assertThat(counterValue).isGreaterThanOrEqualTo(1);
    }

    @SpringBootApplication
    @EnableBinding(Sink.class)
    public static class MySink {
        @Autowired
        private Sink sink;
    }
}
