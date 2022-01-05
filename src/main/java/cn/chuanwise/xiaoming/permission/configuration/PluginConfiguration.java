package cn.chuanwise.xiaoming.permission.configuration;

import cn.chuanwise.util.MapUtil;
import cn.chuanwise.xiaoming.permission.PermissionPlugin;
import cn.chuanwise.xiaoming.permission.object.PermissionPluginObject;
import cn.chuanwise.xiaoming.permission.permission.Role;
import cn.chuanwise.xiaoming.preservable.SimplePreservable;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
public class PluginConfiguration
        extends SimplePreservable<PermissionPlugin>
        implements PermissionPluginObject {
    long allocatedRoleCode;

    long globalDefaultRoleCode;
    Map<String, Long> groupDefaultRoleCodes = new HashMap<>();

    public synchronized long allocateRoleCode() {
        return ++allocatedRoleCode;
    }

    public Optional<Long> getNakedGroupDefaultRoleCode(String groupTag) {
        return MapUtil.get(groupDefaultRoleCodes, groupTag).toOptional();
    }

    public long getGroupDefaultRoleCode(String groupTag) {
        return getNakedGroupDefaultRoleCode(groupTag).orElse(globalDefaultRoleCode);
    }

    public Optional<Role> getNakedGroupDefaultRole(String groupTag) {
        return getNakedGroupDefaultRoleCode(groupTag)
                .flatMap(getPlugin().getPermissionSystem()::getRole);
    }

    public Optional<Role> getGroupDefaultRole(String groupTag) {
        return getPlugin().getPermissionSystem().getRole(getGroupDefaultRoleCode(groupTag));
    }
}