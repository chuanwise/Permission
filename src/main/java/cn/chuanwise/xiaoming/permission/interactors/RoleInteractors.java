package cn.chuanwise.xiaoming.permission.interactors;

import cn.chuanwise.toolkit.container.Container;
import cn.chuanwise.util.CollectionUtil;
import cn.chuanwise.xiaoming.annotation.Filter;
import cn.chuanwise.xiaoming.annotation.FilterParameter;
import cn.chuanwise.xiaoming.annotation.Required;
import cn.chuanwise.xiaoming.interactor.SimpleInteractors;
import cn.chuanwise.xiaoming.permission.Accessible;
import cn.chuanwise.xiaoming.permission.Permission;
import cn.chuanwise.xiaoming.permission.PermissionPlugin;
import cn.chuanwise.xiaoming.permission.PermissionSystem;
import cn.chuanwise.xiaoming.permission.configuration.PluginConfiguration;
import cn.chuanwise.xiaoming.permission.object.PermissionPluginObject;
import cn.chuanwise.xiaoming.permission.permission.Role;
import cn.chuanwise.xiaoming.permission.util.ChooseUtil;
import cn.chuanwise.xiaoming.permission.util.PermissionPluginUtil;
import cn.chuanwise.xiaoming.permission.util.Words;
import cn.chuanwise.xiaoming.user.GroupXiaomingUser;
import cn.chuanwise.xiaoming.user.XiaomingUser;

import java.util.Map;
import java.util.Optional;

