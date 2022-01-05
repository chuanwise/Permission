package cn.chuanwise.xiaoming.permission.record;

import lombok.Data;

@Data
public abstract class AccountRecord
        extends AbstractRecord {
    protected long accountCode;
}
