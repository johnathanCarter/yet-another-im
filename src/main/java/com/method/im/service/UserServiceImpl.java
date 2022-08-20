package com.method.im.service;

import com.method.im.component.netty.DataContent;
import com.method.im.component.netty.MsgBean;
import com.method.im.component.netty.UserChannelRel;
import com.method.im.mapper.*;
import com.method.im.pojo.ChatMsg;
import com.method.im.pojo.Friend;
import com.method.im.pojo.FriendRequest;
import com.method.im.pojo.User;
import com.method.im.pojo.bo.UserBo;
import com.method.im.pojo.enums.MsgActionEnum;
import com.method.im.pojo.enums.MsgSignFlagEnum;
import com.method.im.pojo.enums.SearchFriendsStatusEnum;
import com.method.im.pojo.vo.FriendListVo;
import com.method.im.pojo.vo.FriendRequestVo;
import com.method.im.pojo.vo.UserVo;
import com.method.im.utils.*;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserMapperExtend userMapperExtend;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private FriendMapperExtend friendMapperExtend;

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    @Autowired
    private ChatMsgMapper chatMsgMapper;

    @Autowired
    private ChatMsgMapperExtend chatMsgMapperExtend;

    @Autowired
    private Sid sid;

    @Autowired
    private QRCodeUtils qrCodeUtils;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Override
    public boolean checkUserName(String username) {
        User user = new User();
        user.setUsername(username);
        User result = userMapper.selectOne(user);
        return result != null;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Boolean login(String username, String psw) {
        Example userExample = new Example(User.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username", username);
        User user = userMapper.selectOneByExample(userExample);
        boolean isSuccess = false;
        if (user != null) {
            isSuccess = MD5Utils.verify(psw, user.getPassword());
           System.out.println("psw: " + psw + " getPassword: " + user.getPassword() + " " + isSuccess);
        }
        return isSuccess;
    }

    /**
     * 保存新用户
     * @param user
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public User saveUser(User user) {
        try {
            user.setNickname(user.getUsername());
            user.setFaceImage("");
            user.setFaceImageBig("");
            user.setPassword(MD5Utils.generate(user.getPassword()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String id = sid.nextShort();
        user.setId(id);
        // 二维码生成与保存
        // my_qrcode:[username] 二维码格式
        String qrCodePath = "D://user" + id + "qrcode.png";
        qrCodeUtils.createQRCode(qrCodePath, "my_qrcode:" + user.getUsername());
        MultipartFile qeCodeFile = FileUtils.fileToMultipart(qrCodePath);
        String qrCodeUrl = "";
        try {
            assert qeCodeFile != null;
            qrCodeUrl = fastDFSClient.uploadQRCode(qeCodeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(qrCodePath);
        file.delete();
        user.setQrcode(qrCodeUrl);
        userMapper.insert(user);
        return user;
    }

    /**
     * 上传头像到服务器
     * @param userBo
     * @return
     */
    @Override
    public String uploadFile(UserBo userBo) {
        try {
            String base64Data = userBo.getFaceData();
            String path = "D:\\" + userBo.getUserId() + "profile_pic.png";
            FileUtils.base64ToFile(path, base64Data);
            MultipartFile multipartFile = FileUtils.fileToMultipart(path);
            String url = fastDFSClient.uploadBase64(multipartFile);
            File file = new File(path);
            file.delete();
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 保存头像路径到数据库
     * @param userBo
     * @param url
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public boolean updateImageUrl(UserBo userBo, String url) {
        if (userBo == null || url == null) return false;
        String thumbNail = "_80x80.";
        String[] split = url.split("\\.");
        String thumbNailUrl = split[0] + thumbNail + split[1];
        User user = new User();
        user.setId(userBo.getUserId());
        user.setFaceImage(thumbNailUrl);
        user.setFaceImageBig(url);
        userMapper.updateByPrimaryKeySelective(user);
        return true;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public User queryUserInfo(String id) {
        return userMapper.selectByPrimaryKey(id);
    }

    /**
     * 设置昵称
     * @param userBo
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public User updateNickname(UserBo userBo) {
        User user = new User();
        user.setId(userBo.getUserId());
        user.setNickname(userBo.getNickname());
        userMapper.updateByPrimaryKeySelective(user);
        return queryUserInfo(user.getId());
    }

    /**
     * 查询用户是否存在
     * @param username
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Integer ifExist(String username) {
        if (selectUserByName(username) == null)
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        else return SearchFriendsStatusEnum.SUCCESS.status;
    }

    /**
     * 查询好友是否是自身
     * @param userId
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Integer ifItself(String userId, String username) {
        User user = selectUserByName(username);
        if (user.getId().equals(userId)) {
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }
        else return SearchFriendsStatusEnum.SUCCESS.status;
    }

    /**
     * 查询好友是否已经添加
     * @param username
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Integer ifAdded(String myId, String username) {
        User friend = selectUserByName(username);
        //User user = userMapper.selectByPrimaryKey(myId);

        Example friendExample = new Example(Friend.class);
        Example.Criteria criteria = friendExample.createCriteria();
        criteria.andEqualTo("myUserId", myId);
        criteria.andEqualTo("myFriendUserId", friend.getId());
        Friend user = friendMapper.selectOneByExample(friendExample);
        if (user != null) {
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        } else return SearchFriendsStatusEnum.SUCCESS.status;
    }

    /**
     * 根据用户名查找
     * @param username
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public User selectUserByName(String username) {
        Example userExample = new Example(User.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username", username);
        return userMapper.selectOneByExample(userExample);
    }

    /**
     * 此用户可以被添加为好友
     * @param name
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public UserVo canAdd(String name) {
        UserVo userVo = new UserVo();
        User user = selectUserByName(name);
        BeanUtils.copyProperties(user, userVo);
        return userVo;
    }

    /**
     * 发送好友请求，把请求保存到数据库
     * @param myId
     * @param friendName
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void sendFriendRequest(String myId, String friendName) {
        User friend = selectUserByName(friendName);

        Example friendExample = new Example(FriendRequest.class);
        Example.Criteria criteria = friendExample.createCriteria();
        criteria.andEqualTo("sendUserId", myId);
        criteria.andEqualTo("acceptUserId", friend.getId());

        FriendRequest friendRequest = friendRequestMapper.selectOneByExample(friendExample);
        if (friendRequest == null) {
            String requestId = sid.nextShort();
            FriendRequest request = new FriendRequest();
            request.setId(requestId);
            request.setSendUserId(myId);
            request.setAcceptUserId(friend.getId());
            request.setRequestDateTime(new Date());
            friendRequestMapper.insert(request);
        }
    }

    /**
     * 获取好友请求列表
     * @param acceptUserId
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<FriendRequestVo> queryFriendRequestList(String acceptUserId) {
        return userMapperExtend.queryFriendRequestList(acceptUserId);
    }

    /**
     * 忽略好友请求
     * @param userId
     * @param friendId
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void ignoreRequest(String userId, String friendId) {
        Example requestExample = new Example(FriendRequest.class);
        Example.Criteria criteria = requestExample.createCriteria();
        criteria.andEqualTo("sendUserId", friendId);
        criteria.andEqualTo("acceptUserId", userId);
        if (friendRequestMapper.selectOneByExample(requestExample) != null) {
            friendRequestMapper.deleteByExample(requestExample);
        }
    }

    /**
     * 通过好友请求
     * @param userId
     * @param friendId
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void passRequest(String userId, String friendId) {
        // 先删除数据库中的请求记录
        ignoreRequest(userId, friendId);

        if (isInFriend(userId, friendId)) {
            // 正向保存记录
            Friend me = new Friend();
            String this_id = sid.nextShort();
            me.setId(this_id);
            me.setMyUserId(userId);
            me.setMyFriendUserId(friendId);
            friendMapper.insert(me);

            // 反向保存记录
            Friend friend = new Friend();
            String other_id = sid.nextShort();
            friend.setId(other_id);
            friend.setMyUserId(friendId);
            friend.setMyFriendUserId(userId);
            friendMapper.insert(friend);

            // 使用websocket主动推送消息给请求发起者,跟新通讯录列表. // TODO Android平台需要重新测试此功能,mui有问题
            Channel channel = UserChannelRel.get(userId);
            if (channel != null) {
                DataContent dataContent = new DataContent();
                dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);
                channel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
            }
        }
    }

    /**
     * 获取好友列表
     * @param userId
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<FriendListVo> queryFriendList(String userId) {
        return friendMapperExtend.queryFriendList(userId);
    }

    /**
     * 保存消息到数据库
     * @param msgBean
     * @return msgId
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public String saveMsg(MsgBean msgBean) {
        String id = sid.nextShort();
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setId(id);
        chatMsg.setMsg(msgBean.getMsg());
        chatMsg.setSendUserId(msgBean.getSenderId());
        chatMsg.setAcceptUserId(msgBean.getReceiverId());
        chatMsg.setCreateTime(new Date());
        chatMsg.setSignFlag(MsgSignFlagEnum.UNSIGNED.type);
        chatMsgMapper.insert(chatMsg);
        return id;
    }

    /**
     * 批量签收消息
     * @param msgIdList
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateSighedMsg(List<String> msgIdList) {
        chatMsgMapperExtend.batchUpdateSighedMsg(msgIdList);
    }

    /**
     * 获取未读消息列表
     * @param acceptUserId
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ChatMsg> getUnreadMsgList(String acceptUserId) {
        Example msgExample = new Example(ChatMsg.class);
        Example.Criteria criteria = msgExample.createCriteria();
        criteria.andEqualTo("acceptUserId", acceptUserId);
        criteria.andEqualTo("signFlag", 0);
        return chatMsgMapper.selectByExample(msgExample);
    }

    /**
     * 查询好友是否已经记录
     * @param userId
     * @param friendId
     * @return 好友不存在
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public boolean isInFriend(String userId, String friendId) {
        Example friendExample = new Example(Friend.class);
        Example.Criteria criteria = friendExample.createCriteria();
        criteria.andEqualTo("myUserId", userId);
        criteria.andEqualTo("myFriendUserId", friendId);
        Friend meToFriend = friendMapper.selectOneByExample(friendExample);
        return meToFriend == null;
    }

}
