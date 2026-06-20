package com.library.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lendings")
public class Lending {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LendingID")
    private Integer lendingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookID", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MemberID", nullable = false)
    private Member member;

    @Column(name = "IssueDate", nullable = false)
    private LocalDateTime issueDate;

    @Column(name = "DueDate", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "ReturnDate")
    private LocalDateTime returnDate;

    public Integer getLendingId() {
        return lendingId;
    }

    public void setLendingId(Integer lendingId) {
        this.lendingId = lendingId;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public boolean isActive() {
        return returnDate == null;
    }
}
