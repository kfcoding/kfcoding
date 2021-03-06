package com.cuiyun.kfcoding.rest.modular.common.controller;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.cuiyun.kfcoding.core.base.tips.ErrorTip;
import com.cuiyun.kfcoding.core.base.tips.SuccessTip;
import com.cuiyun.kfcoding.core.base.tips.Tip;
import com.cuiyun.kfcoding.core.exception.KfCodingException;
import com.cuiyun.kfcoding.rest.common.annotion.BussinessLog;
import com.cuiyun.kfcoding.rest.common.annotion.Permission;
import com.cuiyun.kfcoding.rest.common.exception.BizExceptionEnum;
import com.cuiyun.kfcoding.rest.modular.base.controller.BaseController;
import com.cuiyun.kfcoding.rest.modular.common.enums.WorkspaceTypeEnum;
import com.cuiyun.kfcoding.rest.modular.common.model.User;
import com.cuiyun.kfcoding.rest.modular.common.model.Workspace;
import com.cuiyun.kfcoding.rest.modular.common.service.IWorkspaceService;
import com.cuiyun.kfcoding.rest.modular.course.model.Submission;
import com.cuiyun.kfcoding.rest.modular.course.service.ISubmissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: kfcoding
 * @description: 工作空间控制层
 * @author: maple
 * @create: 2018-07-01 19:45
 **/
@RestController
@RequestMapping("/workspaces")
@CrossOrigin(origins = "*")
@Api(description = "工作空间相关接口")
public class WorkspaceController extends BaseController {

    @Value("${kfcoding.workspace.deleteUrl}")
    private String deleteUrl;

    @Value("${kfcoding.workspace.createUrl}")
    private String createUrl;

    @Value("${kfcoding.workspace.release}")
    private String workSpaceRelease;

    @Value("${kfcoding.workspace.startUrl}")
    private String startUrl;

    @Value("${kfcoding.workspace.keepUrl}")
    private String keepUrl;

    @Autowired
    IWorkspaceService workspaceService;

    @Autowired
    ISubmissionService submissionService;


