package cn.chuanwise.xiaoming.permission.permission;

import cn.chuanwise.api.SimpleDescribable;
import cn.chuanwise.util.CollectionUtil;
import cn.chuanwise.util.TagUtil;
import cn.chuanwise.xiaoming.permission.Permission;
import cn.chuanwise.xiaoming.permission.object.PermissionPluginObject;
import cn.chuanwise.xiaoming.permission.record.AbstractRecord;
import lombok.Data;

import java.util.*;
import java.util.function.Supplier;

@Data
public class Role
        extends PermissionEntity
        implements PermissionPluginObject, SimpleDescribable {
    long roleCode;

    int priority;
    String name;

    @Override
    public String getSimpleDescription() {
        return "#" + roleCode + "：" + name;
    }

    @Override
    public Set<String> getOriginalTags() {
        return CollectionUtil.asSet(TagUtil.ALL, name, String.valueOf(roleCode));
    }

    @Data
    public static abstract class Record extends AbstractRecord {
        long roleCode;
    }

    public static class CreateRecord extends Record {
        @Override
        public String getDescription() {
            return "创建了角色 #" + roleCode;
        }
    }

    public static class RemoveRecord extends Record {
        @Override
        public String getDescription() {
            return "删除了角色 #" + roleCode;
        }
    }

    public static abstract class EditRecord extends Record {}

    @Data
    public static class ChangePriorityRecord extends EditRecord {
        int priority;

        @Override
        public String getDescription() {
            return "设置角色 #" + roleCode + " 的优先级为 $" + priority;
        }
    }

    @Data
    public static abstract class ParentRoleRecord extends EditRecord {
        long parentRoleCode;
    }

    public static class AddGlobalParentRoleRecord extends ParentRoleRecord {
        @Override
        public String getDescription() {
            return "为角色 #" + roleCode + " 添加了全局父角色 #" + parentRoleCode;
        }
    }

    public static class RemoveGlobalParentRoleRecord extends ParentRoleRecord {
        @Override
        public String getDescription() {
            return "为角色 #" + roleCode + " 删除了全局父角色 #" + parentRoleCode;
        }
    }

    @Data
    public static abstract class GroupParentRoleRecord extends ParentRoleRecord {
        String groupTag;
    }

    public static class AddGroupParentRoleRecord extends GroupParentRoleRecord {
        @Override
        public String getDescription() {
            return "为角色 #" + roleCode + " 添加了在群 %" + groupTag + " 中的父角色 #" + parentRoleCode;
        }
    }

    public static class RemoveGroupParentRoleRecord extends GroupParentRoleRecord {
        @Override
        public String getDescription() {
            return "为角色 #" + roleCode + " 删除在群 %" + groupTag + " 中的父角色 #" + parentRoleCode;
        }
    }

    @Data
    public static abstract class PermissionRecord extends EditRecord {
        String permission;
    }

    public static class AddGlobalPermissionRecord extends PermissionRecord {
        @Override
        public String getDescription() {
            return "为角色 #" + roleCode + " 添加了全局权限 " + permission;
        }
    }

    public static class RemoveGlobalPermissionRecord extends PermissionRecord {
        @Override
        public String getDescription() {
            return "为角色 #" + roleCode + " 删除了全局权限 " + permission;
        }
    }

    @Data
    public static abstract class GroupPermissionRecord extends PermissionRecord {
        String groupTag;
    }

    public static class AddGroupPermissionRecord extends GroupPermissionRecord {
        @Override
        public String getDescription() {
            return "为角色 #" + roleCode + " 添加了位于群 %" + groupTag + " 中的权限 " + permission;
        }
    }

    public static class RemoveGroupPermissionRecord extends GroupPermissionRecord {
        @Override
        public String getDescription() {
            return "为角色 #" + roleCode + " 删除了位于群 %" + groupTag + " 中的权限 " + permission;
        }
    }

    @Data
    public static class ChangeNameRecord extends EditRecord {
        String nameChangeTo;

        @Override
        public String getDescription() {
            return "将角色 #" + roleCode + " 的名字改为 " + nameChangeTo;
        }
    }

    /** 判断全局父子关系 */
    public boolean isGlobalChildOf(long roleCode) {
        return hasGlobalRole(roleCode);
    }

    public boolean isGlobalChildOf(Role role) {
        return hasGlobalRole(role);
    }

    public boolean isGlobalParentOf(Role role) {
        return role.isGlobalChildOf(this);
    }

    public boolean isGlobalParentOf(long roleCode) {
        return getPermissionSystem()
                .getRole(roleCode)
                .map(role -> role.isGlobalChildOf(this.roleCode))
                .orElse(false);
    }

    /** 判断群聊父子关系 */
    public boolean isGroupChildOf(String groupTag, long roleCode) {
        return hasGroupRole(groupTag, roleCode);
    }

    public boolean isGroupChildOf(String groupTag, Role role) {
        return hasGroupRole(groupTag, role);
    }

    public boolean isGroupParentOf(String groupTag, long roleCode) {
        return getPermissionSystem()
                .getRole(roleCode)
                .map(role -> role.isGroupChildOf(groupTag, this.roleCode))
                .orElse(false);
    }

    public boolean isGroupParentOf(String groupTag, Role role) {
        return role.isGroupChildOf(groupTag, this);
    }

    /** 添加角色的全局父权限组 */
    public boolean assignGlobalParentRole(long operatorCode, int position, long parentRoleCode) {
        return assignGlobalParentRole0(operatorCode,
                () -> (!isGlobalChildOf(parentRoleCode) && isGlobalParentOf(parentRoleCode) && assignGlobalRole(position, parentRoleCode)) ? Optional.of(parentRoleCode) : Optional.empty());
    }

    public boolean assignGlobalParentRole(long operatorCode, long parentRoleCode) {
        return assignGlobalParentRole0(operatorCode,
                () -> (!isGlobalChildOf(parentRoleCode) && isGlobalParentOf(parentRoleCode) && assignGlobalRole(parentRoleCode)) ? Optional.of(parentRoleCode) : Optional.empty());
    }

    public boolean assignGlobalParentRole(long operatorCode, Role role) {
        return assignGlobalParentRole0(operatorCode,
                () -> (!isGlobalChildOf(role) && !isGlobalParentOf(role) && assignGlobalRole(role)) ? Optional.of(role.getRoleCode()) : Optional.empty());
    }

    public boolean assignGlobalParentRole(long operatorCode, int position, Role role) {
        return assignGlobalParentRole0(operatorCode,
                () -> (!isGlobalChildOf(role) && !isGlobalParentOf(role) && assignGlobalRole(position, role)) ? Optional.of(role.getRoleCode()) : Optional.empty());
    }

    protected boolean assignGlobalParentRole0(long operatorCode, Supplier<Optional<Long>> executor) {
        return executor.get()
                .map(parentRoleCode -> {
                    final AddGlobalParentRoleRecord record = new AddGlobalParentRoleRecord();
                    record.setOperatorCode(operatorCode);
                    record.setParentRoleCode(parentRoleCode);
                    record.setRoleCode(roleCode);
                    getPlugin().getHistory().addRecord(record);
                    return true;
                })
                .orElse(false);
    }

    /** 删除角色的全局权限组 */
    public boolean revokeGlobalParentRole(long operatorCode, int position) {
        return getGlobalScope().getRole(position)
                .map(role -> revokeGlobalParentRole0(operatorCode,
                        () -> dismissGlobalRole(role) ? Optional.of(role.getRoleCode()) : Optional.empty()))
                .orElse(false);
    }

    public boolean revokeGlobalParentRole(long operatorCode, Role role) {
        return revokeGlobalParentRole0(operatorCode,
                () -> dismissGlobalRole(role) ? Optional.of(role.getRoleCode()) : Optional.empty());
    }

    public boolean revokeGlobalParentRole(long operatorCode, long roleCode) {
        return revokeGlobalParentRole0(operatorCode,
                () -> dismissGlobalRole(roleCode) ? Optional.of(roleCode) : Optional.empty());
    }

    protected boolean revokeGlobalParentRole0(long operatorCode, Supplier<Optional<Long>> executor) {
        return executor.get()
                .map(parentRoleCode -> {
                    final RemoveGlobalParentRoleRecord record = new RemoveGlobalParentRoleRecord();
                    record.setOperatorCode(operatorCode);
                    record.setParentRoleCode(parentRoleCode);
                    record.setRoleCode(this.roleCode);
                    getPlugin().getHistory().addRecord(record);
                    return true;
                })
                .orElse(false);
    }

    /** 删除角色的全局权限 */
    public boolean grantGlobalPermission(long operatorCode, int position, String permission) {
        return grantGlobalPermission0(operatorCode,
                () -> grantGlobalPermission(position, permission) ? Optional.of(permission) : Optional.empty());
    }

    public boolean grantGlobalPermission(long operatorCode, String permission) {
        return grantGlobalPermission0(operatorCode,
                () -> grantGlobalPermission(permission) ? Optional.of(permission) : Optional.empty());
    }

    public boolean grantGlobalPermission(long operatorCode, int position, Permission permission) {
        return grantGlobalPermission0(operatorCode,
                () -> grantGlobalPermission(position, permission) ? Optional.of(permission.toString()) : Optional.empty());
    }

    public boolean grantGlobalPermission(long operatorCode, Permission permission) {
        return grantGlobalPermission0(operatorCode,
                () -> grantGlobalPermission(permission) ? Optional.of(permission.toString()) : Optional.empty());
    }

    protected boolean grantGlobalPermission0(long operatorCode, Supplier<Optional<String>> executor) {
        return executor.get()
                .map(permission -> {
                    final AddGlobalPermissionRecord record = new AddGlobalPermissionRecord();
                    record.setOperatorCode(operatorCode);
                    record.setRoleCode(roleCode);
                    record.setPermission(permission);
                    getPlugin().getHistory().addRecord(record);
                    return true;
                })
                .orElse(false);
    }

    /** 删除角色的全局权限 */
    public boolean revokeGlobalPermission(long operatorCode, int position) {
        return global.getPermission(position)
                .map(permission -> revokeGlobalPermission0(operatorCode,
                        () -> revokeGlobalPermission(position) ? Optional.of(permission.toString()) : Optional.empty()))
                .orElse(false);
    }

    public boolean revokeGlobalPermission(long operatorCode, String permission) {
        return revokeGlobalPermission0(operatorCode,
                () -> revokeGlobalPermission(permission) ? Optional.of(permission) : Optional.empty());
    }

    public boolean revokeGlobalPermission(long operatorCode, Permission permission) {
        return revokeGlobalPermission0(operatorCode,
                () -> revokeGlobalPermission(permission) ? Optional.of(permission.toString()) : Optional.empty());
    }

    protected boolean revokeGlobalPermission0(long operatorCode, Supplier<Optional<String>> executor) {
        return executor.get()
                .map(permission -> {
                    final RemoveGlobalPermissionRecord record = new RemoveGlobalPermissionRecord();
                    record.setOperatorCode(operatorCode);
                    record.setRoleCode(roleCode);
                    record.setPermission(permission.toString());
                    getPlugin().getHistory().addRecord(record);
                    return true;
                })
                .orElse(false);
    }

    /** 添加角色的全局权限 */
    public boolean grantGroupPermission(long operatorCode, String groupTag, String permission) {
        return grantGroupPermission0(operatorCode, groupTag,
                () -> grantGroupPermission(groupTag, permission) ? Optional.of(permission) : Optional.empty());
    }

    public boolean grantGroupPermission(long operatorCode, String groupTag, int position, String permission) {
        return grantGroupPermission0(operatorCode, groupTag,
                () -> grantGroupPermission(groupTag, position, permission) ? Optional.of(permission) : Optional.empty());
    }

    public boolean grantGroupPermission(long operatorCode, String groupTag, Permission permission) {
        return grantGroupPermission0(operatorCode, groupTag,
                () -> grantGroupPermission(groupTag, permission) ? Optional.of(permission.toString()) : Optional.empty());
    }

    public boolean grantGroupPermission(long operatorCode, String groupTag, int position, Permission permission) {
        return grantGroupPermission0(operatorCode, groupTag,
                () -> grantGroupPermission(groupTag, position, permission) ? Optional.of(permission.toString()) : Optional.empty());
    }

    protected boolean grantGroupPermission0(long operatorCode, String groupTag, Supplier<Optional<String>> executor) {
        return executor.get()
                .map(permission -> {
                    final AddGroupPermissionRecord record = new AddGroupPermissionRecord();
                    record.setOperatorCode(operatorCode);
                    record.setRoleCode(roleCode);
                    record.setPermission(permission);
                    record.setGroupTag(groupTag);
                    getPlugin().getHistory().addRecord(record);
                    return true;
                })
                .orElse(false);
    }

    /** 删除角色的群聊权限 */
    public boolean dismissGroupPermission(long operatorCode, String groupTag, String permission) {
        return dismissGroupPermission0(operatorCode, groupTag,
                () -> revokeGroupPermission(groupTag, permission) ? Optional.of(permission) : Optional.empty());
    }

    public boolean dismissGroupPermission(long operatorCode, String groupTag, Permission permission) {
        return dismissGroupPermission0(operatorCode, groupTag,
                () -> revokeGroupPermission(groupTag, permission) ? Optional.of(permission.toString()) : Optional.empty());
    }

    public boolean dismissGroupPermission(long operatorCode, String groupTag, int position) {
        return getGroupPermission(groupTag, position)
                .map(permission -> dismissGroupPermission0(operatorCode, groupTag,
                        () -> revokeGroupPermission(groupTag, position) ? Optional.of(permission.toString()) : Optional.empty()))
                .orElse(false);
    }

    protected boolean dismissGroupPermission0(long operatorCode, String groupTag, Supplier<Optional<String>> executor) {
        return executor.get()
                .map(permission -> {
                    final RemoveGroupPermissionRecord record = new RemoveGroupPermissionRecord();
                    record.setOperatorCode(operatorCode);
                    record.setPermission(permission);
                    record.setGroupTag(groupTag);
                    record.setRoleCode(roleCode);
                    getPlugin().getHistory().addRecord(record);
                    return true;
                })
                .orElse(false);
    }

    /** 添加角色在群里的父权限组 */
    public boolean assignGroupParentRole(long operatorCode, String groupTag, long roleCode) {
        return assignGroupParentRole0(operatorCode, groupTag,
                () -> (!isGroupChildOf(groupTag, roleCode) && !isGroupParentOf(groupTag, roleCode) && assignGroupRole(groupTag, roleCode)) ? Optional.of(roleCode) : Optional.empty());
    }

    public boolean assignGroupParentRole(long operatorCode, String groupTag, Role role) {
        return assignGroupParentRole0(operatorCode, groupTag,
                () -> (!isGroupChildOf(groupTag, roleCode) && !isGroupParentOf(groupTag, roleCode) && assignGroupRole(groupTag, role)) ? Optional.of(role.getRoleCode()) : Optional.empty());
    }

    public boolean assignGroupParentRole(long operatorCode, String groupTag, int position, long roleCode) {
        return assignGroupParentRole0(operatorCode, groupTag,
                () -> (!isGroupChildOf(groupTag, roleCode) && !isGroupParentOf(groupTag, roleCode) && assignGroupRole(groupTag, position, roleCode)) ? Optional.of(roleCode) : Optional.empty());
    }

    public boolean assignGroupParentRole(long operatorCode, String groupTag, int position, Role role) {
        return assignGroupParentRole0(operatorCode, groupTag,
                () -> (!isGroupChildOf(groupTag, roleCode) && !isGroupParentOf(groupTag, roleCode) && assignGroupRole(groupTag, position, role)) ? Optional.of(role.getRoleCode()) : Optional.empty());
    }

    protected boolean assignGroupParentRole0(long operatorCode, String groupTag, Supplier<Optional<Long>> executor) {
        return executor.get()
                .map(parentRoleCode -> {
                    final AddGroupParentRoleRecord record = new AddGroupParentRoleRecord();
                    record.setOperatorCode(operatorCode);
                    record.setParentRoleCode(parentRoleCode);
                    record.setRoleCode(roleCode);
                    record.setGroupTag(groupTag);
                    getPlugin().getHistory().addRecord(record);
                    return true;
                })
                .orElse(false);
    }

    /** 删除角色在群里的父权限组 */
    public boolean dismissGroupParentRole(long operatorCode, String groupTag, long roleCode) {
        return dismissGroupParentRole0(operatorCode, groupTag,
                () -> dismissGroupRole(groupTag, roleCode) ? Optional.of(roleCode) : Optional.empty());
    }

    public boolean dismissGroupParentRole(long operatorCode, String groupTag, Role role) {
        return dismissGroupParentRole0(operatorCode, groupTag,
                () -> dismissGroupRole(groupTag, role) ? Optional.of(role.getRoleCode()) : Optional.empty());
    }

    public boolean dismissGroupParentRole(long operatorCode, String groupTag, int position) {
        return getGroupRole(groupTag, position)
                .map(role -> dismissGroupParentRole0(operatorCode, groupTag,
                        () -> dismissGroupRole(groupTag, position) ? Optional.of(role.getRoleCode()) : Optional.empty()))
                .orElse(false);
    }

    protected boolean dismissGroupParentRole0(long operatorCode, String groupTag, Supplier<Optional<Long>> executor) {
        return executor.get()
                .map(parentRoleCode -> {
                    final RemoveGroupParentRoleRecord record = new RemoveGroupParentRoleRecord();
                    record.setOperatorCode(operatorCode);
                    record.setParentRoleCode(parentRoleCode);
                    record.setRoleCode(roleCode);
                    record.setGroupTag(groupTag);
                    getPlugin().getHistory().addRecord(record);
                    return true;
                })
                .orElse(false);
    }

    /** 修改权限组优先级 */
    public void changePriority(long operatorCode, int priority) {
        this.priority = priority;

        final ChangePriorityRecord record = new ChangePriorityRecord();
        record.setOperatorCode(operatorCode);
        record.setPriority(priority);
        record.setRoleCode(roleCode);
        getPlugin().getHistory().addRecord(record);
    }

    public void changeName(long operatorCode, String name) {
        this.name = name;

        final ChangeNameRecord record = new ChangeNameRecord();
        record.setRoleCode(roleCode);
        record.setNameChangeTo(name);
        record.setOperatorCode(operatorCode);
        getPlugin().getHistory().addRecord(record);
    }
}
