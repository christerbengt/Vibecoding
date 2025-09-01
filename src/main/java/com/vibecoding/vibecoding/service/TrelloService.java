package com.taskmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
public class TrelloService {

    private static final Logger logger = LoggerFactory.getLogger(TrelloService.class);

    @Value("${api.trello.key}")
    private String trelloKey;

    @Value("${api.trello.token}")
    private String trelloToken;

    @Value("${api.trello.list-id}")
    private String listId;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public TrelloService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    public Mono<String> createCard(Task task) {
        try {
            String cardName = task.getTitle();

            // Lägg till prioritet i titel om det finns
            if (task.getPriority() != null && !task.getPriority().isEmpty()) {
                cardName = "[" + task.getPriority().toUpperCase() + "] " + cardName;
            }

            // Bygg description med extra info
            StringBuilder description = new StringBuilder();
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                description.append(task.getDescription());
            }

            if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
                if (description.length() > 0) {
                    description.append("\n\n");
                }
                description.append("Assignee: ").append(task.getAssignee());
            }

            String url = UriComponentsBuilder
                    .fromHttpUrl("https://api.trello.com/1/cards")
                    .queryParam("key", trelloKey)
                    .queryParam("token", trelloToken)
                    .queryParam("idList", listId)
                    .queryParam("name", cardName)
                    .queryParam("desc", description.toString())
                    .build()
                    .toUriString();

            return webClient.post()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> logger.info("Trello card created successfully: {}", cardName))
                    .doOnError(error -> logger.error("Error creating Trello card: {}", error.getMessage()));

        } catch (Exception e) {
            logger.error("Error preparing Trello request: {}", e.getMessage());
            return Mono.error(e);
        }
    }

    // Bonus: Hämta alla listor från boardet för konfiguration
    public Mono<String> getBoardLists(String boardId) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://api.trello.com/1/boards/" + boardId + "/lists")
                    .queryParam("key", trelloKey)
                    .queryParam("token", trelloToken)
                    .build()
                    .toUriString();

            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> logger.info("Board lists retrieved successfully"))
                    .doOnError(error -> logger.error("Error retrieving board lists: {}", error.getMessage()));

        } catch (Exception e) {
            logger.error("Error preparing board lists request: {}", e.getMessage());
            return Mono.error(e);
        }
    }
}