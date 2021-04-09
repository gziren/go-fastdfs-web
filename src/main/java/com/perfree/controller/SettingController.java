package com.perfree.controller;

import cn.hutool.core.util.StrUtil;
import com.perfree.common.ResponseBean;
import com.perfree.model.User;
import com.perfree.service.SettingService;
import com.perfree.service.UserService;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.regex.Pattern;

/**
 * 系统设置Controller
 */
@Controller
public class SettingController extends BaseController {

    @Autowired
    private SettingService settingService;
    @Autowired
    private UserService userService;

    /**
     * 个人资料
     *
     * @return String
     */
    @RequestMapping("/settings/user")
    public String user(Model model) {
        model.addAttribute("user", userService.getById(getUser().getId()));
        return "settings/user";
    }

    /**
     * 修改个人资料(垃圾代码/有空再改~)
     *
     * @return AjaxResult
     */
    @RequestMapping("/settings/editUser")
    @ResponseBody
    public ResponseBean editUser(User user, String oldPassword, String newPassword) {
        if (StrUtil.isBlank(user.getName()) || user.getName().length() > 100) {
            return ResponseBean.fail("昵称不能为空且在100字符以内");
        }
        String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern regex = Pattern.compile(check);
        if (StrUtil.isBlank(user.getEmail()) || !regex.matcher(user.getEmail()).matches()) {
            return ResponseBean.fail("邮箱不能为空且格式必须正确");
        }
        if (StrUtil.isNotBlank(newPassword)) {
            if (StrUtil.isBlank(oldPassword) || oldPassword.length() > 16) {
                return ResponseBean.fail("请输入原密码");
            }
            if (newPassword.length() > 16 || newPassword.length() < 6) {
                return ResponseBean.fail("新密码必须在6-16字符之间");
            }
            User userResult = userService.getById(user.getId());
            if (userResult.getPassword().equals(new Md5Hash(oldPassword, userResult.getCredentialsSalt()).toString())) {
                user.setPassword(new Md5Hash(newPassword, userResult.getCredentialsSalt()).toString());
            } else {
                return ResponseBean.fail("原密码错误");
            }
        }
        if (settingService.editUser(user)) {
            return ResponseBean.success();
        }
        return ResponseBean.fail("修改失败");
    }
}
