package cn.chuanwise.xiaoming.permission;

import cn.chuanwise.xiaoming.account.Account;
import cn.chuanwise.xiaoming.group.GroupInformation;
import cn.chuanwise.xiaoming.permission.configuration.PermissionConfiguration;
import cn.chuanwise.xiaoming.permission.configuration.PluginConfiguration;
import cn.chuanwise.xiaoming.permission.interactors.AuthorizerInteractors;
import cn.chuanwise.xiaoming.permission.interactors.HistoryInteractors;
import cn.chuanwise.xiaoming.permission.interactors.PermissionInteractors;
import cn.chuanwise.xiaoming.permission.interactors.RoleInteractors;
import cn.chuanwise.xiaoming.permission.permission.Authorizer;
import cn.chuanwise.xiaoming.permission.permission.Role;
import cn.chuanwise.xiaoming.permission.record.PermissionHistory;
import cn.chuanwise.xiaoming.plugin.JavaPlugin;
import cn.chuanwise.xiaoming.user.GroupXiaomingUser;
import cn.chuanwise.xiaoming.user.XiaomingUser;
import lombok.AccessLevel;
import lombok.Getter;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Getter
@SuppressWarnings("all")
public class PermissionPlugin extends JavaPlugin {
    public static final PermissionPlugin INSTANCE = new PermissionPlugin();

    protected PermissionSystem permissionSystem;
    protected PluginConfiguration configuration;
    protected PermissionHistory history;

    @Getter(AccessLevel.NONE)
    private PermissionConfiguration permissionConfiguration;

    @Override
    public void onLoad() {
        getDataFolder().mkdirs();

        this.permissionConfiguration = loadFileOrSupply(PermissionConfiguration.class,
                new File(getDataFolder(), "permission.json"), PermissionConfiguration::new);

        configuration = loadFileOrSupply(PluginConfiguration.class,
                new File(getDataFolder(), "configuration.json"), PluginConfiguration::new);
        history = loadFileOrSupply(PermissionHistory.class,
                new File(getDataFolder(), "history.json"), PermissionHistory::new);
        permissionSystem = new PermissionSystem(permissionConfiguration);
    }

    @Override
    public void onEnable() {
        final Map<Long, Role> roles = permissionSystem.getRoles();
        if (roles.isEmpty()) {
            final Role role = new Role();
            role.setName("默认组");
            permissionSystem.addRole(xiaomingBot.getCode(), role);
            configuration.setGlobalDefaultRoleCode(role.getRoleCode());
        }

        registerPermissionService();

        xiaomingBot.getInteractorManager().registerInteractors(new PermissionInteractors(), this);
        xiaomingBot.getInteractorManager().registerInteractors(new HistoryInteractors(), this);
        xiaomingBot.getInteractorManager().registerInteractors(new RoleInteractors(), this);
        xiaomingBot.getInteractorManager().registerInteractors(new AuthorizerInteractors(), this);

        xiaomingBot.getScheduler().runAtFixedRate(TimeUnit.MINUTES.toMillis(1), permissionSystem::flush);
    }

    public boolean registerPermissionService() {
        getLogger().info("正在注册权限服务");
        final PermissionService service = xiaomingBot.getPermissionService();
        return service.register(new PermissionRequester() {
            @Override
            public boolean hasPermission(XiaomingUser user, Permission permission) {
                final Account account = user.getAccount();
                if (user instanceof GroupXiaomingUser) {
                    return hasPermission(account, ((GroupXiaomingUser) user).getGroupInformation(), permission);
                } else {
                    return hasPermission(account, permission);
                }
            }

            @Override
            public boolean hasPermission(Account account, Permission permission) {
                final Optional<Authorizer> optionalAuthorizer = permissionSystem.getAuthorizer(account.getCode());

                if (optionalAuthorizer.isPresent()) {
                    final Authorizer authorizer = optionalAuthorizer.get();
                    return authorizer.accessibleGlobal(permission) == Accessible.ACCESSIBLE;
                } else {
                    final List<Role> defaultRoles = permissionSystem.getAccountDefaultRoles(account.getCode());

                    if (defaultRoles.isEmpty()) {
                        final Optional<Role> optionalRole = permissionSystem.getRole(configuration.getGlobalDefaultRoleCode());
                        if (!optionalRole.isPresent()) {
                            getLogger().error("验证用户 " + account.getAliasAndCode() + " 是否具备全局权限 " + permission + " 时，" +
                                    "无法找到默认权限组 #" + configuration.getGlobalDefaultRoleCode());
                            return false;
                        }
                        return optionalRole.get().accessibleGlobal(permission) == Accessible.ACCESSIBLE;
                    } else {
                        for (Role defaultRole : defaultRoles) {
                            final Accessible accessible = defaultRole.accessibleGlobal(permission);
                            if (accessible != Accessible.UNACCESSIBLE) {
                                return accessible == Accessible.ACCESSIBLE;
                            }
                        }
                        return false;
                    }
                }
            }

            @Override
            public boolean hasPermission(Account account, GroupInformation groupInformation, Permission permission) {
                final Optional<Authorizer> optionalAuthorizer = permissionSystem.getAuthorizer(account.getCode());

                final Set<String> tags = groupInformation.getTags();
                if (optionalAuthorizer.isPresent()) {
                    final Authorizer authorizer = optionalAuthorizer.get();
                    for (String tag : tags) {
                        final Accessible accessible = authorizer.accessibleGroup(tag, permission);
                        if (accessible == Accessible.ACCESSIBLE) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    final List<Role> defaultRoles = permissionSystem.getAccountDefaultRoles(account.getCode());
                    final String groupTag = groupInformation.getCodeString();

                    if (defaultRoles.isEmpty()) {
                        final Optional<Role> optionalRole = permissionSystem.getRole(configuration.getGlobalDefaultRoleCode());
                        if (!optionalRole.isPresent()) {
                            getLogger().error("验证用户 " + account.getAliasAndCode() + " 在群聊 " + groupInformation.getAliasAndCode() + " 中" +
                                    "是否具备权限 " + permission + " 时，" +
                                    "无法找到默认权限组 #" + configuration.getGlobalDefaultRoleCode());
                            return false;
                        }
                        return optionalRole.get().accessibleGroup(groupTag, permission) == Accessible.ACCESSIBLE;
                    } else {
                        for (Role role : defaultRoles) {
                            final Accessible accessible = role.accessibleGroup(groupTag, permission);
                            if (accessible != Accessible.UNKNOWN) {
                                return accessible == Accessible.ACCESSIBLE;
                            }
                        }
                        return false;
                    }
                }
            }
        }, this);
    }

    @Override
    public void onDisable() {
        xiaomingBot.getFileSaver().readyToSave(history);
        xiaomingBot.getFileSaver().readyToSave(configuration);
        xiaomingBot.getFileSaver().readyToSave(permissionConfiguration);

        xiaomingBot.getPermissionService().reset();
    }
}