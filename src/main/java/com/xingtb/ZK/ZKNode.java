package com.xingtb.ZK;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * ZK上的节点实体类
 */
@Data
@NoArgsConstructor(staticName = "of")
@Accessors(chain = true)
@JSONType(orders = {"path", "type", "ctime", "data", "children"})
public class ZKNode {
    private String path;
    private String type;
    private String ctime;
    private String data;
    private List<ZKNode> children;
}
