package com.peoples.api.service;

import com.peoples.api.service.responseMap.ResponseMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService extends ResponseMap {

    public Map<String,Object> sendEmail(String userId) {

        return super.responseMap("abc", true);
    }
}