public class RoleInteractors
        extends SimpleInteractors<PermissionPlugin>
        implements PermissionPluginObject {

    private PermissionSystem permissionSystem;
    private PluginConfiguration configuration;

    @Override
    public void onRegister() {
        permissionSystem = getPermissionSystem();
        configuration = plugin.getConfiguration();

        xiaomingBot.getInteractorManager().registerParameterParser(Role.class, context -> {
            return Container.ofOptional(ChooseUtil.chooseRole(context.getUser(), context.getInputValue()));
        }, true, plugin);
    }

    /** 列举、查看和增删 */
    @Filter(Words.ROLE)
    @Required("permission.admin.role.list")
    public void listRoles(XiaomingUser user) {
        final Map<Long, Role> roles = permissionSystem.getRoles();
        if (roles.isEmpty()) {
            user.sendError("目前并没有任何角色");
        } else {
            user.sendMessage("目前共有 " + roles.size() + " 个角色：\n" +
                    CollectionUtil.toString(roles.values(), Role::getSimpleDescription, "\n"));
        }
    }

    @Filter(Words.ROLE + " {角色}")
    @Required("permission.admin.role.list")
    public void lookRole(XiaomingUser user, @FilterParameter("角色") Role role) {
        user.sendMessage("『" + role.getName() + "』\n" +
                "【编号】" + role.getRoleCode() + "\n" +
                "【优先】" + role.getPriority() + "\n" +
                "【标签】" + CollectionUtil.toString(role.getTags()) + "\n" +
                "【全局】\n" + PermissionPluginUtil.toDetailString(role.getGlobalScope()) + "\n" +
                "【群聊】" + Optional.ofNullable(CollectionUtil.toString(role.getGroups().entrySet(), entry -> {
                    return "%" + entry.getKey() + "：\n" + PermissionPluginUtil.toDetailString(entry.getValue());
                })).orElse("（无）")
        );
    }

    @Filter(Words.ADD + Words.ROLE + " {角色名}")
    @Filter(Words.NEW + Words.ROLE + " {角色名}")
    @Filter(Words.ADD + Words.GLOBAL + Words.ROLE + " {角色名}")
    @Filter(Words.NEW + Words.GLOBAL + Words.ROLE + " {角色名}")
    @Required("permission.admin.role.add")
    public void addRole(XiaomingUser user, @FilterParameter("角色名") String name) {
        final Role role = new Role();
        role.setName(name);
        final boolean parentRoleAdded = role.assignGlobalParentRole(user.getCode(), configuration.getGlobalDefaultRoleCode());
        permissionSystem.addRole(user.getCode(), role);
        permissionSystem.readyToSave();

        if (parentRoleAdded) {
            user.sendError("成功创建角色 " + role.getSimpleDescription() + "。小明已经自动令其继承自默认权限组了");
        } else {
            user.sendError("成功创建角色 " + role.getSimpleDescription() + "。但小明没有成功令其继承自默认权限组");
        }
    }

    @Filter(Words.REMOVE + Words.ROLE + " {角色}")
    @Required("permission.admin.role.remove")
    public void removeRole(XiaomingUser user, @FilterParameter("角色") Role role) {
        if (permissionSystem.removeRole(user.getCode(), role.getRoleCode())) {
            permissionSystem.readyToSave();
            user.sendMessage("成功删除角色 " + role.getSimpleDescription());
        } else {
            user.sendMessage("删除角色 " + role.getSimpleDescription() + " 失败");
        }
    }

    /** 属性设定，标签增删 */
    @Filter(Words.SET + Words.ROLE + Words.LEVEL + " {角色} {等级}")
    @Filter(Words.SET + Words.ROLE + Words.PRIORITY + " {角色} {等级}")
    @Required("permission.admin.role.level.set")
    public void setRoleLevel(XiaomingUser user,
                             @FilterParameter("角色") Role role,
                             @FilterParameter("等级") int level) {
        role.changePriority(user.getCode(), level);
        permissionSystem.readyToSave();

        user.sendMessage("成功设置角色 " + role.getSimpleDescription() + " 的等级为 $" + level);
    }

    @Filter(Words.SET + Words.ROLE + Words.NAME + " {角色} {角色名}")
    @Required("permission.admin.role.name.set")
    public void setRoleName(XiaomingUser user, @FilterParameter("角色") Role role, @FilterParameter("角色名") String roleName) {
        role.changeName(user.getCode(), roleName);
        permissionSystem.readyToSave();
        user.sendMessage("成功修改角色名为「" + roleName + "」");
    }

    @Filter(Words.TAG + Words.ROLE + " {角色} {r:标记}")
    @Filter(Words.ADD + Words.ROLE + Words.TAG + " {角色} {r:标记}")
    @Required("permission.admin.role.tag.add")
    public void addRoleTag(XiaomingUser user, @FilterParameter("角色") Role role, @FilterParameter("标记") String tag) {
        if (role.addTag(tag)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功为角色 " + role.getSimpleDescription() + " 添加了标记 " + tag);
        } else {
            user.sendError("角色 " + role.getSimpleDescription() + " 已经具有标记  " + tag);
        }
    }

    @Filter(Words.REMOVE + Words.ROLE + Words.TAG + " {角色} {r:标记}")
    @Required("permission.admin.role.tag.remove")
    public void removeRoleTag(XiaomingUser user, @FilterParameter("角色") Role role, @FilterParameter("标记") String tag) {
        if (role.isOriginalTag(tag)) {
            user.sendError("「" + tag + "」是角色 " + role.getSimpleDescription() + " 的原生标记，不可以删除");
            return;
        }
        if (role.removeTag(tag)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功删除了角色 " + role.getSimpleDescription() + " 的标记 " + tag);
        } else {
            user.sendError("角色 " + role.getSimpleDescription() + " 并没有标记  " + tag);
        }
    }

    /** 角色与全局角色 */
    @Filter(Words.ROLE + " {子角色} " + Words.GLOBAL + Words.EXTENDS + " {父角色} {顺序}")
    @Filter(Words.ADD + Words.ROLE + Words.GLOBAL + Words.ROLE + " {顺序} {父角色}")
    @Required("permission.admin.role.global.role.add")
    public void addRoleGlobalRole(XiaomingUser user,
                                  @FilterParameter("父角色") Role parentRole,
                                  @FilterParameter("顺序") int position,
                                  @FilterParameter("子角色") Role childRole) {
        if (childRole.assignGlobalParentRole(user.getCode(), position, parentRole)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功令角色 " + childRole.getSimpleDescription() + " 全局继承自 " + parentRole.getSimpleDescription() + "，顺序为 " + position);
        } else {
            user.sendError("没有成功令角色 " + childRole.getSimpleDescription() + " 全局继承自 " + parentRole.getSimpleDescription() + "，" +
                    "可能是位置错误，或它们已经具备继承和派生关系");
        }
    }

    @Filter(Words.ROLE + " {子角色} " + Words.GLOBAL + Words.EXTENDS + " {父角色}")
    @Filter(Words.ADD + Words.ROLE + Words.GLOBAL + Words.ROLE + " {父角色}")
    @Required("permission.admin.role.global.role.add")
    public void addRoleGlobalRole(XiaomingUser user,
                                  @FilterParameter("父角色") Role parentRole,
                                  @FilterParameter("子角色") Role childRole) {
        if (childRole.assignGlobalParentRole(user.getCode(), parentRole)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功令角色 " + childRole.getSimpleDescription() + " 全局继承自 " + parentRole.getSimpleDescription());
        } else {
            user.sendError("没有成功令角色 " + childRole.getSimpleDescription() + " 全局继承自 " + parentRole.getSimpleDescription() + "，" +
                    "可能是它们已经具备继承和派生关系");
        }
    }

    @Filter(Words.REMOVE + Words.ROLE + " {子角色} " + Words.GLOBAL + Words.EXTENDS + " {父角色}")
    @Filter(Words.REMOVE + Words.ROLE + Words.GLOBAL + Words.ROLE + " {父角色}")
    @Required("permission.admin.role.global.role.remove")
    public void removeRoleGlobalRole(XiaomingUser user,
                                     @FilterParameter("父角色") Role parentRole,
                                     @FilterParameter("子角色") Role childRole) {
        if (childRole.revokeGlobalParentRole(user.getCode(), parentRole)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功令角色 " + childRole.getSimpleDescription() + " 不再全局继承自 " + parentRole.getSimpleDescription());
        } else {
            user.sendError("没有成功令角色 " + childRole.getSimpleDescription() + " 不再全局继承自 " + parentRole.getSimpleDescription() + "，" +
                    "可能是它们并不具有继承和派生关系");
        }
    }

    /** 角色与全局权限 */
    @Filter(Words.GRANT + Words.ROLE + " {角色} {r:权限节点}")
    @Filter(Words.ADD + Words.ROLE + Words.GLOBAL + Words.PERMISSION + " {角色} {r:权限节点}")
    @Required("permission.admin.role.global.permission.add")
    public void addRoleGlobalPermission(XiaomingUser user,
                                        @FilterParameter("角色") Role role,
                                        @FilterParameter("权限节点") Permission permission) {
        if (role.grantGlobalPermission(user.getCode(), permission)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功授予角色 " + role.getSimpleDescription() + " 全局权限：" + permission);
        } else {
            user.sendError("授予角色 " + role.getSimpleDescription() + " 全局权限：" + permission + " 失败，可能是该角色已具备该权限");
        }
    }

    @Filter(Words.GRANT + Words.ROLE + " {角色} {顺序} {r:权限节点}")
    @Filter(Words.ADD + Words.ROLE + Words.GLOBAL + Words.PERMISSION + " {角色} {顺序} {r:权限节点}")
    @Required("permission.admin.role.global.permission.add")
    public void addRoleGlobalPermission(XiaomingUser user,
                                        @FilterParameter("角色") Role role,
                                        @FilterParameter("顺序") int position,
                                        @FilterParameter("权限节点") Permission permission) {
        if (role.grantGlobalPermission(user.getCode(), position, permission)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功授予角色 " + role.getSimpleDescription() + " 全局权限：" + permission + "，顺序为 " + position);
        } else {
            user.sendError("授予角色 " + role.getSimpleDescription() + " 全局权限：" + permission + " 失败，可能顺序错误或该角色已具备该权限");
        }
    }

    @Filter(Words.REVOKE + Words.ROLE + " {角色} {r:权限节点}")
    @Filter(Words.REMOVE + Words.ROLE + Words.GLOBAL + Words.PERMISSION + " {角色} {r:权限节点}")
    @Required("permission.admin.role.global.permission.remove")
    public void removeRoleGlobalPermission(XiaomingUser user,
                                           @FilterParameter("角色") Role role,
                                           @FilterParameter("权限节点") Permission permission) {
        if (role.revokeGlobalPermission(user.getCode(), permission)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功删除角色 " + role.getSimpleDescription() + " 全局权限：" + permission);
        } else {
            user.sendError("删除角色 " + role.getSimpleDescription() + " 全局权限：" + permission + " 失败，可能该角色并没有这个全局权限");
        }
    }

    /** 角色和群权限 */
    @Filter(Words.ADD + Words.ROLE + Words.GROUP + Words.PERMISSION + " {角色} {群标签} {权限节点}")
    @Required("permission.admin.role.group.{arg.群标签}.permission.add")
    public void addRoleGroupPermission(XiaomingUser user,
                                       @FilterParameter("角色") Role role,
                                       @FilterParameter("群标签") String groupTag,
                                       @FilterParameter("权限节点") Permission permission) {
        if (role.grantGroupPermission(user.getCode(), groupTag, permission)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功授予角色 " + role.getSimpleDescription() + " 在群 %" + groupTag + " 中的权限：" + permission);
        } else {
            user.sendError("授予角色 " + role.getSimpleDescription() + " 在群 %" + groupTag + " 中的权限：" + permission + " 失败，可能是该角色在这个群中已具备该权限");
        }
    }

    @Filter(Words.ADD + Words.ROLE + Words.GROUP + Words.PERMISSION + " {角色} {群标签} {顺序} {权限节点}")
    @Required("permission.admin.role.group.{arg.群标签}.permission.remove")
    public void addRoleGroupPermission(XiaomingUser user,
                                       @FilterParameter("角色") Role role,
                                       @FilterParameter("群标签") String groupTag,
                                       @FilterParameter("顺序") int position,
                                       @FilterParameter("权限节点") Permission permission) {
        if (role.grantGroupPermission(user.getCode(), groupTag, position, permission)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功授予角色 " + role.getSimpleDescription() + " 在群 %" + groupTag + " 中的权限：" + permission + "，顺序为 " + position);
        } else {
            user.sendError("授予角色 " + role.getSimpleDescription() + " 在群 %" + groupTag + " 中的权限：" + permission + " 失败，可能是顺序错误或该角色在这个群中已具备该权限");
        }
    }

    @Filter(Words.REMOVE + Words.ROLE + Words.GROUP + Words.PERMISSION + " {角色} {群标签} {权限节点}")
    @Required("permission.admin.role.group.{arg.群标签}.permission.remove")
    public void removeRoleGroupPermission(XiaomingUser user,
                                          @FilterParameter("角色") Role role,
                                          @FilterParameter("群标签") String groupTag,
                                          @FilterParameter("权限节点") Permission permission) {
        if (role.dismissGroupPermission(user.getCode(), groupTag, permission)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功删除角色 " + role.getSimpleDescription() + " 在群 %" + groupTag + " 中的权限：" + permission);
        } else {
            user.sendError("删除角色 " + role.getSimpleDescription() + " 在群 %" + groupTag + " 中的权限：" + permission + " 失败，可能是该角色在这个群并没有此权限");
        }
    }

    /** 角色和群角色 */
    @Filter(Words.ADD + Words.ROLE + Words.GROUP + Words.ROLE + " {子角色} {群标签} {父角色}")
    @Required("permission.admin.role.group.{arg.群标签}.role.add")
    public void addRoleGroupRole(XiaomingUser user,
                                 @FilterParameter("子角色") Role childRole,
                                 @FilterParameter("群标签") String groupTag,
                                 @FilterParameter("父角色") Role parentRole) {
        if (childRole.assignGroupParentRole(user.getCode(), groupTag, parentRole)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功令角色 " + childRole.getSimpleDescription() + " 在群 %" + groupTag + " 中继承自角色 " + parentRole.getSimpleDescription());
        } else {
            user.sendError("令角色 " + childRole.getSimpleDescription() + " 在群 %" + groupTag + " 中继承自角色 " + parentRole.getSimpleDescription() + " 失败，可能是它们已经具备继承派生关系");
        }
    }

    @Filter(Words.ADD + Words.ROLE + Words.GROUP + Words.ROLE + " {子角色} {群标签} {顺序} {父角色}")
    @Required("permission.admin.role.group.{arg.群标签}.role.add")
    public void addRoleGroupRole(XiaomingUser user,
                                 @FilterParameter("子角色") Role childRole,
                                 @FilterParameter("群标签") String groupTag,
                                 @FilterParameter("顺序") int position,
                                 @FilterParameter("父角色") Role parentRole) {
        if (childRole.assignGroupParentRole(user.getCode(), groupTag, position, parentRole)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功令角色 " + childRole.getSimpleDescription() + " 在群 %" + groupTag + " 中继承自角色 " + parentRole.getSimpleDescription() + "，顺序为 " + position);
        } else {
            user.sendError("令角色 " + childRole.getSimpleDescription() + " 在群 %" + groupTag + " 中继承自角色 " + parentRole.getSimpleDescription() + " 失败，可能是顺序错误或它们已经具备继承派生关系");
        }
    }

    @Filter(Words.REMOVE + Words.ROLE + Words.GROUP + Words.ROLE + " {子角色} {群标签} {父角色}")
    @Required("permission.admin.role.group.{arg.群标签}.role.remove")
    public void removeRoleGroupRole(XiaomingUser user,
                                    @FilterParameter("子角色") Role childRole,
                                    @FilterParameter("群标签") String groupTag,
                                    @FilterParameter("父角色") Role parentRole) {
        if (childRole.assignGroupParentRole(user.getCode(), groupTag, parentRole)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功令角色 " + childRole.getSimpleDescription() + " 在群 %" + groupTag + " 中继承自角色 " + parentRole.getSimpleDescription());
        } else {
            user.sendError("令角色 " + childRole.getSimpleDescription() + " 在群 %" + groupTag + " 中继承自角色 " + parentRole.getSimpleDescription() + " 失败，可能是它们已经具备继承派生关系");
        }
    }

    /** 测试权限 */
    @Filter(Words.TEST + Words.ROLE + Words.PERMISSION + " {角色} {r:权限}")
    @Filter(Words.TEST + Words.ROLE + Words.PERMISSION + " {角色} {r:权限}")
    @Required("permission.admin.role.globalPermission.test")
    public void testRolePermission(XiaomingUser user,
                                   @FilterParameter("角色") Role account,
                                   @FilterParameter("权限") Permission permission) {
        final boolean accessible = account.accessibleGlobal(permission) == Accessible.ACCESSIBLE;
        user.sendMessage("角色 " + account.getSimpleDescription() + (accessible ? "具有" : "没有") + "全局权限「" + permission.toString() + "」");
    }

    @Filter(Words.TEST + Words.ROLE + Words.GROUP + Words.PERMISSION + " {角色} {群标签} {r:权限}")
    @Required("permission.admin.role.groupPermission.test")
    public void testRoleGroupPermission(XiaomingUser user,
                                        @FilterParameter("群标签") String groupTag,
                                        @FilterParameter("角色") Role account,
                                        @FilterParameter("权限") Permission permission) {
        final boolean accessible = account.accessibleGroup(groupTag, permission) == Accessible.ACCESSIBLE;
        user.sendMessage("角色" + account.getSimpleDescription() + "在群聊 %" + groupTag + " 中" + (accessible ? "具有" : "没有") + "权限「" + permission.toString() + "」");
    }

    @Filter(Words.TEST + Words.ROLE + Words.THIS + Words.GROUP + Words.PERMISSION + " {角色} {r:权限}")
    @Required("permission.admin.role.groupPermission.test")
    public void testRoleThisGroupPermission(GroupXiaomingUser user,
                                            @FilterParameter("角色") Role account,
                                            @FilterParameter("权限") Permission permission) {
        final String groupTag = user.getGroupCodeString();
        final boolean accessible = account.accessibleGroup(groupTag, permission) == Accessible.ACCESSIBLE;
        user.sendMessage("角色" + account.getSimpleDescription() + "在群聊 %" + groupTag + " 中" + (accessible ? "具有" : "没有") + "权限「" + permission.toString() + "」");
    }
}
