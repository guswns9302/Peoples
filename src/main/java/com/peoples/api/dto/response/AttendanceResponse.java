package com.peoples.api.dto.response;

import com.peoples.api.domain.Study;
import com.peoples.api.domain.enumeration.AttendStatus;
import com.peoples.api.domain.enumeration.StudyCategory;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {
    private String userId;
    private AttendStatus attendStatus;
    private String reason;
    private int fine;
}
