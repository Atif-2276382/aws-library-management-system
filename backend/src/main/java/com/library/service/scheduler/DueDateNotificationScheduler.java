package com.library.service.scheduler;

import com.library.dto.NotificationRequest;
import com.library.entity.Lending;
import com.library.repository.LendingRepository;
import com.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DueDateNotificationScheduler {

    private final LendingRepository
            lendingRepository;

    private final NotificationService
            notificationService;

    @Scheduled(cron = "0 * * * * *")
    public void checkOverdueBooks() {

        log.info(
                "Checking overdue books...");

        List<Lending> overdueBooks =
                lendingRepository
                        .findByDueDateBeforeAndReturnDateIsNull(
                                LocalDate.now());

        overdueBooks.forEach(lending -> {

            NotificationRequest request =
                    new NotificationRequest();

            request.setRecipient(
                    lending.getMember()
                            .getName());

            request.setSubject(
                    "Book Overdue Alert");

            request.setMessage(
                    "Your book '"
                            + lending.getBook()
                            .getTitle()
                            + "' is overdue.");

            notificationService
                    .sendNotification(
                            request);
        });
    }
}