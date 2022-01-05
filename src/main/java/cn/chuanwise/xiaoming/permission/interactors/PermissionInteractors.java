package cn.chuanwise.xiaoming.permission.interactors;

import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.CollectionUtil;
import cn.chuanwise.util.MapUtil;
import cn.chuanwise.xiaoming.annotation.Filter;
import cn.chuanwise.xiaoming.annotation.FilterParameter;
import cn.chuanwise.xiaoming.annotation.Required;
import cn.chuanwise.xiaoming.interactor.SimpleInteractors;
import cn.chuanwise.xiaoming.permission.Permission;
import cn.chuanwise.xiaoming.permission.PermissionPlugin;
import cn.chuanwise.xiaoming.permission.PermissionSystem;
import cn.chuanwise.xiaoming.permission.configuration.PluginConfiguration;
import cn.chuanwise.xiaoming.permission.permission.Authorizer;
import cn.chuanwise.xiaoming.permission.permission.Role;
import cn.chuanwise.xiaoming.permission.util.ChooseUtil;
import cn.chuanwise.xiaoming.permission.util.Words;
import cn.chuanwise.xiaoming.plugin.Plugin;
import cn.chuanwise.xiaoming.user.GroupXiaomingUser;
import cn.chuanwise.xiaoming.user.XiaomingUser;
import cn.chuanwise.xiaoming.util.AtUtil;
import cn.chuanwise.xiaoming.util.MiraiCodeUtil;

import java.util.*;

