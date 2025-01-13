package com.email.automation.service;

import com.email.automation.payload.EmailRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class EmailGeneratorService{

    private final WebClient webClient;

    @Value("${value.gemini.url}")
    private String geminiAPIUrl;

    @Value("${value.gemini.key}")
    private String geminiAPIKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateEmailReply(EmailRequest emailRequest){

        String prompt = buildPrompt(emailRequest);

        Map<String , Object> map = Map.of(
                "contents" , new Object[]{
                        Map.of("parts" , new Object[]{
                                Map.of("text" ,prompt)
                        })
                }
        );

        String response = webClient.post()
                .uri(geminiAPIUrl + geminiAPIKey)
                .header("Content-Type" ,"application/json")
                .bodyValue(map)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return responseContent(response);

    }

    private String responseContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);

            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e){
            return "exception while processing : " + e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailRequest){
        StringBuilder sb = new StringBuilder();
        sb.append("please write a professional email reply for the email .Don't include the subject line");

        if(emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()){
            sb.append("Keep the tone of the whole email ").append(emailRequest.getTone());
        }
        sb.append("\nOriginal email : \n").append(emailRequest.getEmailContent());

        return sb.toString();
    }
}