    @ResponseBody
    @BussinessLog(value = "创建工作空间")
    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = "工作空间", notes = "创建工作空间")
    @Permission
    public Tip create(@RequestBody Workspace workspace) throws UnsupportedEncodingException {
        Workspace targetWorkspace = createWorkSpace(workspace);
        if (targetWorkspace != null) {
            MAP = new HashMap<>();
            MAP.put("workspace", targetWorkspace);
            SUCCESSTIP.setResult(MAP);
            return SUCCESSTIP;
        } else {
            throw new KfCodingException(BizExceptionEnum.WORKSPACE_CREATE_ERROR);
        }
    }

    @ResponseBody
    @RequestMapping(path = "/terminal" ,method = RequestMethod.POST)
    @ApiOperation(value = "创建terminal工作空间工作空间", notes = "创建terminal工作空间")
    public Tip createTerminal(@RequestBody Workspace workspace) throws UnsupportedEncodingException {
        Map params = new HashMap();
        params.put("image", workspace.getImage());
        params.put("type", workspace.getType());

        String result = this.initHttp(createUrl, HttpMethod.POST).body(JSON.toJSONString(params)).execute().body();
        Map resultMap = JSON.parseObject(result);
        if (resultMap.containsKey("error")){
            System.out.println(resultMap.get("error"));
            throw new KfCodingException(BizExceptionEnum.WORKSPACE_SERVER);
        }
        // 若类型是ternimal不插入数据库
        if (workspace.getType().equals(WorkspaceTypeEnum.TERMINAL.getValue())) {
            Map data = (Map) resultMap.get("data");
            workspace.setContainerName((String) data.get("name"));
            workspace.setWsaddr((String) data.get("url"));
        } else
            throw new KfCodingException(BizExceptionEnum.WORKSPACE_TYPE);
        MAP = new HashMap<>();
        MAP.put("workspace", workspace);
        SUCCESSTIP.setResult(MAP);
        return SUCCESSTIP;
    }

    private HttpRequest initHttp(String url , HttpMethod method) {
        String encoding = Base64Encoder.encode("admin:admin");
        if (method == HttpMethod.POST) {
            return HttpRequest.post(url).header(Header.CONTENT_TYPE, "application/json").header("Authorization", "Basic " + encoding);
        } else if (method == HttpMethod.GET) {
            return HttpRequest.get(url).header(Header.CONTENT_TYPE, "application/json").header("Authorization", "Basic " + encoding);
        } else if (method == HttpMethod.DELETE) {
            return HttpRequest.delete(url).header(Header.CONTENT_TYPE, "application/json").header("Authorization", "Basic " + encoding);
        }
        return null;
    }

    public Workspace createWorkSpace(Workspace workspace) throws UnsupportedEncodingException {

        Map params = new HashMap();
        params.put("image", workspace.getImage());
        params.put("type", workspace.getType());
//        HttpResult httpResult = HttpKit.postResult(createUrl, JSON.toJSONString(params), header);
//        String result = httpResult.getResult();
//        String url
        System.err.println(JSON.toJSONString(params));
        String result = this.initHttp(createUrl, HttpMethod.POST).body(JSON.toJSONString(params)).execute().body();
        Map resultMap = JSON.parseObject(result);
        if (resultMap.containsKey("error")){
            System.out.println(resultMap.get("error"));
            throw new KfCodingException(BizExceptionEnum.WORKSPACE_SERVER);
        }

        // 若类型是ternimal不插入数据库
        if (workspace.getType().equals(WorkspaceTypeEnum.TERMINAL.getValue())){
            throw new KfCodingException(BizExceptionEnum.WORKSPACE_TYPE);
        } else {
            User user = getUser();
            workspace.setUserId(user.getId());
            workspace.setCreateTime(new Date());
            workspace.setRelease(workSpaceRelease);
            workspace.setContainerName((String) resultMap.get("data"));
            if (!workspaceService.insert(workspace)) {
                throw new KfCodingException(BizExceptionEnum.WORKSPACE_CREATE_ERROR);
            }
        }
        return workspace;
    }


    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    @Permission
    @ApiOperation(value = "获取workspace", notes = "获取当前用户的所有workspace")
    public SuccessTip getWorkspaces() {
        User user = getUser();
        SUCCESSTIP = new SuccessTip();
        MAP = new HashMap<>();

        List workspaces = workspaceService.getWorkspacesByUserId(user.getId());
        MAP.put("workspaces", workspaces);

        SUCCESSTIP.setResult(MAP);
        return SUCCESSTIP;
    }

    @ResponseBody
    @RequestMapping(path = "/{id}/start", method = RequestMethod.GET)
    @ApiOperation(value = "开启工作空间", notes = "开启工作空间")
    public Tip start(@PathVariable String id) {
        MAP = new HashMap<>();
        Workspace workspace = workspaceService.selectById(id);
        if (workspace != null) {
//            Map params = new HashMap();
//            params.put("name", workspace.getContainerName());
            String url = this.startUrl + workspace.getContainerName() + "/" + workspace.getType();
            String result = initHttp(url, HttpMethod.GET).execute().body();
            Map resultMap = (Map) JSON.parse(result);
            if (resultMap.containsKey("error"))
                return new ErrorTip(BizExceptionEnum.WORKSPACE_SERVER.getCode(), BizExceptionEnum.WORKSPACE_SERVER.getMessage());
//            StringBuffer sb = new StringBuffer();
//            sb.append("http://").append(workspace.getContainerName()).append(".workspace.cloudwarehub.com");
            workspace.setWsaddr(resultMap.get("data"));
            Submission submission = submissionService.selectOne(new EntityWrapper<Submission>().eq("workspace_id", id));
            if (submission != null){
                MAP.put("submission", submission);
            }
            MAP.put("workspace", workspace);
            SUCCESSTIP.setResult(MAP);
            return SUCCESSTIP;
        } else {
            throw new KfCodingException(BizExceptionEnum.WORKSPACE_NULL);
        }
    }

    @ResponseBody
    @BussinessLog(value = "删除工作空间")
    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
    @ApiOperation(value = "删除工作空间", notes = "删除工作空间")
    @Permission
    @Transactional
    public SuccessTip delete(@PathVariable String id) throws UnsupportedEncodingException {
        Workspace workspace = workspaceService.selectById(id);
        if (workspace == null)
            throw new KfCodingException(BizExceptionEnum.WORKSPACE_NULL);
        if (!workspaceService.deleteById(id))
            throw new KfCodingException(BizExceptionEnum.WORKSPACE_DELETE);
//        Map<String, String> param = new HashMap<String, String>();
//        param.put("name", id);
//        HttpKit.post(deleteUrl, param);
        String url = this.deleteUrl + workspace.getContainerName() + "/" + workspace.getType();
        HttpResponse httpResponse =  initHttp(url, HttpMethod.DELETE).execute();
        if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
            throw new KfCodingException(BizExceptionEnum.WORKSPACE_DELETE);
        }
        SUCCESSTIP = new SuccessTip();
        return SUCCESSTIP;
    }

    @ResponseBody
    @RequestMapping(path = "/keep", method = RequestMethod.GET)
    @ApiOperation(value = "心跳", notes = "")
    @Permission
    public Tip keep(@RequestParam String containerName , @RequestParam String type) {
        String url = this.keepUrl + containerName + "/" + type;
        String result = initHttp(url, HttpMethod.GET).execute().body();
        Map resultMap = (Map) JSON.parse(result);
        if (resultMap.containsKey("error"))
            return new ErrorTip(BizExceptionEnum.WORKSPACE_SERVER.getCode(), BizExceptionEnum.WORKSPACE_SERVER.getMessage());
        return new SuccessTip();
    }

    @ResponseBody
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "工作空间", notes = "获取工作空间")
    public SuccessTip create(@PathVariable String id) {
        Workspace workspace = workspaceService.selectById(id);
        MAP = new HashMap<>();
        SUCCESSTIP = new SuccessTip();
        if (workspace != null) {
            Submission submission = submissionService.selectOne(new EntityWrapper<Submission>().eq("workspace_id", id));
            if (submission != null){
                MAP.put("submission", submission);
            }
            MAP.put("workspace", workspace);
            SUCCESSTIP.setResult(MAP);
            return SUCCESSTIP;
        } else {
            throw new KfCodingException(BizExceptionEnum.WORKSPACE_NULL);
        }
    }

}
