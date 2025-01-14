package dev.jianmu.api.controller;

import dev.jianmu.api.vo.WebhookResult;
import dev.jianmu.application.service.TriggerApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @class WebHookController
 * @description WebHookController
 * @author Ethan Liu
 * @create 2021-06-25 23:04
*/
@Controller
@RequestMapping("webhook")
@Tag(name = "WebHook API", description = "WebHook API")
public class WebHookController {
    private final TriggerApplication triggerApplication;

    public WebHookController(TriggerApplication triggerApplication) {
        this.triggerApplication = triggerApplication;
    }


    @RequestMapping(value = "/**", method = RequestMethod.POST, consumes = {"application/json", "application/x-www-form-urlencoded", "text/plain"})
    @ResponseBody
    @Operation(summary = "触发项目", description = "触发项目启动")
    public WebhookResult receivePostJsonEvent(
            HttpServletRequest request,
            @RequestHeader("Content-Type") String contentType
    ) {
        var path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        var bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        var apm = new AntPathMatcher();
        var projectName = apm.extractPathWithinPattern(bestMatchPattern, path);
        var decodeProjectName = URLDecoder.decode(projectName, StandardCharsets.UTF_8);
        var triggerEvent = this.triggerApplication.receiveHttpEvent(decodeProjectName, request, contentType);
        return WebhookResult.builder()
                .projectId(triggerEvent.getProjectId())
                .triggerId(triggerEvent.getId())
                .build();
    }
}
