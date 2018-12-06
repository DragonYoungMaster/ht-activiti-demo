package com.ultrapower.activiti.management;

import com.google.common.collect.Maps;

import com.ultrapower.activiti.util.Page;
import com.ultrapower.activiti.util.PageUtil;

import org.activiti.engine.ManagementService;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * 作业控制器
 */
@Controller
@RequestMapping(value = "/management/job")
public class JobController {

    @Autowired
    private ManagementService managementService;

    private static Map<String, String> JOB_TYPES = new HashMap<String, String>();

    static {
        JOB_TYPES.put("activate-processdefinition", "激活流程定义");
        JOB_TYPES.put("timer-intermediate-transition", "中间定时");
        JOB_TYPES.put("timer-transition", "边界定时");
        JOB_TYPES.put("timer-start-event", "定时启动流程");
        JOB_TYPES.put("suspend-processdefinition", "挂起流程定义");
        JOB_TYPES.put("async-continuation", "异步锁");
    }

    /**
     * Job列表
     */
    @RequestMapping(value = "list")
    public Map<String, Object> jobList(HttpServletRequest request) {
        JobQuery jobQuery = managementService.createJobQuery();

        Page<Job> page = new Page<>(PageUtil.PAGE_SIZE);
        int[] pageParams = PageUtil.init(page, request);
        List<Job> jobList = jobQuery.listPage(pageParams[0], pageParams[1]);

        page.setResult(jobList);
        page.setTotalCount(jobQuery.count());

        Map<String, String> exceptionStacktraces = new HashMap<String, String>();
        for (Job job : jobList) {
            if (StringUtils.isNotBlank(job.getExceptionMessage())) {
                exceptionStacktraces.put(job.getId(), managementService.getJobExceptionStacktrace(job.getId()));
            }
        }

        Map<String, Object> result = Maps.newHashMap();
        result.put("page", page);
        result.put("exceptionStacktraces", exceptionStacktraces);
        result.put("JOB_TYPES", JOB_TYPES);

        return result;
    }

    /**
     * 删除作业
     *
     * @param jobId 作业id
     */
    @RequestMapping(value = "delete/{jobId}")
    public String deleteJob(@PathVariable("jobId") String jobId) {
        managementService.deleteJob(jobId);
        return "redirect:/management/job/list";
    }

    /**
     * 执行作业
     *
     * @param jobId 作业id
     */
    @RequestMapping(value = "execute/{jobId}")
    public String executeJob(@PathVariable("jobId") String jobId) {
        managementService.executeJob(jobId);
        return "redirect:/management/job/list";
    }

    /**
     * 更改作业可重试次数
     *
     * @param jobId 作业id
     */
    @RequestMapping(value = "change/retries/{jobId}")
    public String changeRetries(@PathVariable("jobId") String jobId, @RequestParam("retries") int retries) {
        managementService.setJobRetries(jobId, retries);
        return "redirect:/management/job/list";
    }

    /**
     * 读取作业异常信息
     *
     * @param jobId 作业id
     */
    @RequestMapping(value = "stacktrace/{jobId}")
    @ResponseBody
    public String getJobExceptionStacktrace(@PathVariable("jobId") String jobId) {
        return managementService.getJobExceptionStacktrace(jobId);
    }

}
