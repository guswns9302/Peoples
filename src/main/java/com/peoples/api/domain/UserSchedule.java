package com.peoples.api.domain;

import com.peoples.api.domain.enumeration.Status;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_USER_SCHEDULE")
public class UserSchedule {

    @Id
    @Column(name = "SCHEDULE_ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "SCHEDULE_NAME")
    private String scheduleName;

    @Column(name = "SCHEDULE_DATE")
    private LocalDate scheduleDate;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private Status status;

    public void updateStatus(Status status){
        this.status = status;
    }

    public void updateName(String scheduleName){
        this.scheduleName = scheduleName;
    }
}
