package com.library.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MemberID")
    private Integer memberId;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Column(name = "MembershipID", nullable = false, unique = true, length = 20)
    private String membershipId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false, unique = true)
    private User user;

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(String membershipId) {
        this.membershipId = membershipId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
