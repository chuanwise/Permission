package cn.chuanwise.xiaoming.permission.record;

import lombok.Data;

@Data
public class GrantAccountRecord
        extends AccountRecord {
    protected String permission;

    @Override
    public String getDescription() {
        return "授予用户 " + accountCode + " 权限节点 " + permission;
    }
}
