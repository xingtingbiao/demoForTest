package com.test;

import com.alibaba.fastjson.JSON;
import com.xingtb.ZK.MyZKClient;
import com.xingtb.ZK.ZKNode;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ZKTest {
    private static final String connectString = "akka-test.internal.gridx.com:2181";
//    private static final String connectString = "127.0.0.1:2181";
    private static final int sessionTimeout = 2000;
    ZooKeeper zkClient = null;

    private static ZooKeeper getClient() throws IOException {
        return new ZooKeeper(connectString, sessionTimeout, null);
    }

//    @Before
//    public void init() throws IOException {
//        zkClient = new ZooKeeper(connectString, sessionTimeout, watchedEvent -> {
//            //收到事件通知后的回调函数（应该是我们自己的事件处理逻辑）
//            System.out.println(watchedEvent.getType() + "---" + watchedEvent.getPath());
//            try {
//                zkClient.getChildren("/", true);
//            } catch (Exception e) {
//            }
//        });
//    }

    /**
     * 数据的增删改查
     */
    //创建数据节点到zk中
    @Test
    public void testCreate() throws Exception{
        MyZKClient.create("/test1.1", "message1.1");
        MyZKClient.create("/test1.2", "message1.2");
        MyZKClient.create("/test1.1/test2.1", "message2.1");
        MyZKClient.create("/test1.1/test2.2", "message2.2");
        MyZKClient.create("/test1.2/test2.1", "message2.1");
        MyZKClient.create("/test1.2/test2.2", "message2.2");
    }

    @Test
    public void testExist() throws Exception{
        System.out.println(MyZKClient.exist("/idea"));
    }

    //获取子节点
    @Test
    public void getChildren() throws Exception{
        MyZKClient.getChildren("/");
        MyZKClient.getChildren("/test1.1");
        MyZKClient.getChildren("/zookeeper");
//        MyZKClient.getChildren("/tao_test");
        MyZKClient.getChildren("/test1.2");
    }

    @Test
    public void deleteNode() throws Exception {
        MyZKClient.delete("/test1.1");
    }

    @Test
    public void getNodeData() throws Exception{
        System.out.println(MyZKClient.getData("/test1.2"));
    }

    //设置znode
    @Test
    public void setData() throws Exception{
        zkClient.setData("/app1","hello".getBytes(), -1);
        byte[] data = zkClient.getData("/app1", false, null);
        System.out.println(new String(data));
    }

    @Test
    public void getZKNode() throws Exception {
        ZKNode allZKNodes = MyZKClient.getZKNode("/");
        System.out.println(allZKNodes);
        System.out.println(JSON.toJSONString(allZKNodes));
    }

    @Test
    public void testGet() throws Exception {
        MyZKClient.getChildren("/tao_test/global-data-actor");
    }
}
