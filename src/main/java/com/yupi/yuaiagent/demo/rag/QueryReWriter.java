package com.yupi.yuaiagent.demo.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

@Component
public class QueryReWriter {

    private final QueryTransformer queryTransformer;

    public QueryReWriter(ChatModel dashscopeChatModel){
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
    }

    /**
     * 执行查询重写
     * @param prompt
     * @return
     */
    public String doQueryRewrite(String prompt){
        String chinesePrompt = "请用中文进行查询重写: " + prompt;
        Query query = new Query(chinesePrompt);
        //执行查询重写
        Query transform = queryTransformer.transform(query);
        //执行重写后的查询
        return transform.text();
    }
}
