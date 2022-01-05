package cn.chuanwise.xiaoming.permission.permission;

import cn.chuanwise.api.AbstractOriginalTagMarkable;
import cn.chuanwise.util.ConditionUtil;
import cn.chuanwise.util.LambdaUtil;
import cn.chuanwise.util.MapUtil;
import cn.chuanwise.util.StringUtil;
import cn.chuanwise.xiaoming.permission.Permission;
import cn.chuanwise.xiaoming.permission.Accessible;
import cn.chuanwise.xiaoming.permission.object.PermissionPluginObject;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public abstract class PermissionEntity
        extends AbstractOriginalTagMarkable
        implements PermissionPluginObject {
    final PermissionScope global = new PermissionScope();
    final Map<String, PermissionScope> groups = new HashMap<>();

    public Accessible accessibleGlobal(Permission required) {
        return global.accessible(required, role -> role.accessibleGlobal(required));
    }

    public Accessible accessibleGroup(String groupTag, Permission required) {
        ConditionUtil.checkArgument(StringUtil.notEmpty(groupTag), "group tag is empty!");

        final Optional<PermissionScope> optionalPermissionScope = Optional.ofNullable(groups.get(groupTag));
        if (optionalPermissionScope.isPresent()) {
            final PermissionScope groupScope = optionalPermissionScope.get();
            final Accessible accessible = groupScope.accessible(required, x -> x.accessibleGroup(groupTag, required));
            if (accessible != Accessible.UNKNOWN) {
                return accessible;
            }
        }

        final Accessible accessible = global.accessible(required, x -> x.accessibleGroup(groupTag, required));
        if (accessible != Accessible.UNKNOWN) {
            return accessible;
        }

        return Accessible.UNKNOWN;
    }

    /** 添加全局权限 */
    protected boolean grantGlobalPermission(String permission) {
        return global.grant(permission);
    }

    protected boolean grantGlobalPermission(Permission permission) {
        return global.grant(permission);
    }

    protected boolean grantGlobalPermission(int position, String permission) {
        return global.grant(position, permission);
    }

    protected boolean grantGlobalPermission(int position, Permission permission) {
        return global.grant(position, permission);
    }

    /** 删除全局权限 */
    protected boolean revokeGlobalPermission(Permission permission) {
        return global.revoke(permission);
    }

    protected boolean revokeGlobalPermission(String permission) {
        return global.revoke(permission);
    }

    protected boolean revokeGlobalPermission(int position) {
        return global.revoke(position);
    }

    /** 添加全局角色 */
    protected boolean assignGlobalRole(Role role) {
        return global.assign(role);
    }

    protected boolean assignGlobalRole(long roleCode) {
        return global.assign(roleCode);
    }

    protected boolean assignGlobalRole(int position, Role role) {
        return global.assign(position, role);
    }

    protected boolean assignGlobalRole(int position, long roleCode) {
        return global.assign(position, roleCode);
    }

    /** 添加群角色 */
    protected boolean assignGroupRole(String groupTag, long roleCode) {
        return createGroupScope(groupTag).assign(roleCode);
    }

    protected boolean assignGroupRole(String groupTag, Role role) {
        return createGroupScope(groupTag).assign(role);
    }

    protected boolean assignGroupRole(String groupTag, int position, Role role) {
        return createGroupScope(groupTag).assign(position, role);
    }

    protected boolean assignGroupRole(String groupTag, int position, long roleCode) {
        return createGroupScope(groupTag).assign(position, roleCode);
    }

    /** 删除群角色 */
    protected boolean dismissGroupRole(String groupTag, long roleCode) {
        return createGroupScope(groupTag).dismiss(roleCode);
    }

    protected boolean dismissGroupRole(String groupTag, Role role) {
        return createGroupScope(groupTag).dismiss(role);
    }

    /** 删除全局角色 */
    protected boolean dismissGroupRole(String groupTag, int position) {
        return createGroupScope(groupTag).dismiss(position);
    }

    protected boolean dismissGlobalRole(Role role) {
        return global.dismiss(role);
    }

    protected boolean dismissGlobalRole(long roleCode) {
        return global.dismiss(roleCode);
    }

    protected boolean dismissGlobalRole(int position) {
        return global.dismiss(position);
    }

    /** 添加群权限 */
    protected boolean grantGroupPermission(String groupTag, String permission) {
        return createGroupScope(groupTag).grant(permission);
    }

    protected boolean grantGroupPermission(String groupTag, Permission permission) {
        return createGroupScope(groupTag).grant(permission);
    }

    protected boolean grantGroupPermission(String groupTag, int position, String permission) {
        return createGroupScope(groupTag).grant(position, permission);
    }

    protected boolean grantGroupPermission(String groupTag, int position, Permission permission) {
        return createGroupScope(groupTag).grant(position, permission);
    }

    /** 删除群权限 */
    protected boolean revokeGroupPermission(String groupTag, String permission) {
        return getGroupScope(groupTag)
                .map(scope -> {
                    final boolean result = scope.revoke(permission);
                    if (scope.isEmpty()) {
                        groups.remove(groupTag);
                    }
                    return result;
                })
                .orElse(false);
    }

    protected boolean revokeGroupPermission(String groupTag, Permission permission) {
        return getGroupScope(groupTag)
                .map(scope -> {
                    final boolean result = scope.revoke(permission);
                    if (scope.isEmpty()) {
                        groups.remove(groupTag);
                    }
                    return result;
                })
                .orElse(false);
    }

    protected boolean revokeGroupPermission(String groupTag, int position) {
        return getGroupScope(groupTag)
                .map(scope -> {
                    final boolean result = scope.revoke(position);
                    if (scope.isEmpty()) {
                        groups.remove(groupTag);
                    }
                    return result;
                })
                .orElse(false);
    }

    /** 快捷方式 */
    protected Optional<PermissionScope> getGroupScope(String groupTag) {
        return MapUtil.get(groups, groupTag).toOptional();
    }

    protected Optional<Permission> getGroupPermission(String groupTag, int position) {
        return getGroupScope(groupTag)
                .flatMap(scope -> scope.getPermission(position));
    }

    protected Optional<Role> getGroupRole(String groupTag, int position) {
        return getGroupScope(groupTag)
                .flatMap(scope -> scope.getRole(position));
    }

    protected PermissionScope createGroupScope(String groupTag) {
        return MapUtil.getOrPutSupply(groups, groupTag, PermissionScope::new);
    }

    public PermissionScope getGlobalScope() {
        return global;
    }

    protected Optional<Role> getGlobalRole(int position) {
        return global.getRole(position);
    }

    public Map<String, PermissionScope> getGroups() {
        return Collections.unmodifiableMap(groups);
    }

    /** 判定器 */
    protected boolean hasGlobalRole(long roleCode) {
        if (global.roleCodes.contains(roleCode)) {
            return true;
        }
        for (Role role : global.getRoles()) {
            if (role.hasGlobalRole(roleCode)) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasGlobalRole(Role role) {
        return hasGlobalRole(role.getRoleCode());
    }

    protected boolean hasGroupRole(String groupTag, long roleCode) {
        return getGroupScope(groupTag)
                .map(scope -> {
                    if (scope.roleCodes.contains(roleCode)) {
                        return true;
                    }
                    for (Role role : scope.getRoles()) {
                        if (role.hasGroupRole(groupTag, roleCode)) {
                            return true;
                        }
                    }
                    return false;
                })
                .orElseGet(() -> hasGlobalRole(roleCode));
    }

    protected boolean hasGroupRole(String groupTag, Role role) {
        return hasGroupRole(groupTag, role.getRoleCode());
    }
}