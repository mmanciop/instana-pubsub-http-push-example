package com.instana.gcp.pubsub.push;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.instana.sdk.annotation.Span;
import com.instana.sdk.annotation.Span.Type;
import com.instana.sdk.support.SpanSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class PubSubPushDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PubSubPushDemoApplication.class, args);
	}

	@RestController
	static class PushSubPushController {

		private static final Logger LOGGER = LoggerFactory.getLogger(PubSubPushDemoApplication.class);

		@RequestMapping(
			value = "/", method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_VALUE
		)
		public ResponseEntity<?> consumePushNotification(@RequestBody final PubSubMessage message) {
			if (SpanSupport.isTracing()) {
				SpanSupport.clearCurrent();
			}

			restoreInstanaTraceContextIfPresent(message);

			processMessage(message);

			return ResponseEntity.accepted().build();
		}

		/**
		 * This method will look into the attributes of this message, searching for Instana tracing headers,
		 * specifically {@code X-INSTANA-T} and {@code X-INSTANA-S}, and use them to create a trace context,
		 * that is then used when processing the {@code @Span} annocation on the {@link #processMessage} method.
		 */
		private void restoreInstanaTraceContextIfPresent(final PubSubMessage message) {
			if (message.getMessage().getAttributes() != null) {
				String traceId = null, spanId = null;

				for (final Entry<String, String> attribute : message.getMessage().getAttributes().entrySet()) {
					final String attributeKeyUppercase = attribute.getKey().toUpperCase();
					if (traceId == null && SpanSupport.TRACE_ID.equals(attributeKeyUppercase)) {
						traceId = attribute.getValue();
					} else if (spanId == null && SpanSupport.SPAN_ID.equals(attributeKeyUppercase)) {
						spanId = attribute.getValue();
					}

					if (traceId != null && spanId != null) {
						SpanSupport.inheritNext(traceId, spanId);
						break;
					}
				}
			}
		}

		@Span(
			value = "CONSUME message",
			type = Type.ENTRY
		)
		private void processMessage(final PubSubMessage message) {
			SpanSupport.annotate("message_bus.destination", message.getSubscription());

			LOGGER.info("Received the following data: {}", message.getMessage().getData());
		}

	}

	static class PubSubMessage {

		private final String subscription;
		private final Message message;

		@JsonCreator
		public PubSubMessage(final String subscription, final Message message) {
			this.subscription = subscription;
			this.message = message;
		}

		public String getSubscription() {
			return subscription;
		}

		public Message getMessage() {
			return message;
		}

		static class Message {

			/*
			 * These attributes will contain, among other things, the Instana trace context;
			 * specifically, the trace identifier is stored in the {@code x-instana-t} attribute, and span identifier
			 * in the {@code x-instana-s} attribute.
			 */
			private final Map<String, String> attributes;
			private final String messageId;
			private final String publishTime;
			private final String data;

			@JsonCreator
			public Message(final Map<String, String> attributes, final String messageId, final String publishTime, final String data) {
				this.attributes = Objects.nonNull(attributes) ? Collections.unmodifiableMap(attributes) : Collections.emptyMap();
				this.messageId = messageId;
				this.publishTime = publishTime;
				this.data = data;
			}

			public Map<String, String> getAttributes() {
				return attributes;
			}

			public String getMessageId() {
				return messageId;
			}

			public String getPublishTime() {
				return publishTime;
			}

			public String getData() {
				return data;
			}

		}

	}

}
