package com.mynotes.contentcenter.sentinel;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: 乔童
 * @Description: 自定义错误页
 * @Date: 2020/05/04 11:27
 * @Version: 1.0
 */
@Component
public class CustomBlockException implements BlockExceptionHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        BlockStatus blockStatus = null;
        if (e instanceof AuthorityException) {

            blockStatus = new BlockStatus("授权规则不通过", 100);

        } else if (e instanceof DegradeException) {

            blockStatus = new BlockStatus("被降级了", 101);

        } else if (e instanceof FlowException) {

            blockStatus = new BlockStatus("被限流了", 102);

        } else if (e instanceof SystemBlockException) {

            blockStatus = new BlockStatus("系统规则错误", 103);

        } else if (e instanceof ParamFlowException) {

            blockStatus = new BlockStatus("热点参数限流了", 104);

        }

        response.setStatus(500);
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-type", "application/json;charset=utf-8");
        response.setContentType("application/json;charset=utf-8");
        // spring mvc 自带的json操作工具，叫jackson
        new ObjectMapper().writeValue(response.getWriter(), blockStatus);
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class BlockStatus {
    private String message;
    private Integer status;
}