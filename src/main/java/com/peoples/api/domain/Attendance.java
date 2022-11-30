package com.peoples.api.domain;

import com.peoples.api.domain.enumeration.AttendStatus;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_ATTENDANCE")
public class Attendance {

    @Id
    @Column(name = "TB_ATTENDANCE")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long attendanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEDULE_ID")
    private StudySchedule studySchedule;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "ATTENDANCE")
    @Enumerated(EnumType.STRING)
    private AttendStatus attendStatus;

    @Column(name = "REASON")
    private String reason;

    @Column(name = "FINE")
    private int fine;

    public void updateStatusAndFine(String attendStatus, int fine){
        this.fine = fine;
        this.attendStatus = AttendStatus.valueOf(attendStatus);
    }
}
