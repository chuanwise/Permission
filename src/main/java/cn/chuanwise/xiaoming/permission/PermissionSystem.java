package cn.chuanwise.xiaoming.permission;

import cn.chuanwise.api.Flushable;
import cn.chuanwise.util.CollectionUtil;
import cn.chuanwise.util.MapUtil;
import cn.chuanwise.xiaoming.account.Account;
import cn.chuanwise.xiaoming.contact.contact.MemberContact;
import cn.chuanwise.xiaoming.permission.configuration.PermissionConfiguration;
import cn.chuanwise.xiaoming.permission.configuration.PluginConfiguration;
import cn.chuanwise.xiaoming.permission.object.PermissionPluginObject;
import cn.chuanwise.xiaoming.permission.permission.Authorizer;
import cn.chuanwise.xiaoming.permission.permission.Role;
import cn.chuanwise.xiaoming.plugin.Plugin;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
public class PermissionSystem
        implements PermissionPluginObject, Flushable {
    @Getter(AccessLevel.NONE)
    final PermissionConfiguration permissionFile;
    final List<PermissionHandler> permissionHandlers = new CopyOnWriteArrayList<>();

    @Data
    public static final class PermissionHandler {
        final Plugin plugin;
        final Permission permission;
    }

    public PermissionSystem(PermissionConfiguration permissionFile) {
        this.permissionFile = permissionFile;
    }

    public Optional<PermissionHandler> getPermissionHandler(String string) {
        return CollectionUtil.findFirst(permissionHandlers, x -> Objects.equals(x.getPermission().toString(), string)).toOptional();
    }

    public Optional<Permission> getPermission(String string) {
        return getPermissionHandler(string).map(PermissionHandler::getPermission);
    }

    public Permission createPermission(String string, Plugin plugin) {
        final Optional<Permission> optional = getPermission(string);
        if (optional.isPresent()) {
            return optional.get();
        }

        synchronized (permissionHandlers) {
            final Permission permission = Permission.compile(string);
            permissionHandlers.add(new PermissionHandler(plugin, permission));
            return permission;
        }
    }

    public Optional<Role> getRole(long roleCode) {
        return MapUtil.get(permissionFile.getRoles(), roleCode).toOptional();
    }

    public List<Role> searchRoles(String string) {
        return getRoles().values().stream()
                .filter(role -> {
                    final String name = role.getName();
                    return role.hasTag(string) || name.contains(string) || string.contains(name);
                })
                .collect(Collectors.toList());
    }

    public List<Role> searchRolesByTag(String tag) {
        return getRoles().values().stream()
                .filter(role -> role.hasTag(tag))
                .collect(Collectors.toList());
    }

    public Map<Long, Role> getRoles() {
        return Collections.unmodifiableMap(permissionFile.getRoles());
    }

    public Optional<Authorizer> getAccount(long accountCode) {
        return MapUtil.get(permissionFile.getAuthorizers(), accountCode).toOptional();
    }

    public Authorizer createAccount(long accountCode) {
        return MapUtil.getOrPutSupply(permissionFile.getAuthorizers(), accountCode, () -> {
            final Authorizer account = new Authorizer();
            account.setAuthorizerCode(accountCode);

            getAccountDefaultRoles(accountCode).stream()
                    .map(Role::getRoleCode)
                    .forEach(roleCode -> account.assignGlobalRole(getXiaomingBot().getCode(), roleCode));
            return account;
        });
    }

    public Map<Long, Authorizer> getAccounts() {
        return Collections.unmodifiableMap(permissionFile.getAuthorizers());
    }

    public List<Authorizer> searchAccountsByTag(String tag) {
        return permissionFile.getAuthorizers().values().stream()
                .filter(account -> getXiaomingBot().getAccountManager().createAccount(account.getAuthorizerCode()).hasTag(tag))
                .collect(Collectors.toList());
    }

    public List<Authorizer> searchAccounts(String input) {
        return permissionFile.getAuthorizers().values().stream()
                .filter(account -> {
                    final Account a = getXiaomingBot().getAccountManager().createAccount(account.getAuthorizerCode());
                    final String alias = a.getAlias();

                    if (a.hasTag(input)) {
                        return true;
                    }
                    if (Objects.isNull(alias)) {
                        return false;
                    }
                    return alias.contains(input) || input.contains(alias);
                })
                .collect(Collectors.toList());
    }

    public void addRole(long operatorCode, Role role) {
        // allocate roleCode
        final long roleCode = getPlugin().getConfiguration().allocateRoleCode();
        role.setRoleCode(roleCode);

        // add to list
        permissionFile.getRoles().put(roleCode, role);

        // log
        final Role.CreateRecord record = new Role.CreateRecord();
        record.setOperatorCode(operatorCode);
        record.setRoleCode(roleCode);
        getPlugin().getHistory().addRecord(record);
    }

    public boolean removeRole(long operatorCode, long roleCode) {
        final boolean result = Objects.nonNull(permissionFile.getRoles().remove(roleCode));
        if (result) {
            // log
            final Role.RemoveRecord record = new Role.RemoveRecord();
            record.setOperatorCode(operatorCode);
            record.setRoleCode(roleCode);
            getPlugin().getHistory().addRecord(record);
        }
        return result;
    }

    public Optional<Role> getGlobalDefaultRole() {
        return getRole(getConfiguration().getGlobalDefaultRoleCode());
    }

    public PluginConfiguration getConfiguration() {
        return getPlugin().getConfiguration();
    }

    public List<Role> getAccountDefaultRoles(long accountCode) {
        final List<Role> defaultRoles = getXiaomingBot().getContactManager()
                .getPrivateContactPossibly(accountCode)
                .stream()
                .filter(MemberContact.class::isInstance)
                .map(contact -> ((MemberContact) contact).getGroupContact().getTags())
                .flatMap(Collection::stream)
                .distinct()
                .map(getConfiguration()::getNakedGroupDefaultRole)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        getGlobalDefaultRole().ifPresent(x -> {
            if (!defaultRoles.contains(x)) {
                defaultRoles.add(x);
            }
        });
        defaultRoles.sort((l, r) -> -Long.compare(l.getPriority(), r.getPriority()));

        return defaultRoles;
    }

    public void readyToSave() {
        permissionFile.readyToSave();
    }

    @Override
    public synchronized void flush() {
        permissionHandlers.clear();

        // 注册权限
        getXiaomingBot().getInteractorManager().getInteractors().forEach(interactor -> {
            final Plugin plugin = interactor.getPlugin();
            for (Permission permission : interactor.getPermissions()) {
                permissionHandlers.add(new PermissionHandler(plugin, permission));
            }
        });
    }
}