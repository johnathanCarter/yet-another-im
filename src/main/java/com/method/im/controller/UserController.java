package com.method.im.controller;

import com.method.im.pojo.ChatMsg;
import com.method.im.pojo.User;
import com.method.im.pojo.bo.UserBo;
import com.method.im.pojo.enums.OperateTypeEnum;
import com.method.im.pojo.enums.SearchFriendsStatusEnum;
import com.method.im.pojo.vo.FriendListVo;
import com.method.im.pojo.vo.UserVo;
import com.method.im.service.UserService;
import com.method.im.utils.MyJSONResult;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    /**
     * 登录或注册
     * @param user
     * @return
     * @throws Exception
     */
    @PostMapping("/loginOrRegister")
    public MyJSONResult loginOrRegister(@RequestBody User user) throws Exception {

        if (StringUtils.isEmpty(user.getUsername()) ||
            StringUtils.isEmpty(user.getPassword())) {
            return MyJSONResult.errorMsg("用户名或密码不能为空");
        }

        boolean isSuccess = false;
        User mUser = null;
        if (userService.checkUserName(user.getUsername())) {
            log.info("===========user login==========");
            isSuccess = userService.login(user.getUsername(), user.getPassword());
            if (!isSuccess) {
                System.out.println("============= " + "blocked");
                return MyJSONResult.errorMsg("用户名或密码错误");
            } else {
                System.out.println("============= " + "pass");
                mUser = userService.selectUserByName(user.getUsername());
            }
        } else {
            log.info("========add new user=======");
            mUser = userService.saveUser(user);
        }

        UserVo userVo = new UserVo();
        if (mUser != null) {
            BeanUtils.copyProperties(mUser, userVo);
        }
        log.info("username: " + userVo.getUsername());
        return MyJSONResult.ok(userVo);
    }

    /**
     * 上传头像
     * @param userBo
     * @return
     */
    @PostMapping("/uploadFaceBase64")
    public MyJSONResult upload(@RequestBody UserBo userBo) {
        String url = userService.uploadFile(userBo);
        boolean result = userService.updateImageUrl(userBo, url);
        User user = userService.queryUserInfo(userBo.getUserId());
        if (result) {
            return MyJSONResult.ok(user);
        } else return MyJSONResult.errorMsg("upload filed");
    }

    /**
     * 设置昵称
     * @param userBo
     * @return
     */
    @PostMapping("/setNickname")
    public MyJSONResult setName(@RequestBody UserBo userBo) {
        User user = userService.updateNickname(userBo);
        return MyJSONResult.ok(user);
    }

    /**
     * 查找用户
     * @param myId
     * @param friendUsername
     * @return
     */
    @PostMapping("/searchFriends")
    public MyJSONResult searchFriends(String myId, String friendUsername) {
        log.info("===========search friends===========");
        // 1.是否为空
        // 2.是否存在
        // 3.是否为本身
        // 4.是否已添加
        Integer status;
        if (StringUtils.isEmpty(myId) || StringUtils.isEmpty(friendUsername)) {
            log.info("null");
            return MyJSONResult.errorMsg("");
        } else if (!(status = userService.ifExist(friendUsername)).equals(SearchFriendsStatusEnum.SUCCESS.status)) {
            log.info(SearchFriendsStatusEnum.getMsgByKey(status));
            return MyJSONResult.errorMsg(SearchFriendsStatusEnum.getMsgByKey(status));
        } else if (!(status = userService.ifItself(myId, friendUsername)).equals(SearchFriendsStatusEnum.SUCCESS.status)) {
            log.info(SearchFriendsStatusEnum.getMsgByKey(status));
            return MyJSONResult.errorMsg(SearchFriendsStatusEnum.getMsgByKey(status));
        } else if (!(status = userService.ifAdded(myId, friendUsername)).equals(SearchFriendsStatusEnum.SUCCESS.status)) {
            log.info(SearchFriendsStatusEnum.getMsgByKey(status));
            return MyJSONResult.errorMsg(SearchFriendsStatusEnum.getMsgByKey(status));
        } else {
            log.info(SearchFriendsStatusEnum.getMsgByKey(SearchFriendsStatusEnum.SUCCESS.status));
            return MyJSONResult.ok(userService.canAdd(friendUsername));
        }
    }

    /**
     * 发送好友申请
     * @param myId
     * @param friendUsername
     * @return
     */
    @PostMapping("/sendFriendRequest")
    public MyJSONResult sendFriendRequest(String myId, String friendUsername) {
        log.info("===========send friend request===========");
        // 再次确认一次,防止直接跳过searchFriends接口
        // 1.是否为空
        // 2.是否存在
        // 3.是否为本身
        // 4.是否已添加
        Integer status;
        if (StringUtils.isEmpty(myId) || StringUtils.isEmpty(friendUsername)) {
            log.info("null");
            return MyJSONResult.errorMsg("");
        } else if (!(status = userService.ifExist(friendUsername)).equals(SearchFriendsStatusEnum.SUCCESS.status)) {
            log.info(SearchFriendsStatusEnum.getMsgByKey(status));
            return MyJSONResult.errorMsg(SearchFriendsStatusEnum.getMsgByKey(status));
        } else if (!(status = userService.ifItself(myId, friendUsername)).equals(SearchFriendsStatusEnum.SUCCESS.status)) {
            log.info(SearchFriendsStatusEnum.getMsgByKey(status));
            return MyJSONResult.errorMsg(SearchFriendsStatusEnum.getMsgByKey(status));
        } else if (!(status = userService.ifAdded(myId, friendUsername)).equals(SearchFriendsStatusEnum.SUCCESS.status)) {
            log.info(SearchFriendsStatusEnum.getMsgByKey(status));
            return MyJSONResult.errorMsg(SearchFriendsStatusEnum.getMsgByKey(status));
        } else {
            log.info(SearchFriendsStatusEnum.getMsgByKey(SearchFriendsStatusEnum.SUCCESS.status));
            userService.sendFriendRequest(myId, friendUsername);
            return MyJSONResult.ok();
        }
    }

    /**
     * 获取好友申请列表
     * @param acceptUserId
     * @return
     */
    @PostMapping("/friendRequestList")
    public MyJSONResult friendRequestList(String acceptUserId) {
        return MyJSONResult.ok(userService.queryFriendRequestList(acceptUserId));
    }

    /**
     * 操作好友申请
     * @param acceptUserId
     * @param sendUserId
     * @param type
     * @return
     */
    @PostMapping("/operateFriendRequest")
    public MyJSONResult operateFriendRequest(String acceptUserId, String sendUserId, Integer type) {
        // 1.检查三个值是否有空
        // 2.分别处理忽略和通过的情况
        //   通过,双向写入my_friends数据库,并且删除friends_request数据库中的相应记录
        //   忽略,删除friends_request数据库中的相应记录
        if (StringUtils.isEmpty(acceptUserId) || StringUtils.isEmpty(sendUserId) || StringUtils.isEmpty(Integer.toString(type))) { // TODO fix it soon
            return MyJSONResult.errorMsg("error");
        }
        if (StringUtils.isEmpty(OperateTypeEnum.getMsgByType(type))) {
            return MyJSONResult.errorMsg("error");
        }

        if (OperateTypeEnum.IGNORE.type.equals(type)) {
            log.info("===========ignored===========");
            userService.ignoreRequest(acceptUserId, sendUserId);
        }
        if (OperateTypeEnum.PASS.type.equals(type)) {
            log.info("===========passed===========");
            userService.passRequest(acceptUserId, sendUserId);
        }

        return MyJSONResult.ok();
    }

    /**
     * 获取好友列表
     * @param myUserId
     * @return
     */
    @PostMapping("/queryFriendList")
    public MyJSONResult queryFriendList(String myUserId) {
        log.info("===========queryFriendList===========");
        List<FriendListVo> friendList = userService.queryFriendList(myUserId);
        log.info(friendList.toString());
        return MyJSONResult.ok(friendList);
    }

    /**
     * 获取手机端未签收消息列表
     * @param acceptUserId
     * @return
     */
    @PostMapping("/getUnreadMsgList")
    public MyJSONResult getUnreadMsgList(String acceptUserId) {
        List<ChatMsg> unreadMsgList = userService.getUnreadMsgList(acceptUserId);
        return MyJSONResult.ok(unreadMsgList);
    }
}
