package com.openkg.openbase.web;

import com.openkg.openbase.Manage.ReviewFactory;
import com.openkg.openbase.common.AuthorizationOperation;
import com.openkg.openbase.common.APIPermissions;
import com.openkg.openbase.constant.Msg;
import com.openkg.openbase.model.Page;
import com.openkg.openbase.model.PaginationResult;
import com.openkg.openbase.model.Res;
import com.openkg.openbase.model.Token;
import com.openkg.openbase.service.HttpClientService;
import com.openkg.openbase.service.ReviewService;
import com.openkg.openbase.service.ViewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@Api(description = "openbase review api", tags = "review api",
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@RestController
@RequestMapping(value = "viewKG")
@CrossOrigin
public class ViewController {
    private HttpClientService httpClient;

    @Autowired
    public void setHttpClient(HttpClientService httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * 图谱浏览 --> 图谱展示
     */
    @ApiOperation(value = "specialView")
    @RequestMapping(value = "specialView", method = RequestMethod.GET)
    public Res getTask(@RequestParam("token") String token, @RequestParam("masterScholar") String masterScholar, @RequestParam("masterField") String masterField) {
        Res res = new Res();
        /*if (token == null || token.equals("")) {
            res.setCode(Msg.FAILED.getCode());
            res.setMsg("请求参数错误");
            return res;
        }
        //权限验证
        res = AuthOperation.checkAuth(token, Authority.VIEWKG);
        if (res.getCode() != Msg.SUCCESS.getCode()) {
            return res;
        }*/
        String sc = masterScholar;
        if (masterScholar == null || masterScholar.equals("")) {
            sc = null;
        }
        String fi = masterField;
        if (masterField == null || masterField.equals("")) {
            fi = null;
        }

        //领取任务, 返回任务数据
        Map data = null;
        try {
            data = ViewService.getMongoKG(sc,fi,"kg4ai");
        } catch (Exception e) {
            res.setCode(Msg.FAILED.getCode());
            res.setMsg("未获取到该id的学者信息");
            res.setToken(token);
            return res;
        }

        if (data.size() == 0) {
            res.setCode(Msg.FAILED.getCode());
            res.setMsg("任务出错");
            res.setToken(token);
        } else {
            res.setCode(Msg.SUCCESS.getCode());
            res.setMsg("领取成功");
            res.setToken(token);
            res.setData(data);
        }
        return res;
    }

    /**
     * 图谱浏览 --> 列表展示
     */
    @ApiOperation(value = "entityname")
    @RequestMapping(value = "entityname", method = RequestMethod.GET)
    public Res getEntityByName(@RequestParam("token") String token,
    		@RequestParam(value = "entity", required=false) String entityName, @RequestParam("source") String source,
    		Integer pageSize, Integer pageIndex) {
        Res res = new Res();
        /*if (token == null || token.equals("")) {
            res.setCode(Msg.FAILED.getCode());
            res.setMsg("请求参数错误");
            return res;
        }
        //权限验证
        res = AuthOperation.checkAuth(token, Authority.VIEWKG);
        if (res.getCode() != Msg.SUCCESS.getCode()) {
            return res;
        }*/
        //领取任务, 返回任务数据
        Map data = null;
        if (pageSize == null || pageSize <= 0) {
        		pageSize = Page.DEFAULT_PAGE_SIZE;
        }
        if (pageIndex == null || pageIndex < 1) {
        		pageIndex = 1;
        }
        Page page = new Page(pageSize, pageIndex, 0);
        try {
            data = ViewService.getEntityByName(entityName, page, source);

            // http://113.31.104.113:8080/api/v1/
            // 在这里插入 post /api/v1/honor-point接口: user_id
            // 可以用 get /api/v1/honor-point接口来验证: user_id, data_id
            //获取用户id
            Token tokenvalue = Token.fromCache(token);
            String user_id = tokenvalue.getUser_id();
            System.out.println("getEntityByName: user_id = " + user_id);
            List<Map> arraylist = (List)(data.get("RetrievedEntities"));
            String data_id = (String) ((arraylist.get(0)).get("@id"));
            System.out.println("data_id = " + data_id);
            Map<String, Object> parameter = new HashMap<String, Object>();
            // post请求生成荣誉值
            parameter.put("userId", user_id);
            parameter.put("amount", 10);
            parameter.put("dataId", data_id);
            parameter.put("version", "");
            HttpClientService.HttpResponse response_http = httpClient.doPost("http://113.31.104.113:8080/api/v1/honor-point", parameter);
            System.out.println("post response = " + response_http.getBody());
            // get请求得到荣誉值
            String get_url = "http://113.31.104.113:8080/api/v1/honor-point?userId=" + user_id;
            String response_str = httpClient.doGet(get_url);
            System.out.println("get response = " + response_str);


        } catch (Exception e) {
            res.setCode(Msg.EXCEPTION.getCode());
            res.setMsg(Msg.EXCEPTION.getMsg());
            res.setToken(token);
            return res;
        }
        if (data.size() == 0) {
            res.setCode(Msg.FAILED.getCode());
            res.setMsg("未查询到学者信息");
            res.setToken(token);
        } else {
            res.setCode(Msg.SUCCESS.getCode());
            res.setMsg(Msg.SUCCESS.getMsg());
            res.setToken(token);
            //PaginationResult<Map> resultWithPage = new PaginationResult<Map>(data, page);
            res.setData(data);
            res.setPage(page);
        }
        return res;
    }

    /**
     * 图谱浏览 --> 列表页面点击后的详情页面
     */
    @ApiOperation(value = "entityId")
    @RequestMapping(value = "entityId", method = RequestMethod.GET)
    public Res getEntityById(@RequestParam("token") String token, @RequestParam("entityId") String entId) {
        Res res = new Res();
        /*if (token == null || token.equals("")) {
            res.setCode(Msg.FAILED.getCode());
            res.setMsg("请求参数错误");
            return res;
        }
        //权限验证
        res = AuthOperation.checkAuth(token, Authority.VIEWKG);
        if (res.getCode() != Msg.SUCCESS.getCode()) {
            return res;
        }*/

        //领取任务, 返回任务数据
        List<Map> data = null;
        try {
            data = ViewService.getEntityById(entId);
        } catch (Exception e) {
            res.setCode(Msg.FAILED.getCode());
            res.setMsg("未获取到该id的学者信息");
            res.setToken(token);
            return res;
        }
        if (data.size() == 0) {
            res.setCode(Msg.FAILED.getCode());
            res.setMsg("任务出错");
            res.setToken(token);
        } else {
            res.setCode(Msg.SUCCESS.getCode());
            res.setMsg("领取成功");
            res.setToken(token);
            res.setData(data);
        }
        return res;
    }


}
