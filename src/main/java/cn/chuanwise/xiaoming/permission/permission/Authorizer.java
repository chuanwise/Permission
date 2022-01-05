package cn.chuanwise.xiaoming.permission.permission;

import cn.chuanwise.api.SimpleDescribable;
import cn.chuanwise.xiaoming.account.Account;
import cn.chuanwise.xiaoming.permission.Permission;
import cn.chuanwise.xiaoming.permission.object.PermissionPluginObject;
import cn.chuanwise.xiaoming.permission.record.AbstractRecord;
import lombok.Data;

import java.util.*;
import java.util.function.Supplier;

@Data
public class Authorizer
        extends PermissionEntity
        implements PermissionPluginObject, SimpleDescribable {
    long authorizerCode;

    @Override
    public String getSimpleDescription() {
        return getXiaomingBot().getAccountManager().getAlias(authorizerCode)
                .map(alias -> "#" + authorizerCode + "：" + alias)
                .orElse("#" + authorizerCode);
    }

    public Account getAccount() {
        return getXiaomingBot().getAccountManager().createAccount(authorizerCode);
    }

    @Override
    public Set<String> getOriginalTags() {
        return getXiaomingBot().getAccountManager().getTags(authorizerCode);
    }

    @Override
    public boolean isOriginalTag(String tag) {
        return getAccount().isOriginalTag(tag);
    }

    @Override
    public boolean addTags(String... tags) {
        return getAccount().addTags(tags);
    }

    @Override
    public boolean addTags(Iterable<String> tags) {
        return getAccount().addTags(tags);
    }

    @Override
    public boolean hasTags(String... tags) {
        return getAccount().hasTags(tags);
    }

    @Override
    public boolean hasTags(Iterable<String> tags) {
        return getAccount().hasTags(tags);
    }

    @Data
    public abstract static class Record extends AbstractRecord {
        long accountCode;
    }

    public static class CreateRecord extends Record {
        @Override
        public String getDescription() {
            return "创建了账户 #" + accountCode;
        }
    }

    public static class RemoveRecord extends Record {
        @Override
        public String getDescription() {
            return "删除了账户 #" + accountCode;
        }
    }

    public static abstract class EditRecord extends Record {}

    @Data
    public static abstract class RoleRecord extends EditRecord {
        long parentRoleCode;
    }

    public static class AddGlobalRoleRecord extends RoleRecord {
        @Override
        public String getDescription() {
            return "为账户 #" + accountCode + " 添加了全局父角色 #" + parentRoleCode;
        }
    }

    public static class RemoveGlobalRoleRecord extends RoleRecord {
        @Override
        public String getDescription() {
            return "为账户 #" + accountCode + " 删除了全局父角色 #" + parentRoleCode;
        }
    }

    @Data
    public static abstract class GroupRoleRecord extends RoleRecord {
        String groupTag;
    }

    public static class AddGroupRoleRecord extends GroupRoleRecord {
        @Override
        public String getDescription() {
            return "为账户 #" + accountCode + " 添加了在群 %" + groupTag + " 中的父角色 #" + parentRoleCode;
        }
    }

    public static class RemoveGroupRoleRecord extends GroupRoleRecord {
        @Override
        public String getDescription() {
            return "为账户 #" + accountCode + " 删除在群 %" + groupTag + " 中的父角色 #" + parentRoleCode;
        }
    }

    @Data
    public static abstract class PermissionRecord extends EditRecord {
        String permission;
    }

    public static class AddGlobalPermissionRecord extends PermissionRecord {
        @Override
        public String getDescription() {
            return "为账户 #" + accountCode + " 添加了全局权限 " + permission;
        }
    }

    public static class RemoveGlobalPermissionRecord extends PermissionRecord {
        @Override
        public String getDescription() {
            return "为账户 #" + accountCode + " 删除了全局权限 " + permission;
        }
    }

    @Data
    public static abstract class GroupPermissionRecord extends PermissionRecord {
        String groupTag;
    }

    public static class AddGroupPermissionRecord extends GroupPermissionRecord {
        @Override
        public String getDescription() {
            return "为账户 #" + accountCode + " 添加了位于群 %" + groupTag + " 中的权限 " + permission;
        }
    }

    public static class RemoveGroupPermissionRecord extends GroupPermissionRecord {
        @Override
        public String getDescription() {
            return "为账户 #" + accountCode + " 删除了位于群 %" + groupTag + " 中的权限 " + permission;
        }
    }

    /** 判断是否具备在权限组中 */
    public boolean hasGlobalRole(long roleCode) {
        return getPlugin().getPermissionSystem()
                .getRole(roleCode)
                .map(this::hasGlobalRole)
                .orElse(false);
    }

    public boolean hasGlobalRole(Role role) {
        for (Long roleCode : global.roleCodes) {
            if (Objects.equals(role.getRoleCode(), roleCode)) {
                return true;
            }

            final Boolean hasParentRole = getPlugin().getPermissionSystem()
                    .getRole(roleCode)
                    .map(x -> x.hasGlobalRole(role))
                    .orElse(false);

            if (hasParentRole) {
                return true;
            }
        }

        return false;
    }

    public boolean hasGroupRole(String groupTag, Role role) {
        final Optional<PermissionScope> optionalPermissionScope = getGroupScope(groupTag);
        if (!optionalPermissionScope.isPresent()) {
            return false;
        }
        final PermissionScope scope = optionalPermissionScope.get();

        for (Long roleCode : scope.roleCodes) {
            if (Objects.equals(role.getRoleCode(), roleCode)) {
                return true;
            }

            final Boolean hasParentRole = getPlugin().getPermissionSystem()
                    .getRole(roleCode)
                    .map(x -> x.hasGroupRole(groupTag, role))
                    .orElse(false);

            if (hasParentRole) {
                return true;
            }
        }

        return false;
    }

    public boolean hasGroupRole(String groupTag, long roleCode) {
        return getPlugin().getPermissionSystem().getRole(roleCode)
                .map(x -> hasGroupRole(groupTag, x))
                .orElse(false);
    }

    /** 添加账户的全局父权限组 */
    public boolean assignGlobalRole(long operatorCode, int position, long parentRoleCode) {
        return assignGlobalRole0(operatorCode,
                () -> assignGlobalRole(operatorCode, position, parentRoleCode) ? Optional.of(parentRoleCode) : Optional.empty());
    }

    public boolean assignGlobalRole(long operatorCode, long parentRoleCode) {
        return assignGlobalRole0(operatorCode,
                () -> assignGlobalRole(parentRoleCode) ? Optional.of(parentRoleCode) : Optional.empty());
    }

    public boolean assignGlobalRole(long operatorCode, Role role) {
        return assignGlobalRole0(operatorCode,
                () -> assignGlobalRole(role) ? Optional.of(role.getRoleCode()) : Optional.empty());
    }

    public boolean assignGlobalRole(long operatorCode, int position, Role role) {
        return assignGlobalRole0(operatorCode,
                () -> assignGlobalRole(position, role) ? Optional.of(role.getRoleCode()) : Optional.empty());
    }

    protected boolean assignGlobalRole0(long operatorCode, Supplier<Optional<Long>> executor) {
        return executor.get()
                .map(parentRoleCode -> {
                    final AddGlobalRoleRecord record = new AddGlobalRoleRecord();
                    record.setOperatorCode(operatorCode);
                    record.setParentRoleCode(parentRoleCode);
                    record.setAccountCode(authorizerCode);
                    getPlugin().getHistory().addRecord(record);
                    return true;
                })
                .orElse(false);
    }

    /** 删除账户的全局权限组 */
    public boolean dismissGlobalRole(long operatorCode, int position) {
        return getGlobalScope().getRole(position)
                .map(role -> dismissGlobalRole0(operatorCode,
                        () -> dismissGlobalRole(role) ? Optional.of(role.getRoleCode()) : Optional.empty()))
                .orElse(false);
    }

    public boolean dismissGlobalRole(long operatorCode, Role role) {
        return dismissGlobalRole0(operatorCode,
                () -> dismissGlobalRole(role) ? Optional.of(role.getRoleCode()) : Optional.empty());
    }

    public boolean dismissGlobalRole(long operatorCode, long accountCode) {
        return dismissGlobalRole0(operatorCode,
                () -> dismissGlobalRole(accountCode) ? Optional.of(accountCode) : Optional.empty());
    }

    protected boolean dismissGlobalRole0(long operatorCode, Supplier<Optional<Long>> executor) {
        return executor.get()
                .map(parentRoleCode -> {
                    final RemoveGlobalRoleRecord record = new RemoveGlobalRoleRecord();
                    record.setOperatorCode(operatorCode);
                    record.setParentRoleCode(parentRoleCode);
                    record.setAccountCode(this.authorizerCode);
                    getPlugin().getHistory().addRecord(record);
                    return true;
                })
                .orElse(false);
    }

    /** 删除账户的全局权限 */
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
                    record.setAccountCode(authorizerCode);
                    record.setPermission(permission);
                    getPlugin().getHistory().addRecord(record);
                    return true;
                })
                .orElse(false);
    }

    /** 删除账户的全局权限 */
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
                    record.setAccountCode(authorizerCode);
                    record.setPermission(permission.toString());
                    getPlugin().getHistory().addRecord(record);
                    return true;
                })
                .orElse(false);
    }

    /** 添加账户的全局权限 */
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
                    record.setAccountCode(authorizerCode);
                    record.setPermission(permission);
                    record.setGroupTag(groupTag);
                    getPlugin().getHistory().addRecord(record);
                    return true;
                })
                .orElse(false);
    }

    /** 删除账户的群聊权限 */
    public boolean revokeGroupPermission(long operatorCode, String groupTag, String permission) {
        return revokeGroupPermission0(operatorCode, groupTag,
                () -> revokeGroupPermission(groupTag, permission) ? Optional.of(permission) : Optional.empty());
    }

    public boolean revokeGroupPermission(long operatorCode, String groupTag, Permission permission) {
        return revokeGroupPermission0(operatorCode, groupTag,
                () -> revokeGroupPermission(groupTag, permission) ? Optional.of(permission.toString()) : Optional.empty());
    }

    public boolean revokeGroupPermission(long operatorCode, String groupTag, int position) {
        return getGroupPermission(groupTag, position)
                .map(permission -> revokeGroupPermission0(operatorCode, groupTag,
                        () -> revokeGroupPermission(groupTag, position) ? Optional.of(permission.toString()) : Optional.empty()))
                .orElse(false);
    }

    protected boolean revokeGroupPermission0(long operatorCode, String groupTag, Supplier<Optional<String>> executor) {
        return executor.get()
                .map(permission -> {
                    final RemoveGroupPermissionRecord record = new RemoveGroupPermissionRecord();
                    record.setOperatorCode(operatorCode);
                    record.setPermission(permission);
                    record.setGroupTag(groupTag);
                    record.setAccountCode(authorizerCode);
                    getPlugin().getHistory().addRecord(record);
                    return true;
                })
                .orElse(false);
    }

    /** 添加账户在群里的父权限组 */
    public boolean assignGroupRole(long operatorCode, String groupTag, long roleCode) {
        return assignGroupRole0(operatorCode, groupTag,
                () -> assignGroupRole(groupTag, roleCode) ? Optional.of(roleCode) : Optional.empty());
    }

    public boolean assignGroupRole(long operatorCode, String groupTag, Role role) {
        return assignGroupRole0(operatorCode, groupTag,
                () -> assignGroupRole(groupTag, role) ? Optional.of(role.getRoleCode()) : Optional.empty());
    }

    public boolean assignGroupRole(long operatorCode, String groupTag, int position, long accountCode) {
        return assignGroupRole0(operatorCode, groupTag,
                () -> assignGroupRole(groupTag, position, accountCode) ? Optional.of(accountCode) : Optional.empty());
    }

    public boolean assignGroupRole(long operatorCode, String groupTag, int position, Role role) {
        return assignGroupRole0(operatorCode, groupTag,
                () -> assignGroupRole(groupTag, position, role) ? Optional.of(role.getRoleCode()) : Optional.empty());
    }

    /** 删除账户在群里的父权限组 */
    public boolean dismissGroupRole(long operatorCode, String groupTag, long roleCode) {
        return dismissGroupRole0(operatorCode, groupTag,
                () -> dismissGroupRole(groupTag, roleCode) ? Optional.of(roleCode) : Optional.empty());
    }

    public boolean dismissGroupRole(long operatorCode, String groupTag, Role role) {
        return dismissGroupRole0(operatorCode, groupTag,
                () -> dismissGroupRole(groupTag, role) ? Optional.of(role.getRoleCode()) : Optional.empty());
    }

    public boolean dismissGroupRole(long operatorCode, String groupTag, int position) {
        return getGroupRole(groupTag, position)
                .map(role -> dismissGroupRole0(operatorCode, groupTag,
                        () -> dismissGroupRole(groupTag, position) ? Optional.of(role.getRoleCode()) : Optional.empty()))
                .orElse(false);
    }

    protected boolean dismissGroupRole0(long operatorCode, String groupTag, Supplier<Optional<Long>> executor) {
        return executor.get()
                .map(parentRoleCode -> {
                    final RemoveGroupRoleRecord record = new RemoveGroupRoleRecord();
                    record.setOperatorCode(operatorCode);
                    record.setParentRoleCode(parentRoleCode);
                    record.setParentRoleCode(parentRoleCode);
                    record.setGroupTag(groupTag);
                    getPlugin().getHistory().addRecord(record);
                    return true;
                })
                .orElse(false);
    }

    protected boolean assignGroupRole0(long operatorCode, String groupTag, Supplier<Optional<Long>> executor) {
        return executor.get()
                .map(parentRoleCode -> {
                    final AddGroupRoleRecord record = new AddGroupRoleRecord();
                    record.setOperatorCode(operatorCode);
                    record.setParentRoleCode(parentRoleCode);
                    record.setAccountCode(authorizerCode);
                    record.setGroupTag(groupTag);
                    getPlugin().getHistory().addRecord(record);
                    return true;
                })
                .orElse(false);
    }
}