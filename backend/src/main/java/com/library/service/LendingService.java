package com.library.service;

import com.library.dto.IssueBookRequest;
import com.library.entity.Lending;

import java.util.List;

public interface LendingService {

    Lending issueBook(
            IssueBookRequest request);

    Lending returnBook(Long lendingId);

    List<Lending> getAllLendings();

    Lending getLendingById(Long id);

}