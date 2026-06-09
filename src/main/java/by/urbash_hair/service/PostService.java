package by.urbash_hair.service;

import by.urbash_hair.entity.Post;
import by.urbash_hair.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository repository;

    public List<Post> getAll() {
        return repository.findAll();
    }
}
