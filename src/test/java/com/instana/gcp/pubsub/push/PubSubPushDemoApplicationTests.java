package com.instana.gcp.pubsub.push;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.io.InputStream;
import java.net.URI;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class PubSubPushDemoApplicationTests {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	public void processRequest() throws Exception {
		try (final InputStream is = PubSubPushDemoApplicationTests.class.getClassLoader().getResourceAsStream("message-with-trace-context.json")) {
			InputStreamResource inputStreamResource = new InputStreamResource(is);
			RequestEntity<?> request = RequestEntity
				.post(new URI("/"))
				.headers(headers -> {
					headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
				})
				.body(inputStreamResource);

			ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);

			assertThat(response.getStatusCode(), equalTo(HttpStatus.ACCEPTED));
		}
	}

}
