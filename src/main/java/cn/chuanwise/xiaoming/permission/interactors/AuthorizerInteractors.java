package cn.chuanwise.xiaoming.permission.interactors;

import cn.chuanwise.util.CollectionUtil;
import cn.chuanwise.xiaoming.annotation.Filter;
import cn.chuanwise.xiaoming.annotation.FilterParameter;
import cn.chuanwise.xiaoming.annotation.Required;
import cn.chuanwise.xiaoming.interactor.SimpleInteractors;
import cn.chuanwise.xiaoming.permission.Accessible;
import cn.chuanwise.xiaoming.permission.Permission;
import cn.chuanwise.xiaoming.permission.PermissionPlugin;
import cn.chuanwise.xiaoming.permission.PermissionSystem;
import cn.chuanwise.xiaoming.permission.object.PermissionPluginObject;
import cn.chuanwise.xiaoming.permission.permission.Authorizer;
import cn.chuanwise.xiaoming.permission.permission.Role;
import cn.chuanwise.xiaoming.permission.util.PermissionPluginUtil;
import cn.chuanwise.xiaoming.permission.util.Words;
import cn.chuanwise.xiaoming.user.GroupXiaomingUser;
import cn.chuanwise.xiaoming.user.XiaomingUser;

import java.util.Optional;

