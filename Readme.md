**服务器的运行依赖 kurento 流媒体服务器**

*流媒体服务器地址修改在KurentoConfig.java 中*

在确保流媒体服务器地址正确的情况下，直接通过启动类 VChatsApp 启动即可。

---

服务器详细处理流程如下：

<font size="5" color="#008B8B">如何进行sdp协商</font>

建立任何聊天都需要保证服务端有相应的 WebRtc 对象，当然，客户端也需要建立对应的 WebRtc 对象，

建立 WebRtc 对象需要流媒体管道 (MediaPipeline )，同一域中的 流媒体管道应该是同一个；所以在进行 sdp 协商之前，需要先创建域，但并不激活该域；也就是说域中的流媒体管道已经准备好。

1. 客户端通过域id 以及 sdpOffer 要求服务端创建 WebRtc 对象，
2. 服务端创建好WebRtc 对象，响应 SdpAnswer ，并开始收集 候选人信息
3. 客户端与服务端交换候选人信息

至此，服务端存在该用户的一个可用的 WebRtc 对象。剩下的就需要在建立实际的聊天时，去为该webRtc 创建连接了。

这个过程很好的抽取了出来，在建立聊天域时，就不需要再关心 sdp的协商过程。

<br>

<font size="5" color="#008B8B">如何建立一对一聊天</font>

1. 客户端 A 创建域
2. 域创建成功后，A和B 会收到域Id
3. 开始 进行 sdp 协商 
4. 客户端A激活域：如果B 已经激活（即B 的 webRtc 对象存在该域中），那么进行连接（A连B，B连A），否则仅把 A的 webRtc 对象放在该域中。
5. 客户端B 激活域：如果A 已经激活（即A 的 webRtc 对象存在该域中），那么进行连接（A连B，B连A），否则仅把 B的 webRtc 对象放在该域中。
6. A 结束通话时，会销毁该域，并通知到 B 

<br>

<font size="5" color="#008B8B">如何建立一对多聊天</font>

1. 客户端 A 请求 创建直播室
2. 直播室创建成功，接收到直播室域的 Id
3. 进行sdp协商
4. A 激活域，将自身的 webRtc对象 设置为直播室域的主持人

客户B 如何加入直播室呢？

1. 客户端 B 请求加入直播室
2. 请求成功，返回直播室域的 id
3. 进行sdp 协商
4. B 在直播室中激活，将自身的 WebRtc 对象设置为直播室域的参观者，并且设置主持人的WebRtc 对象连接到自身的 WebRtc 对象。

那么，如何退出直播室呢？

 如果是主持人，则会销毁域；如果是参观者，则会退出域，断开主持人的webRtc 与 其的连接，并且释放参观者的 WebRtc。

<br>

<font size="5" color="#008B8B">如何建立多对多聊天</font>

客户 A 创建群组域成功，接下来，A 和其它想要加入群组域的成员一样：

1. 申请加入域

2. 请求成功，返回群组域的id

3. 进行 sdp 协商：创建自身的 webRtc

4. 激活域：

   4.1 升级该用户为群组用户，将 自身的 WebRtc 对象设置为群组用户的输出流，

   4.2  检测群组中已经存在的用户，给该用户推送已经存在的用户信息

   4.3  检测群组中已经存在的用户，给已经存在的用户推送新成员加入的信息

5. 当前用户收到已经存在的多个用户信息（循环下述过程，直到处理完所有存在的用户）：

   5.1 进行sdp 协商：创建当前用户与存在用户的webrtc

   5.2  申请从存在用户接收媒体流：

   - 将当前用户与存在用户的webrtc（即在5.1 协商好的 webRtc 对象） 作为当前用户的群组对象的输入流
   - 设置存在用户的输出流连接到 当前用户与其的 webrtc 对象（这里是只有一个webRtc 对象，不过是该对象用于当前用户从存在用户接收流媒体数据）

6. 存在的用户收到新成员加入的信息：

   6.1 进行sdp 协商：创建存在用户与新成员的 webRtc 对象

   6.2 申请从新成员接收媒体流：

   - 将存在的用户与新成员的webRtc 对象（即在 6.1 协商好的webRtc 对象）作为存在用户的输入流
   - 设置新成员的输出流连接到 存在用户与新成员的webRtc 对象 

这个过程有点复杂，需要细细理解...

<br>

<font size="5" color="#008B8B">如何建立多人聊天</font>

多人聊天与多对多聊天不同之处在于多对多需要在客户端分别创建多个webRtc，而多人聊天只需要分别在客户端和服务器端创建一个webRtc，通过 `Composite`（集线器）来将多个媒体流合成网格状。

<br>

A 邀请 B、C 进行多人聊天，只要B、C 中有一人接受，多人聊天域创建成功。

1. B 、C 接受，给 A、B、C 发送多人聊天域创建成功的消息，包含了该域的id

2. 客户端接收到 多人聊天域创建成功的消息，开始进行sdp 协商

3. sdp 协商完成以后，开始激活

   3.1 创建`HubPort`，WebRtc 连接该 `HubPort`

   3.2 使用集线器构建的统一 `HubPort`, 连接该 WebRtc

4. 当 B、C 均退出时，多人聊天室结束，清理资源。

B 在第一次拒绝后，可以再次申请加入，流程和上面一样 （从第二步开始）；

这种聊天方式会比较卡，可能是 KMS 的缘故（所在服务器内存分配的太少）；

<br>

<font size="5" color="#008B8B">如何进行录制</font>

录制是通过构建 `RecorderEndpoint` 对象来完成。

1. 在一对一聊天时，客户A 请求录制，会录制 客户B 的媒体；
2. 在一对多时，会录制主持人；
3. 在多对多时，会通过集线器进行合流，将多个媒体流合成网格，录制成一个视频；
4. 在进行多人聊天时，直接使用已有的集线器即可。

<br>

<font size="5" color="#008B8B">如何进行回放</font>

回放功能是指播放已经录制好的视频；该功能是通过创建 `PlayerEndpoint` 对象来完成的。