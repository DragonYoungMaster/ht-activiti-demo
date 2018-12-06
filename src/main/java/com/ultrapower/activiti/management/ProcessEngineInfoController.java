package com.ultrapower.activiti.management;

import com.google.common.collect.Maps;

import org.activiti.engine.ManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@RestController
@RequestMapping(value = "/management/engine")
public class ProcessEngineInfoController {

    @Autowired
    ManagementService managementService;

    @RequestMapping("")
    public Map<String, Object> info() {
        Map<String, Object> result = Maps.newHashMap();
        Map<String,String> engineProperties = managementService.getProperties();
        result.put("engineProperties", engineProperties);

        Map<String,String> systemProperties = new HashMap<String, String>();
        Properties systemProperties11 = System.getProperties();
        Set<Object> objects = systemProperties11.keySet();
        for (Object object : objects) {
            systemProperties.put(object.toString(), systemProperties11.get(object.toString()).toString());
        }
        result.put("systemProperties", systemProperties);
        return result;
    }

}
