package cn.chuanwise.xiaoming.permission.record;

import lombok.Data;

@Data
public class RecokeAccountRecord
        extends AccountRecord {
    protected String permission;

    @Override
    public String getDescription() {
        return "收回用户 " + accountCode + " 权限节点 " + permission;
    }
}
