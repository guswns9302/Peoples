package com.peoples.api.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "TB_VOTE_USER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteUser {

    @Id
    @Column(name = "ITEM_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "VOTE_START_AT")
    private LocalDateTime voteStartAt;

    @Column(name = "VOTE_END_AT")
    private LocalDateTime voteEndAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VOTE_ID")
    private Vote vote;
}
