package by.urbash_hair.service;

import by.urbash_hair.entity.Review;
import by.urbash_hair.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository repository;

    public List<Review> getAll() {
        return repository.findAll();
    }

    public void create(Review review) {
        repository.save(review);
    }
}