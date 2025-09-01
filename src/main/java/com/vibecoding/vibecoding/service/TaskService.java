package com.taskmanager.service;

import com.taskmanager.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TaskService {

    @Autowired
    private TrelloService trelloService;

    public Mono<String> createTask(Task task) {
        return trelloService.createCard(task);
    }
}