public class PermissionInteractors
        extends SimpleInteractors<PermissionPlugin> {
    private PermissionSystem permissionSystem;
    private PluginConfiguration configuration;

    @Override
    public void onRegister() {
        permissionSystem = plugin.getPermissionSystem();
        configuration = plugin.getConfiguration();

        xiaomingBot.getInteractorManager().registerParameterParser(Authorizer.class, context -> {
            final String inputValue = context.getInputValue();
            final Optional<Long> optionalCode = AtUtil.parseAt(inputValue);
            if (optionalCode.isPresent()) {
                final long accountCode = optionalCode.get();
                return Container.of(permissionSystem.createAccount(accountCode));
            }

            return Container.ofOptional(ChooseUtil.chooseAccount(context.getUser(), inputValue));
        }, true, plugin);

        xiaomingBot.getInteractorManager().registerParameterParser(Permission.class, context -> {
            final Permission permission;
            final String inputValue = MiraiCodeUtil.contentToString(context.getInputValue());
            try {
                permission = Permission.compile(MiraiCodeUtil.contentToString(inputValue));
            } catch (Exception exception) {
                context.getUser().sendError("编译错误「" + inputValue + "」：" + exception.getMessage());
                return null;
            }
            return Container.of(permission);
        }, true, plugin);
    }

    /** 其他操作 */
    @Filter(Words.SET + Words.GROUP + Words.DEFAULT + Words.ROLE + " {群标签} {角色}")
    @Required("permission.admin.role.groupDefault.set")
    public void setGroupDefaultRole(XiaomingUser user,
                                    @FilterParameter("群标签") String groupTag,
                                    @FilterParameter("角色") Role role) {
        final Map<String, Long> defaultRoleCodes = permissionSystem.getConfiguration().getGroupDefaultRoleCodes();
        defaultRoleCodes.put(groupTag, role.getRoleCode());
        permissionSystem.readyToSave();

        user.sendMessage("成功设置群 %" + groupTag + " 内所有成员的默认角色为 " + role.getSimpleDescription());
    }

    @Filter(Words.REMOVE + Words.GROUP + Words.DEFAULT + Words.ROLE + " {群标签}")
    @Required("permission.admin.role.groupDefault.remove")
    public void removeGroupDefaultRole(XiaomingUser user,
                                       @FilterParameter("群标签") String groupTag) {
        final Map<String, Long> defaultRoleCodes = permissionSystem.getConfiguration().getGroupDefaultRoleCodes();
        defaultRoleCodes.remove(groupTag);
        permissionSystem.readyToSave();

        user.sendMessage("成功删除群 %" + groupTag + " 内所有成员的特定默认角色");
    }

    @Filter(Words.GROUP + Words.DEFAULT + Words.ROLE + " {群标签}")
    @Required("permission.admin.role.groupDefault.look")
    public void lookGroupDefaultRole(XiaomingUser user,
                                     @FilterParameter("群标签") String groupTag) {
        final Optional<Long> optionalCode = permissionSystem.getConfiguration().getNakedGroupDefaultRoleCode(groupTag);
        if (optionalCode.isPresent()) {
            final long roleCode = optionalCode.get();
            final Optional<Role> optionalRole = permissionSystem.getRole(roleCode);
            user.sendMessage("群聊 %" + groupTag + " 中所有成员自动属于角色 " +
                    optionalRole.map(Role::getSimpleDescription).orElse("#" + roleCode + "？"));
        } else {
            final long roleCode = permissionSystem.getConfiguration().getGlobalDefaultRoleCode();
            final Optional<Role> optionalRole = permissionSystem.getRole(roleCode);
            user.sendMessage("群聊 %" + groupTag + " 中所有成员自动属于全局的默认角色 " +
                    optionalRole.map(Role::getSimpleDescription).orElse("#" + roleCode + "？"));
        }
    }

    @Filter(Words.SET + Words.THIS + Words.GROUP + Words.DEFAULT + Words.ROLE + " {角色}")
    @Required("permission.admin.role.groupDefault.set")
    public void setThisGroupDefaultRole(GroupXiaomingUser user,
                                        @FilterParameter("角色") Role role) {
        final String groupTag = user.getGroupCodeString();
        final Map<String, Long> defaultRoleCodes = permissionSystem.getConfiguration().getGroupDefaultRoleCodes();
        defaultRoleCodes.put(groupTag, role.getRoleCode());
        permissionSystem.readyToSave();

        user.sendMessage("成功设置本群所有成员的默认角色为 " + role.getSimpleDescription());
    }

    @Filter(Words.REMOVE + Words.THIS + Words.GROUP + Words.DEFAULT + Words.ROLE)
    @Required("permission.admin.role.groupDefault.remove")
    public void removeThisGroupDefaultRole(GroupXiaomingUser user) {
        final String groupTag = user.getGroupCodeString();
        final Map<String, Long> defaultRoleCodes = permissionSystem.getConfiguration().getGroupDefaultRoleCodes();
        defaultRoleCodes.remove(groupTag);
        permissionSystem.readyToSave();

        user.sendMessage("成功删除本群所有成员的特定默认角色");
    }

    @Filter(Words.THIS + Words.GROUP + Words.DEFAULT + Words.ROLE)
    @Required("permission.admin.role.groupDefault.look")
    public void lookThisGroupDefaultRole(GroupXiaomingUser user) {
        final String groupTag = user.getGroupCodeString();
        final Optional<Long> optionalCode = permissionSystem.getConfiguration().getNakedGroupDefaultRoleCode(groupTag);
        if (optionalCode.isPresent()) {
            final long roleCode = optionalCode.get();
            final Optional<Role> optionalRole = permissionSystem.getRole(roleCode);
            user.sendMessage("本群所有成员自动属于角色 " +
                    optionalRole.map(Role::getSimpleDescription).orElse("#" + roleCode + "？"));
        } else {
            final long roleCode = permissionSystem.getConfiguration().getGlobalDefaultRoleCode();
            final Optional<Role> optionalRole = permissionSystem.getRole(roleCode);
            user.sendMessage("本群所有成员自动属于全局的默认角色 " +
                    optionalRole.map(Role::getSimpleDescription).orElse("#" + roleCode + "？"));
        }
    }

    @Filter(Words.FLUSH + Words.PERMISSION)
    @Required("permission.admin.permission.flush")
    public void flush(XiaomingUser user) {
        permissionSystem.flush();
        user.sendMessage("成功刷新权限缓存");
    }

    @Filter(Words.PERMISSION + Words.LIST)
    @Required("permission.admin.permission.list")
    public void listPermission(XiaomingUser user) {
        final Map<Plugin, List<Permission>> permissions = new HashMap<>();
        permissionSystem.getPermissionHandlers().forEach(handler -> {
            MapUtil.getOrPutSupply(permissions, handler.getPlugin(), ArrayList::new).add(handler.getPermission());
        });
        permissions.values().forEach(list -> Collections.sort(list, Comparator.comparing(Permission::toString)));

        if (permissions.isEmpty()) {
            user.sendError("没有注册任何权限节点");
        } else {
            user.sendMessage("各插件注册的权限节点有：\n" +
                    CollectionUtil.toIndexString(permissions.entrySet(), entry -> {
                        final Plugin plugin = entry.getKey();
                        final List<Permission> requiredPermissions = entry.getValue();

                        return Plugin.getChineseName(plugin) + "（" + requiredPermissions.size() + "）个：\n" +
                                CollectionUtil.toString(requiredPermissions, "\n");
                    }));
        }
    }

    @Filter(Words.COMPILE + Words.PERMISSION + " {r:权限}")
    @Filter(Words.COMPILE + Words.PERMISSION + " {r:权限}")
    @Required("permission.admin.compile")
    public void compilePermission(XiaomingUser user, @FilterParameter("权限") Permission permission) {
        user.sendMessage("编译成功：" + permission);
    }

    @Filter(Words.TEST + Words.PERMISSION + " {权限1} {r:权限2}")
    @Required("permission.admin.test")
    public void testPermission(XiaomingUser user,
                               @FilterParameter("权限1") Permission left,
                               @FilterParameter("权限2") Permission right) {
        user.sendMessage(left + " => " + right + "：" + left.acceptable(right).toChinese());
    }

    @Filter(Words.REGISTER + Words.PERMISSION + Words.SERVICE)
    @Required("permission.admin.register")
    public void registerPermissionService(XiaomingUser user) {
        if (plugin.registerPermissionService()) {
            user.sendMessage("当前权限服务由 Permission 插件提供，无需注册");
        } else {
            user.sendMessage("注册成功");
        }
    }
}