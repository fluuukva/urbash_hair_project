package by.urbash_hair.controller;

import by.urbash_hair.entity.Review;
import by.urbash_hair.repository.ClientRepository;
import by.urbash_hair.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin
public class ReviewController {

    private final ReviewService service;
    private final ClientRepository clientRepository;

    @GetMapping
    public List<Review> getAll() {
        List<Review> all = service.getAll();
        return all.stream()
                .filter(r -> "APPROVED".equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    @PostMapping
    public void create(@RequestBody Review review) {
        if (review.getClient() != null && review.getClient().getId() != null) {
            var client = clientRepository.findById(review.getClient().getId());
            client.ifPresent(review::setClient);
        }
        review.setStatus("PENDING");
        service.create(review);
    }
}