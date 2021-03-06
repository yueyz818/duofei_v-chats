package com.duofei.user;

import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author duofei
 * @date 2019/8/16
 */
public class GroupUser extends BaseUser {

    private final WebRtcEndpoint outgoingMedia;

    private final ConcurrentMap<String, WebRtcEndpoint> incomingMedia = new ConcurrentHashMap<>();

    private MediaPipeline mediaPipeline;

    public GroupUser(String userName, WebSocketSession webSocketSession,WebRtcEndpoint outgoingMedia, String scopeId) {
        super(userName, webSocketSession);
        this.outgoingMedia = outgoingMedia;
    }

    public WebRtcEndpoint getOutgoingMedia() {
        return outgoingMedia;
    }

    /**
     * 添加输入端
     * @author duofei
     * @date 2019/8/29
     * @param name 当前用户名+输入端用户名
     * @param webRtcEndpoint
     */
    public void addIncomingMedia(String name,WebRtcEndpoint webRtcEndpoint){
        this.incomingMedia.put(name, webRtcEndpoint);
    }

    public MediaPipeline getMediaPipeline() {
        return mediaPipeline;
    }

    public void setMediaPipeline(MediaPipeline mediaPipeline) {
        this.mediaPipeline = mediaPipeline;
    }

    /**
     * 将 groupUser 用户降级为 baseuser
     * @author duofei
     * @date 2019/9/2
     * @return BaseUser 基础用户
     */
    public BaseUser upgrade(){
        BaseUser baseUser = new BaseUser(this.getUserName(), this.getSession());
        return baseUser;
    }

    public ConcurrentMap<String, WebRtcEndpoint> getIncomingMedia() {
        return incomingMedia;
    }
}
