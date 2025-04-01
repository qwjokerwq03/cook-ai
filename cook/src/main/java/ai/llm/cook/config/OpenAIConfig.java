package ai.llm.cook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenAIConfig {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    @Value("${openai.model}")
    private String openaiModel;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getOpenaiApiKey() {
        return openaiApiKey;
    }

    public String getOpenaiApiUrl() {
        return openaiApiUrl;
    }

    public String getOpenaiModel() {
        return openaiModel;
    }
}