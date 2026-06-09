package by.urbash_hair.controller;

import by.urbash_hair.entity.Post;
import by.urbash_hair.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@CrossOrigin
public class PostController {

    private final PostService service;

    @GetMapping
    public List<Post> getAll() {
        return service.getAll();
    }
}
