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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTests {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private Source source;
    @Autowired
    private MessageCollector messageCollector;
    @Autowired
    private ObjectMapper objectMapper;

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
    public void testIncrementCounter() throws IOException {
        assertThat(restTemplate.postForEntity("/counter/increment", null, String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        Message<?> msg = messageCollector.forChannel(source.output()).poll();
        IncrementRequestMessage req = objectMapper.readerFor(IncrementRequestMessage.class).readValue(msg.getPayload().toString());
        assertThat(req.getTimestamp()).isNotNull();

        assertThat(restTemplate.postForEntity("/counter/increment", null, String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        msg = messageCollector.forChannel(source.output()).poll();
        req = objectMapper.readerFor(IncrementRequestMessage.class).readValue(msg.getPayload().toString());
        assertThat(req.getTimestamp()).isNotNull();
    }

    @SpringBootApplication
    @EnableBinding(Source.class)
    public static class MySource {
        @Autowired
        private Source source;
    }
}