public class AuthorizerInteractors
        extends SimpleInteractors<PermissionPlugin>
        implements PermissionPluginObject {

    private PermissionSystem permissionSystem;

    @Override
    public void onRegister() {
        permissionSystem = getPermissionSystem();
    }

    @Filter(Words.USER + Words.PERMISSION + " {r:账号}")
    @Required("permission.admin.authorizer.look")
    public void lookAuthorizer(XiaomingUser user, @FilterParameter("账号") Authorizer authorizer) {
        user.sendMessage("『用户权限』\n" +
                "【用户】" + authorizer.getAccount().getAliasAndCode() + "\n" +
                "【编号】" + authorizer.getAuthorizerCode() + "\n" +
                "【标签】" + CollectionUtil.toString(authorizer.getTags()) + "\n" +
                "【全局】\n" + PermissionPluginUtil.toDetailString(authorizer.getGlobalScope()) + "\n" +
                "【群聊】" + Optional.ofNullable(CollectionUtil.toString(authorizer.getGroups().entrySet(), entry -> {
                    return "%" + entry.getKey() + "：\n" + PermissionPluginUtil.toDetailString(entry.getValue());
                })).orElse("（无）")
        );
    }

    /** 账户和全局权限 */
    @Filter(Words.GRANT + " {账户} {权限节点}")
    @Filter(Words.GRANT + " {账户} {权限节点}")
    @Filter(Words.GRANT + Words.ACCOUNT + " {账户} {权限节点}")
    @Filter(Words.GRANT + Words.USER + " {账户} {权限节点}")
    @Filter(Words.ADD + Words.ACCOUNT + Words.GLOBAL + Words.PERMISSION + " {账户} {权限节点}")
    @Filter(Words.ADD + Words.USER + Words.GLOBAL + Words.PERMISSION + " {账户} {权限节点}")
    @Required("permission.admin.authorizer.global.permission.add")
    public void addAuthorizerGlobalPermission(XiaomingUser user,
                                              @FilterParameter("账户") Authorizer authorizer,
                                              @FilterParameter("权限节点") Permission permission) {
        final String authorizerAliasAndCode = xiaomingBot.getAccountManager().getAliasAndCode(authorizer.getAuthorizerCode());
        if (authorizer.grantGlobalPermission(user.getCode(), permission)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功授予账户 " + authorizerAliasAndCode + " 全局权限：" + permission);
        } else {
            user.sendError("授予账户 " + authorizerAliasAndCode + " 全局权限：" + permission + " 失败，可能是已经具备该权限");
        }
    }

    @Filter(Words.GRANT + Words.ACCOUNT + " {账户} {顺序} {权限节点}")
    @Filter(Words.GRANT + Words.USER + " {账户} {顺序} {权限节点}")
    @Filter(Words.ADD + Words.ACCOUNT + Words.GLOBAL + Words.PERMISSION + " {账户} {顺序} {权限节点}")
    @Filter(Words.ADD + Words.USER + Words.GLOBAL + Words.PERMISSION + " {账户} {顺序} {权限节点}")
    @Required("permission.admin.authorizer.global.permission.add")
    public void addAuthorizerGlobalPermission(XiaomingUser user,
                                              @FilterParameter("账户") Authorizer authorizer,
                                              @FilterParameter("顺序") int position,
                                              @FilterParameter("权限节点") Permission permission) {
        final String authorizerAliasAndCode = xiaomingBot.getAccountManager().getAliasAndCode(authorizer.getAuthorizerCode());
        if (authorizer.grantGlobalPermission(user.getCode(), position, permission)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功授予账户 " + authorizerAliasAndCode + " 全局权限：" + permission + "，顺序为 " + position);
        } else {
            user.sendError("授予账户 " + authorizerAliasAndCode + " 全局权限：" + permission + " 失败，可能是顺序错误或已经具备该权限");
        }
    }

    @Filter(Words.REMOVE + Words.ACCOUNT + " {账户} {权限节点}")
    @Filter(Words.REMOVE + Words.USER + " {账户} {权限节点}")
    @Filter(Words.REMOVE + Words.ACCOUNT + Words.GLOBAL + Words.PERMISSION + " {账户} {权限节点}")
    @Filter(Words.REMOVE + Words.USER + Words.GLOBAL + Words.PERMISSION + " {账户} {顺序} {权限节点}")
    @Filter(Words.REVOKE + Words.ACCOUNT + " {账户} {权限节点}")
    @Filter(Words.REVOKE + Words.USER + " {账户} {权限节点}")
    @Filter(Words.REVOKE + Words.ACCOUNT + Words.GLOBAL + Words.PERMISSION + " {账户} {权限节点}")
    @Filter(Words.REVOKE + Words.USER + Words.GLOBAL + Words.PERMISSION + " {账户} {权限节点}")
    @Required("permission.admin.authorizer.global.permission.remove")
    public void removeAuthorizerGlobalPermission(XiaomingUser user,
                                              @FilterParameter("账户") Authorizer authorizer,
                                              @FilterParameter("权限节点") Permission permission) {
        final String authorizerAliasAndCode = xiaomingBot.getAccountManager().getAliasAndCode(authorizer.getAuthorizerCode());
        if (authorizer.grantGlobalPermission(user.getCode(), permission)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功删除账户 " + authorizerAliasAndCode + " 全局权限：" + permission);
        } else {
            user.sendError("删除账户 " + authorizerAliasAndCode + " 全局权限：" + permission + " 失败，可能是并不具备该权限");
        }
    }

    /** 账户和全局角色 */
    @Filter(Words.LET + " {账号} {角色}")
    @Filter(Words.ADD + Words.ACCOUNT + Words.GLOBAL + Words.ROLE + " {账号} {角色}")
    @Filter(Words.ADD + Words.ACCOUNT + Words.GLOBAL + Words.GLOBAL + Words.ROLE + " {账号} {角色}")
    @Filter(Words.ADD + Words.USER + Words.GLOBAL + Words.ROLE + " {账号} {角色}")
    @Filter(Words.ADD + Words.USER + Words.GLOBAL + Words.GLOBAL + Words.ROLE + " {账号} {角色}")
    @Required("permission.admin.role.global.role.add")
    public void addAuthorizerGlobalRole(XiaomingUser user,
                                     @FilterParameter("账号") Authorizer authorizer,
                                     @FilterParameter("角色") Role role) {
        if (authorizer.assignGlobalRole(user.getCode(), role.getRoleCode())) {
            permissionSystem.readyToSave();
            user.sendMessage("添加" + authorizer.getSimpleDescription() + " 全局角色 " + role.getName() + " 成功");
        } else {
            user.sendError("添加" + authorizer.getSimpleDescription() + " 全局角色 " + role.getName() + " 失败，可能是已经是该角色");
        }
    }

    @Filter(Words.ADD + Words.ACCOUNT + Words.GLOBAL + Words.ROLE + " {账号} {顺序} {角色}")
    @Filter(Words.ADD + Words.ACCOUNT + Words.GLOBAL + Words.GLOBAL + Words.ROLE + " {账号} {顺序} {角色}")
    @Filter(Words.ADD + Words.USER + Words.GLOBAL + Words.ROLE + " {账号} {顺序} {角色}")
    @Filter(Words.ADD + Words.USER + Words.GLOBAL + Words.GLOBAL + Words.ROLE + " {账号} {顺序} {角色}")
    @Required("permission.admin.role.global.role.add")
    public void addAuthorizerGlobalRole(XiaomingUser user,
                                     @FilterParameter("账号") Authorizer authorizer,
                                     @FilterParameter("顺序") int position,
                                     @FilterParameter("角色") Role role) {
        if (authorizer.assignGlobalRole(user.getCode(), position, role.getRoleCode())) {
            permissionSystem.readyToSave();
            user.sendMessage("添加" + authorizer.getSimpleDescription() + " 全局角色 " + role.getName() + " 成功，顺序为 " + position);
        } else {
            user.sendError("添加" + authorizer.getSimpleDescription() + " 全局角色 " + role.getName() + " 失败，可能是顺序错误或已经是该角色");
        }
    }

    @Filter(Words.REMOVE + Words.ACCOUNT + Words.GLOBAL + Words.ROLE + " {账号} {角色}")
    @Filter(Words.REMOVE + Words.ACCOUNT + Words.GLOBAL + Words.GLOBAL + Words.ROLE + " {账号} {角色}")
    @Filter(Words.REMOVE + Words.USER + Words.GLOBAL + Words.ROLE + " {账号} {角色}")
    @Filter(Words.REMOVE + Words.USER + Words.GLOBAL + Words.GLOBAL + Words.ROLE + " {账号} {角色}")
    @Required("permission.admin.role.global.role.remove")
    public void removeAuthorizerGlobalRole(XiaomingUser user,
                                        @FilterParameter("账号") Authorizer authorizer,
                                        @FilterParameter("角色") Role role) {
        if (authorizer.dismissGlobalRole(user.getCode(), role.getRoleCode())) {
            permissionSystem.readyToSave();
            user.sendMessage("添加" + authorizer.getSimpleDescription() + " 全局角色 " + role.getName() + " 成功");
        } else {
            user.sendError("添加" + authorizer.getSimpleDescription() + " 全局角色 " + role.getName() + " 失败，可能是他并不是该角色");
        }
    }

    /** 账号和群角色 */
    @Filter(Words.ADD + Words.ACCOUNT + Words.GROUP + Words.ROLE + " {账户} {群标签} {角色}")
    @Filter(Words.ADD + Words.USER + Words.GROUP + Words.ROLE + " {账户} {群标签} {角色}")
    @Required("permission.admin.authorizer.group.{arg.群标签}.role.add")
    public void addAuthorizerGroupRole(XiaomingUser user,
                                    @FilterParameter("账户") Authorizer authorizer,
                                    @FilterParameter("群标签") String groupTag,
                                    @FilterParameter("角色") Role role) {
        final String authorizerAliasAndCode = xiaomingBot.getAccountManager().getAliasAndCode(authorizer.getAuthorizerCode());
        if (authorizer.assignGroupRole(user.getCode(), groupTag, role)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功令 " + authorizerAliasAndCode + " 在群 %" + groupTag + " 具有角色 " + role.getSimpleDescription() + " 的权限");
        } else {
            user.sendError("令 " + authorizerAliasAndCode + " 在群 %" + groupTag + " 具有角色 " + role.getSimpleDescription() + " 的权限失败，可能是已经是该角色");
        }
    }

    @Filter(Words.ADD + Words.ACCOUNT + Words.GROUP + Words.ROLE + " {账户} {群标签} {顺序} {角色}")
    @Filter(Words.ADD + Words.USER + Words.GROUP + Words.ROLE + " {账户} {群标签} {顺序} {角色}")
    @Required("permission.admin.authorizer.group.{arg.群标签}.role.add")
    public void addAuthorizerGroupRole(XiaomingUser user,
                                    @FilterParameter("账户") Authorizer authorizer,
                                    @FilterParameter("顺序") int position,
                                    @FilterParameter("群标签") String groupTag,
                                    @FilterParameter("角色") Role role) {
        final String authorizerAliasAndCode = xiaomingBot.getAccountManager().getAliasAndCode(authorizer.getAuthorizerCode());
        if (authorizer.assignGroupRole(user.getCode(), groupTag, position, role)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功令 " + authorizerAliasAndCode + " 在群 %" + groupTag + " 具有角色 " + role.getSimpleDescription() + " 的权限，顺序为 " + position);
        } else {
            user.sendError("令 " + authorizerAliasAndCode + " 在群 %" + groupTag + " 具有角色 " + role.getSimpleDescription() + " 的权限失败，可能是顺序错误或已经是该角色");
        }
    }

    @Filter(Words.REMOVE + Words.ACCOUNT + Words.GROUP + Words.ROLE + " {账户} {群标签} {角色}")
    @Filter(Words.REMOVE + Words.USER + Words.GROUP + Words.ROLE + " {账户} {群标签} {角色}")
    @Required("permission.admin.authorizer.group.{arg.群标签}.role.remove")
    public void removeAuthorizerGroupRole(XiaomingUser user,
                                       @FilterParameter("账户") Authorizer authorizer,
                                       @FilterParameter("群标签") String groupTag,
                                       @FilterParameter("角色") Role role) {
        final String authorizerAliasAndCode = xiaomingBot.getAccountManager().getAliasAndCode(authorizer.getAuthorizerCode());
        if (authorizer.dismissGroupRole(user.getCode(), groupTag, role)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功删除 " + authorizerAliasAndCode + " 在群 %" + groupTag + " 中的角色 " + role.getSimpleDescription());
        } else {
            user.sendError("删除 " + authorizerAliasAndCode + " 在群 %" + groupTag + " 的角色 " + role.getSimpleDescription() + " 失败，可能它并不是该角色");
        }
    }

    /** 账号和群权限 */
    @Filter(Words.ADD + Words.ACCOUNT + Words.GROUP + Words.PERMISSION + " {账户} {群标签} {权限节点}")
    @Filter(Words.ADD + Words.USER + Words.GROUP + Words.PERMISSION + " {账户} {群标签} {权限节点}")
    @Required("permission.admin.authorizer.group.{arg.群标签}.permission.add")
    public void addAuthorizerGroupPermission(XiaomingUser user,
                                    @FilterParameter("账户") Authorizer authorizer,
                                    @FilterParameter("群标签") String groupTag,
                                    @FilterParameter("权限节点") Permission permission) {
        final String authorizerAliasAndCode = xiaomingBot.getAccountManager().getAliasAndCode(authorizer.getAuthorizerCode());
        if (authorizer.grantGroupPermission(user.getCode(), groupTag, permission)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功添加 " + authorizerAliasAndCode + " 在群 %" + groupTag + " 中的权限 " + permission);
        } else {
            user.sendError("添加 " + authorizerAliasAndCode + " 在群 %" + groupTag + " 中的权限 " + permission + " 失败，可能是已经具备该权限");
        }
    }

    @Filter(Words.ADD + Words.ACCOUNT + Words.GROUP + Words.PERMISSION + " {账户} {群标签} {顺序} {权限节点}")
    @Filter(Words.ADD + Words.USER + Words.GROUP + Words.PERMISSION + " {账户} {群标签} {顺序} {权限节点}")
    @Required("permission.admin.authorizer.group.{arg.群标签}.permission.add")
    public void addAuthorizerGroupPermission(XiaomingUser user,
                                    @FilterParameter("账户") Authorizer authorizer,
                                    @FilterParameter("顺序") int position,
                                    @FilterParameter("群标签") String groupTag,
                                    @FilterParameter("权限节点") Permission permission) {
        final String authorizerAliasAndCode = xiaomingBot.getAccountManager().getAliasAndCode(authorizer.getAuthorizerCode());
        if (authorizer.grantGroupPermission(user.getCode(), groupTag, position, permission)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功添加 " + authorizerAliasAndCode + " 在群 %" + groupTag + " 中的权限 " + permission + "，顺序为 " + position);
        } else {
            user.sendError("添加 " + authorizerAliasAndCode + " 在群 %" + groupTag + " 中的权限 " + permission + " 失败，可能是顺序错误或已经具备该权限");
        }
    }

    @Filter(Words.REMOVE + Words.ACCOUNT + Words.GROUP + Words.PERMISSION + " {账户} {群标签} {权限节点}")
    @Filter(Words.REMOVE + Words.USER + Words.GROUP + Words.PERMISSION + " {账户} {群标签} {权限节点}")
    @Required("permission.admin.authorizer.group.{arg.群标签}.permission.remove")
    public void removeAuthorizerGroupPermission(XiaomingUser user,
                                       @FilterParameter("账户") Authorizer authorizer,
                                       @FilterParameter("群标签") String groupTag,
                                       @FilterParameter("权限节点") Permission permission) {
        final String authorizerAliasAndCode = xiaomingBot.getAccountManager().getAliasAndCode(authorizer.getAuthorizerCode());
        if (authorizer.revokeGroupPermission(user.getCode(), groupTag, permission)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功删除 " + authorizerAliasAndCode + " 在群 %" + groupTag + " 中的权限 " + permission);
        } else {
            user.sendError("删除 " + authorizerAliasAndCode + " 在群 %" + groupTag + " 中的权限 " + permission + " 失败，可能是并不具备该权限");
        }
    }

    /** 账号和本群角色 */
    @Filter(Words.ADD + Words.ACCOUNT + Words.THIS + Words.GROUP + Words.ROLE + " {账户} {角色}")
    @Filter(Words.ADD + Words.USER + Words.THIS + Words.GROUP + Words.ROLE + " {账户} {角色}")
    @Required("permission.admin.authorizer.group.{user.groupCode}.role.add")
    public void addAuthorizerCurrentGroupRole(GroupXiaomingUser user,
                                       @FilterParameter("账户") Authorizer authorizer,
                                       @FilterParameter("角色") Role role) {
        final String groupTag = user.getGroupCodeString();
        final String authorizerAliasAndCode = xiaomingBot.getAccountManager().getAliasAndCode(authorizer.getAuthorizerCode());
        if (authorizer.assignGroupRole(user.getCode(), groupTag, role)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功令 " + authorizerAliasAndCode + " 在本群具有角色 " + role.getSimpleDescription() + " 的权限");
        } else {
            user.sendError("令 " + authorizerAliasAndCode + " 在本群具有角色 " + role.getSimpleDescription() + " 的权限失败，可能是已经是该角色");
        }
    }

    @Filter(Words.ADD + Words.ACCOUNT + Words.THIS + Words.GROUP + Words.ROLE + " {账户} {顺序} {角色}")
    @Filter(Words.ADD + Words.USER + Words.THIS + Words.GROUP + Words.ROLE + " {账户} {顺序} {角色}")
    @Required("permission.admin.authorizer.group.{user.groupCode}.role.add")
    public void addAuthorizerCurrentGroupRole(GroupXiaomingUser user,
                                       @FilterParameter("账户") Authorizer authorizer,
                                       @FilterParameter("顺序") int position,
                                       @FilterParameter("角色") Role role) {
        final String groupTag = user.getGroupCodeString();
        final String authorizerAliasAndCode = xiaomingBot.getAccountManager().getAliasAndCode(authorizer.getAuthorizerCode());
        if (authorizer.assignGroupRole(user.getCode(), groupTag, position, role)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功令 " + authorizerAliasAndCode + " 在本群具有角色 " + role.getSimpleDescription() + " 的权限，顺序为 " + position);
        } else {
            user.sendError("令 " + authorizerAliasAndCode + " 在本群具有角色 " + role.getSimpleDescription() + " 的权限失败，可能是顺序错误或已经是该角色");
        }
    }

    @Filter(Words.REMOVE + Words.ACCOUNT + Words.THIS + Words.GROUP + Words.ROLE + " {账户} {角色}")
    @Filter(Words.REMOVE + Words.USER + Words.THIS + Words.GROUP + Words.ROLE + " {账户} {角色}")
    @Required("permission.admin.authorizer.group.{user.groupCode}.role.remove")
    public void removeAuthorizerCurrentGroupRole(GroupXiaomingUser user,
                                          @FilterParameter("账户") Authorizer authorizer,
                                          @FilterParameter("角色") Role role) {
        final String groupTag = user.getGroupCodeString();
        final String authorizerAliasAndCode = xiaomingBot.getAccountManager().getAliasAndCode(authorizer.getAuthorizerCode());
        if (authorizer.dismissGroupRole(user.getCode(), groupTag, role)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功删除 " + authorizerAliasAndCode + " 在本群中的角色 " + role.getSimpleDescription());
        } else {
            user.sendError("删除 " + authorizerAliasAndCode + " 在本群的角色 " + role.getSimpleDescription() + " 失败，可能它并不是该角色");
        }
    }

    /** 账号和本群权限 */
    @Filter(Words.ADD + Words.ACCOUNT + Words.THIS + Words.GROUP + Words.PERMISSION + " {账户} {权限节点}")
    @Filter(Words.ADD + Words.USER + Words.THIS + Words.GROUP + Words.PERMISSION + " {账户} {权限节点}")
    @Required("permission.admin.authorizer.group.{user.groupCode}.permission.add")
    public void addAuthorizerCurrentGroupPermission(GroupXiaomingUser user,
                                             @FilterParameter("账户") Authorizer authorizer,
                                             @FilterParameter("权限节点") Permission permission) {
        final String groupTag = user.getGroupCodeString();
        final String authorizerAliasAndCode = xiaomingBot.getAccountManager().getAliasAndCode(authorizer.getAuthorizerCode());
        if (authorizer.grantGroupPermission(user.getCode(), groupTag, permission)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功添加 " + authorizerAliasAndCode + " 在本群中的权限 " + permission);
        } else {
            user.sendError("添加 " + authorizerAliasAndCode + " 在本群中的权限 " + permission + " 失败，可能是已经具备该权限");
        }
    }

    @Filter(Words.ADD + Words.ACCOUNT + Words.THIS + Words.GROUP + Words.PERMISSION + " {账户} {顺序} {权限节点}")
    @Filter(Words.ADD + Words.USER + Words.THIS + Words.GROUP + Words.PERMISSION + " {账户} {顺序} {权限节点}")
    @Required("permission.admin.authorizer.group.{user.groupCode}.permission.add")
    public void addAuthorizerCurrentGroupPermission(GroupXiaomingUser user,
                                             @FilterParameter("账户") Authorizer authorizer,
                                             @FilterParameter("顺序") int position,
                                             @FilterParameter("权限节点") Permission permission) {
        final String groupTag = user.getGroupCodeString();
        final String authorizerAliasAndCode = xiaomingBot.getAccountManager().getAliasAndCode(authorizer.getAuthorizerCode());
        if (authorizer.grantGroupPermission(user.getCode(), groupTag, position, permission)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功添加 " + authorizerAliasAndCode + " 在本群中的权限 " + permission + "，顺序为 " + position);
        } else {
            user.sendError("添加 " + authorizerAliasAndCode + " 在本群中的权限 " + permission + " 失败，可能是顺序错误或已经具备该权限");
        }
    }

    @Filter(Words.REMOVE + Words.ACCOUNT + Words.THIS + Words.GROUP + Words.PERMISSION + " {账户} {权限节点}")
    @Filter(Words.REMOVE + Words.USER + Words.THIS + Words.GROUP + Words.PERMISSION + " {账户} {权限节点}")
    @Required("permission.admin.authorizer.group.{user.groupCode}.permission.remove")
    public void removeAuthorizerCurrentGroupPermission(GroupXiaomingUser user,
                                                @FilterParameter("账户") Authorizer authorizer,
                                                @FilterParameter("权限节点") Permission permission) {
        final String groupTag = user.getGroupCodeString();
        final String authorizerAliasAndCode = xiaomingBot.getAccountManager().getAliasAndCode(authorizer.getAuthorizerCode());
        if (authorizer.revokeGroupPermission(user.getCode(), groupTag, permission)) {
            permissionSystem.readyToSave();
            user.sendMessage("成功删除 " + authorizerAliasAndCode + " 在本群中的权限 " + permission);
        } else {
            user.sendError("删除 " + authorizerAliasAndCode + " 在本群中的权限 " + permission + " 失败，可能是并不具备该权限");
        }
    }



    /** 测试权限 */
    @Filter(Words.TEST + Words.USER + Words.PERMISSION + " {账号} {r:权限}")
    @Filter(Words.TEST + Words.ACCOUNT + Words.PERMISSION + " {账号} {r:权限}")
    @Filter(Words.TEST + Words.USER + Words.GLOBAL + Words.PERMISSION + " {账号} {r:权限}")
    @Filter(Words.TEST + Words.ACCOUNT + Words.GLOBAL + Words.PERMISSION + " {账号} {r:权限}")
    @Required("permission.admin.authorizer.globalPermission.test")
    public void testAuthorizerPermission(XiaomingUser user,
                                      @FilterParameter("账号") Authorizer authorizer,
                                      @FilterParameter("权限") Permission permission) {
        final boolean accessible = authorizer.accessibleGlobal(permission) == Accessible.ACCESSIBLE;
        user.sendMessage("用户 " + authorizer.getSimpleDescription() + (accessible ? "具有" : "没有") + "全局权限「" + permission.toString() + "」");
    }

    @Filter(Words.TEST + Words.USER + Words.GROUP + Words.PERMISSION + " {账号} {群标签} {r:权限}")
    @Filter(Words.TEST + Words.ACCOUNT + Words.GROUP + Words.PERMISSION + " {账号} {群标签} {r:权限}")
    @Required("permission.admin.authorizer.groupPermission.test")
    public void testAuthorizerGroupPermission(XiaomingUser user,
                                              @FilterParameter("群标签") String groupTag,
                                              @FilterParameter("账号") Authorizer authorizer,
                                              @FilterParameter("权限") Permission permission) {
        final boolean accessible = authorizer.accessibleGroup(groupTag, permission) == Accessible.ACCESSIBLE;
        user.sendMessage("用户" + authorizer.getSimpleDescription() + "在群聊 %" + groupTag + " 中" + (accessible ? "具有" : "没有") + "权限「" + permission.toString() + "」");
    }

    @Filter(Words.TEST + Words.USER + Words.THIS + Words.GROUP + Words.PERMISSION + " {账号} {r:权限}")
    @Filter(Words.TEST + Words.ACCOUNT + Words.THIS + Words.GROUP + Words.PERMISSION + " {账号} {r:权限}")
    @Required("permission.admin.authorizer.groupPermission.test")
    public void testAuthorizerThisGroupPermission(GroupXiaomingUser user,
                                               @FilterParameter("账号") Authorizer authorizer,
                                               @FilterParameter("权限") Permission permission) {
        final String groupTag = user.getGroupCodeString();
        final boolean accessible = authorizer.accessibleGroup(groupTag, permission) == Accessible.ACCESSIBLE;
        user.sendMessage("用户" + authorizer.getSimpleDescription() + "在群聊 %" + groupTag + " 中" + (accessible ? "具有" : "没有") + "权限「" + permission.toString() + "」");
    }

}
