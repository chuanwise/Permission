package cn.chuanwise.xiaoming.permission.util;

import cn.chuanwise.util.CollectionUtil;
import cn.chuanwise.util.StaticUtil;
import cn.chuanwise.xiaoming.permission.permission.PermissionScope;
import cn.chuanwise.xiaoming.permission.permission.Role;

import java.util.Optional;

public class PermissionPluginUtil extends StaticUtil {
    public static String toDetailString(PermissionScope scope) {
        final String space = "  ";
        return "→ 角色：" + Optional.ofNullable(CollectionUtil.toIndexString(scope.getRoles(),
                                (integer, role) -> space + (integer + 1) + "：", Role::getSimpleDescription))
                                .map(x -> "\n" + x)
                                .orElse("（无）") + "\n" +
                "→ 权限：" + Optional.ofNullable(CollectionUtil.toIndexString(scope.getPermissions()))
                                .map(x -> "\n" + x)
                                .orElse("（无）");
    }
}