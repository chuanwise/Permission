package cn.chuanwise.xiaoming.permission.record;

import cn.chuanwise.xiaoming.permission.object.PermissionPluginObject;
import lombok.Data;

@Data
public abstract class AbstractRecord
        implements PermissionPluginObject {
    protected long operatorCode;
    protected long timeMillis = System.currentTimeMillis();

    public abstract String getDescription();
}
