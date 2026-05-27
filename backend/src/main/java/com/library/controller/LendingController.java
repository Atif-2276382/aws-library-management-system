package com.library.controller;

import com.library.dto.IssueBookRequest;
import com.library.entity.Lending;
import com.library.service.LendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lendings")
@RequiredArgsConstructor
public class LendingController {

    private final LendingService lendingService;

    @PostMapping("/issue")
    public Lending issueBook(
            @RequestBody
            IssueBookRequest request) {

        return lendingService
                .issueBook(request);
    }

    @PutMapping("/return/{id}")
    public Lending returnBook(
            @PathVariable Long id) {

        return lendingService
                .returnBook(id);
    }

    @GetMapping
    public List<Lending> getAllLendings() {

        return lendingService
                .getAllLendings();
    }

    @GetMapping("/{id}")
    public Lending getLendingById(
            @PathVariable Long id) {

        return lendingService
                .getLendingById(id);
    }
}