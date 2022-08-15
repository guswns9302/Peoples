package com.peoples.api.service.responseMap;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ResponseMap {

    public <T> Map<String,Object> responseMap(String message, T result){
        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put("timestamp", LocalDateTime.now());
        responseMap.put("message", message);
        responseMap.put("result", result);
        return responseMap;
    }
}
