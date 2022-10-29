package com.peoples.api.service.responseMap;

import com.peoples.api.domain.Study;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ResponseMap {

    public <T> Map<String,Object> responseMap(String message, T result){
        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        responseMap.put("message", message);
        responseMap.put("result", result);
        return responseMap;
    }
}
