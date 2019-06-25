package com.xingtb.ZK;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyZKClient {
    private static final FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS Z");
    private static final String connectString = "akka-test.internal.gridx.com:2181";
    // private static final String connectString = "127.0.0.1:2181";
    private static final int sessionTimeout = 2000;
    private static ZooKeeper zkClient = null;

    private static ZooKeeper getClient() throws IOException {
        if (null == zkClient) zkClient = new ZooKeeper(connectString, sessionTimeout, null);
        return zkClient;
    }

    public static void main(String[] args) throws Exception {
        ZooKeeper zkClient = getClient();
        System.out.println(zkClient.getState());
        List<String> children = zkClient.getChildren("/", true);
        byte[] data = zkClient.getData("/zookeeper", false, null);
        String s = new String(data);
        zkClient.close();
        System.out.println(zkClient.getState());
    }

    //判断znode是否存在
    public static Stat exist(String path) throws Exception {
        //节点元数据
        return getClient().exists(path, false);
    }

    public static void create(String path, String data) throws Exception {
        //上传的数据可以是任何类型，但都要转成byte[]
        String node = getClient().create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println("Create node success: " + node);
    }

    //获取znode的数据
    public static String getData(String path) throws Exception {
        byte[] data = getClient().getData(path, false, null);
        if (null == data) return null;
        return new String(data);
    }

    //删除znode的数据
    public static void delete(String path) throws Exception {
        //参数2：指定要删除的版本，-1表示删除所有版本
        getClient().delete(path, -1);
    }

    //获取子节点
    public static void getChildren(String path) throws Exception {
        List<String> children = getClient().getChildren(path, false);
        for (String child : children) {
            System.out.println(child);
        }
    }

    //获取子节点
    public static ZKNode getZKNode(String path) throws Exception {
        ZKNode zkNode = null;
        Stat exist = exist(path);
        if (null != exist) {
            String ephemeralOwner = "ephemeral";
            if (exist.getEphemeralOwner() == 0) ephemeralOwner = "persistent";
            zkNode = ZKNode.of().setPath(path)
                    .setType(ephemeralOwner)
                    .setCtime(format.format(new Date(exist.getCtime())));
            zkNode.setData(getData(path));
            List<String> children = getClient().getChildren(path, false);
            ArrayList<ZKNode> childs = new ArrayList<>();
            if (null != children && !children.isEmpty()) {
                String p = path;
                if (!p.endsWith("/")) p = p + "/";
                for (String child : children) {
                    childs.add(getZKNode(p + child));
                }
            }
            zkNode.setChildren(childs);
        }
        return zkNode;
    }

}
