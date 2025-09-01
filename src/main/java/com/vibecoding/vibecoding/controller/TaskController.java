package com.taskmanager.controller;

import com.taskmanager.model.Task;
import com.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/create-task")
    public String showCreateTaskForm(Model model) {
        model.addAttribute("task", new Task());
        return "create-task";
    }

    @PostMapping("/create-task")
    public String createTask(@Valid @ModelAttribute Task task,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        if (bindingResult.hasErrors()) {
            return "create-task";
        }

        try {
            taskService.createTask(task)
                    .doOnSuccess(result -> {
                        logger.info("Task created successfully on Trello: {}", task.getTitle());
                        redirectAttributes.addFlashAttribute("successMessage",
                                "Task '" + task.getTitle() + "' skapad på Trello!");
                    })
                    .doOnError(error -> {
                        logger.error("Error creating task: {}", error.getMessage());
                        redirectAttributes.addFlashAttribute("errorMessage",
                                "Fel vid skapande av task: " + error.getMessage());
                    })
                    .subscribe();

            return "redirect:/create-task";

        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage());
            model.addAttribute("errorMessage", "Oväntat fel uppstod");
            return "create-task";
        }
    }
}