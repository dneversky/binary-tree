package com.dneversky.websocket;

import com.dneversky.service.Node;
import com.dneversky.service.TreeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MessageController {

    @Autowired
    private TreeManager treeManager;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @MessageMapping("/profile-insert")
    @SendTo("/process/insert-profile-result")
    public Double profileInsert(int size) {
        return treeManager.profileInsertIntoTree(size);
    }

    @MessageMapping("/elements")
    @SendTo("/process/elements")
    public List<Integer> elements() {
        return treeManager.getAvailableElements();
    }

    @MessageMapping("/profile-search")
    @SendTo("/process/profile-search")
    public Double profileSearch(int element) {
        return treeManager.profileSearchInTree(element);
    }

    @MessageMapping("/pull")
    @SendTo("/process/pull")
    public Node pull() {
        return treeManager.getActiveRoot();
    }

    @MessageMapping("/insert")
    public void insert(InsertRequest request) {
        switch (request.type()) {
            case "string" -> treeManager.getOrCreateTree(String.class).insert(request.value());
            case "int" -> treeManager.getOrCreateTree(Integer.class).insert(Integer.parseInt(request.value()));
            case "double" -> treeManager.getOrCreateTree(Double.class).insert(Double.parseDouble(request.value()));
            default -> throw new IllegalArgumentException("Unsupported type: " + request.type());
        }
    }

    @MessageMapping("/reset")
    public void reset() {
        treeManager.reset();
    }
}
