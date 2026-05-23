package org.zjh.aiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.ModelOptionsUtils;
import reactor.core.publisher.Flux;

import java.util.function.Function;

/**
 * 自定义日志 Advisor
 */
@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

	public static final Function<ChatClientRequest, String> DEFAULT_REQUEST_TO_STRING = ChatClientRequest::toString;

	public static final Function<ChatResponse, String> DEFAULT_RESPONSE_TO_STRING = ModelOptionsUtils::toJsonStringPrettyPrinter;

	private final Function<ChatClientRequest, String> requestToString;

	private final Function<ChatResponse, String> responseToString;

	private final int order;

	public MyLoggerAdvisor() {
		this(DEFAULT_REQUEST_TO_STRING, DEFAULT_RESPONSE_TO_STRING, 0);
	}

	public MyLoggerAdvisor(int order) {
		this(DEFAULT_REQUEST_TO_STRING, DEFAULT_RESPONSE_TO_STRING, order);
	}

	public MyLoggerAdvisor(Function<ChatClientRequest, String> requestToString,
						   Function<ChatResponse, String> responseToString, int order) {
		this.requestToString = requestToString != null ? requestToString : DEFAULT_REQUEST_TO_STRING;
		this.responseToString = responseToString != null ? responseToString : DEFAULT_RESPONSE_TO_STRING;
		this.order = order;
	}

	@NotNull
	@Override
	public ChatClientResponse adviseCall(@NotNull ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
		logRequest(chatClientRequest);

		ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

		logResponse(chatClientResponse);

		return chatClientResponse;
	}

	@NotNull
	@Override
	public Flux<ChatClientResponse> adviseStream(@NotNull ChatClientRequest chatClientRequest,
												 StreamAdvisorChain streamAdvisorChain) {
		logRequest(chatClientRequest);

		Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);

		return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this::logResponse);
	}

	protected void logRequest(ChatClientRequest request) {
		log.debug("AI request: {}", this.requestToString.apply(request));
		log.info("AI request: {}", request.prompt().getUserMessage().getText());
	}

	protected void logResponse(ChatClientResponse chatClientResponse) {
		log.debug("AI response: {}", this.responseToString.apply(chatClientResponse.chatResponse()));
		if (chatClientResponse.chatResponse() == null) {
			log.error("AI response is null");
		}
        log.info("AI response: {}", chatClientResponse.chatResponse().getResult().getOutput().getText());
	}

	@NotNull
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public String toString() {
		return MyLoggerAdvisor.class.getSimpleName();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private Function<ChatClientRequest, String> requestToString;

		private Function<ChatResponse, String> responseToString;

		private int order = 0;

		private Builder() {
		}

		public Builder requestToString(Function<ChatClientRequest, String> requestToString) {
			this.requestToString = requestToString;
			return this;
		}

		public Builder responseToString(Function<ChatResponse, String> responseToString) {
			this.responseToString = responseToString;
			return this;
		}

		public Builder order(int order) {
			this.order = order;
			return this;
		}

		public MyLoggerAdvisor build() {
			return new MyLoggerAdvisor(this.requestToString, this.responseToString, this.order);
		}

	}

}