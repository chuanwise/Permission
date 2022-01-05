package cn.chuanwise.xiaoming.permission.interactors;

import cn.chuanwise.xiaoming.annotation.Filter;
import cn.chuanwise.xiaoming.annotation.FilterParameter;
import cn.chuanwise.xiaoming.annotation.Required;
import cn.chuanwise.xiaoming.interactor.SimpleInteractors;
import cn.chuanwise.xiaoming.permission.PermissionPlugin;
import cn.chuanwise.xiaoming.permission.permission.Authorizer;
import cn.chuanwise.xiaoming.permission.permission.Role;
import cn.chuanwise.xiaoming.permission.record.AbstractRecord;
import cn.chuanwise.xiaoming.permission.util.Words;
import cn.chuanwise.xiaoming.user.XiaomingUser;
import cn.chuanwise.xiaoming.util.InteractorUtil;

import java.util.List;
import java.util.stream.Collectors;

public class HistoryInteractors extends SimpleInteractors<PermissionPlugin> {
    @Filter(Words.PERMISSION + Words.HISTORY)
    @Required("permission.admin.history.list")
    void lookHistory(XiaomingUser user) {
        final List<AbstractRecord> records = plugin.getHistory().getRecords();
        if (records.isEmpty()) {
            user.sendError("尚未产生任何权限记录");
        } else {
            InteractorUtil.showCollection(user, records, AbstractRecord::getDescription, 30);
        }
    }

    @Filter(Words.PERMISSION + Words.ROLE + Words.HISTORY + " {角色}")
    @Required("permission.admin.history.list")
    void lookRoleHistory(XiaomingUser user, @FilterParameter("角色") Role role) {
        final List<AbstractRecord> records = plugin.getHistory().getRecords()
                .stream()
                .filter(x -> x instanceof Role.Record && ((Role.Record) x).getRoleCode() == role.getRoleCode())
                .collect(Collectors.toList());
        if (records.isEmpty()) {
            user.sendError("未找到任何和权限组「" + role.getSimpleDescription() + "」相关的记录");
        } else {
            InteractorUtil.showCollection(user, records, AbstractRecord::getDescription, 30);
        }
    }

    @Filter(Words.PERMISSION + Words.ACCOUNT + Words.HISTORY + " {qq}")
    @Filter(Words.PERMISSION + Words.USER + Words.HISTORY + " {qq}")
    @Required("permission.admin.history.list")
    void lookAuthorizerHistory(XiaomingUser user, @FilterParameter("qq") Authorizer authorizer) {
        final List<AbstractRecord> records = plugin.getHistory().getRecords()
                .stream()
                .filter(x -> x instanceof Authorizer.Record && ((Authorizer.Record) x).getAccountCode() == authorizer.getAuthorizerCode())
                .collect(Collectors.toList());
        if (records.isEmpty()) {
            user.sendError("未找到任何和用户「" + authorizer.getSimpleDescription() + "」相关的记录");
        } else {
            InteractorUtil.showCollection(user, records, AbstractRecord::getDescription, 30);
        }
    }
}
