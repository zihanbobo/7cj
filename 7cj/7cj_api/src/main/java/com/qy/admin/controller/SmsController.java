package com.qy.admin.controller;

import com.qy.admin.service.SmsService;
import com.qy.base.core.*;
import com.qy.base.utils.DateUtil;
import com.qy.base.utils.RongYunSMS;
import com.qy.model.Sms;
//import com.github.pagehelper.PageHelper;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Condition;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by zaq on 2018/03/12.
 */
@RestController
@RequestMapping("/sys/sms")
public class SmsController {
    @Resource
    private SmsService smsService;

    /**
     * 发送短信
     */
    @RequestMapping("/sendSMS")
    public Result sendSMS(String phone) {

        Sms sms =new Sms();
        sms.setPhone(phone);
        Random random = new Random();
        String content = String.valueOf(random.nextInt(9999) % (9999 - 1000 + 1) + 1000);
        Boolean result = RongYunSMS.sendSMS(sms.getPhone(), "263611", content);
        if (result) {
            sms.setPhone(sms.getPhone());
            sms.setCode(content);
            sms.setAdd_time(DateUtil.getNowTimestamp());
            smsService.save(sms);
            return ResultGenerator.successResult();
        } else {
            return ResultGenerator.errResult(Constants.FAIL);
        }
    }

    /**
     * 检查短信
     */
    @RequestMapping("/checkSMS")
    public Result checkSMS(String phone,String code) throws Exception {

        Sms sms = new Sms();
        sms.setCode(code);
        sms.setPhone(phone);
        // 检查参数
        if (sms.getPhone().length() <= 0 || sms.getCode().length() <= 0) {
            return ResultGenerator.errResult(Constants.CODE_ERR_PARAM);
        }
        Condition condition = new Condition(Sms.class);
        Example.Criteria criteria = condition.createCriteria();
        criteria.andCondition("phone = " + sms.getPhone());
        criteria.andCondition("code = " + sms.getCode());
        List<Sms> smsList = smsService.findByCondition(condition);
        Boolean isFalse = false;
        for (Sms aSmsList : smsList) {
            if (new Date().getTime() - Long.parseLong(aSmsList.getAdd_time()) < 1000 * 600) {
                isFalse = true;
            }
        }
        if (isFalse) {
            return ResultGenerator.successResult();
        } else {
            return ResultGenerator.errResult(Constants.CODE_ERR_SMS_INVALID);
        }
    }
}
