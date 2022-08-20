package com.method.im.service;

import com.method.im.component.netty.MsgBean;
import com.method.im.pojo.ChatMsg;
import com.method.im.pojo.User;
import com.method.im.pojo.bo.UserBo;
import com.method.im.pojo.vo.FriendListVo;
import com.method.im.pojo.vo.FriendRequestVo;
import com.method.im.pojo.vo.UserVo;

import java.util.List;

public interface UserService {

    boolean checkUserName(String username);

    Boolean login(String username, String psw);

    User saveUser(User user);

    String uploadFile(UserBo userBo);

    boolean updateImageUrl(UserBo userBO, String url);

    User queryUserInfo(String id);

    User updateNickname(UserBo userBo);

    Integer ifExist(String username);

    Integer ifItself(String userId, String username);

    Integer ifAdded(String myId, String username);

    User selectUserByName(String username);

    UserVo canAdd(String name);

    void sendFriendRequest(String myId, String friendName);

    List<FriendRequestVo> queryFriendRequestList(String acceptUserId);

    void ignoreRequest(String userId, String friendId);

    void passRequest(String userId, String friendId);

    List<FriendListVo> queryFriendList(String userId);

    String saveMsg(MsgBean msgBean);

    void updateSighedMsg(List<String> msgIdList);

    List<ChatMsg> getUnreadMsgList(String acceptUserId);

    default void get(){

    }

}
