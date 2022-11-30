package com.peoples.api.dto.response;

import com.peoples.api.domain.StudyMember;
import com.peoples.api.domain.User;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@Getter
@Builder
public class DeleteUserResponse {
    private boolean result;
    private List<StudyResponse> studyMemberList;
}
