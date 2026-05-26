package org.zjh.aiagent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

/**
 * 查询重写器
 *
 * @author kayson
 * @since 2026/5/26 12:39
 */
@Component
public class QueryRewriter {

    private final QueryTransformer queryTransformer;

    public QueryRewriter(ChatClient.Builder chatClientBuilder) {
        this.queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .build();
    }

    public String rewrite(String prompt) {
        Query query = Query.builder().text(prompt).build();
        return queryTransformer.transform(query).text();
    }
}
