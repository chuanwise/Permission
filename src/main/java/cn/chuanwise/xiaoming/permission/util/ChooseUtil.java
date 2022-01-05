package cn.chuanwise.xiaoming.permission.util;

import cn.chuanwise.api.SimpleDescribable;
import cn.chuanwise.util.CollectionUtil;
import cn.chuanwise.util.NumberUtil;
import cn.chuanwise.util.StaticUtil;
import cn.chuanwise.util.StringUtil;
import cn.chuanwise.xiaoming.permission.PermissionPlugin;
import cn.chuanwise.xiaoming.permission.PermissionSystem;
import cn.chuanwise.xiaoming.permission.permission.Authorizer;
import cn.chuanwise.xiaoming.permission.permission.Role;
import cn.chuanwise.xiaoming.user.XiaomingUser;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class ChooseUtil extends StaticUtil {
    public static <T extends SimpleDescribable> Optional<T> choose(XiaomingUser user, String inputValue,
                                                                   String what, Collection<T> collection,
                                                                   Function<Long, Optional<T>> getter,
                                                                   Function<String, List<T>> tagGetter,
                                                                   Function<String, List<T>> searcher) {
        if (collection.isEmpty()) {
            user.sendError("没有任何" + what + "，无法选择");
            return Optional.empty();
        }

        if (inputValue.startsWith("#")) {
            if (Objects.isNull(getter)) {
                user.sendError("不支持使用编号指定" + what + "！");
                return Optional.empty();
            }

            final String codeString = inputValue.substring(1);
            if (StringUtil.isEmpty(codeString)) {
                user.sendError(what + "编号不能为空！");
                return Optional.empty();
            }
            final Optional<Long> optionalCode = NumberUtil.parseLong(codeString);

            if (optionalCode.isPresent()) {
                final long roleCode = optionalCode.get();
                final Optional<T> optionalT = getter.apply(roleCode);
                if (!optionalT.isPresent()) {
                    user.sendError("没有找到编号为 #" + roleCode + " 的" + what);
                    return Optional.empty();
                } else {
                    return optionalT;
                }
            } else {
                user.sendError("「" + codeString + "」并不是一个合理的数字哦");
                return Optional.empty();
            }
        }

        if (inputValue.startsWith("%")) {
            if (Objects.isNull(tagGetter)) {
                user.sendError("不允许使用标签搜索" + what + "！");
                return Optional.empty();
            }

            final String tagString = inputValue.substring(1);
            if (StringUtil.isEmpty(tagString)) {
                user.sendError(what + "标签不能为空！");
                return Optional.empty();
            }

            final List<T> searchResults = tagGetter.apply(tagString);
            if (searchResults.isEmpty()) {
                user.sendError("用标签 %" + tagString + " 没有搜索到任何" + what);
                return Optional.empty();
            } else if (searchResults.size() == 1) {
                final T element = searchResults.iterator().next();
//                user.sendMessage("用标签 %" + tagString + " 搜索到一个" + what + "：" + element.getSimpleDescription() + "，已自动选择");
                return Optional.of(element);
            } else {
                user.sendMessage("用标签 %" + tagString + " 搜索到 " + searchResults.size() + " 个" + what + "：\n" +
                        CollectionUtil.toString(searchResults, T::getSimpleDescription, "\n") + "\n" +
                        "要选择哪一个" + what + "呢？告诉小明它的编号吧！");
                String codeString = user.nextMessageOrExit().serialize();
                final int lastSpiltterPosition = codeString.lastIndexOf("#");
                if (lastSpiltterPosition != 0) {
                    codeString = codeString.substring(lastSpiltterPosition + 1);
                }

                if (StringUtil.isEmpty(codeString)) {
                    user.sendError(what + "编号不能为空！");
                    return Optional.empty();
                } else {
                    final Optional<Long> optionalCode = NumberUtil.parseLong(codeString);

                    if (optionalCode.isPresent()) {
                        final long roleCode = optionalCode.get();
                        final Optional<T> optionalT = getter.apply(roleCode);
                        if (!optionalT.isPresent()) {
                            user.sendError("没有找到编号为 #" + roleCode + " 的" + what);
                            return Optional.empty();
                        } else {
                            return optionalT;
                        }
                    } else {
                        user.sendError("「" + codeString + "」并不是一个合理的数字哦");
                        return Optional.empty();
                    }
                }
            }
        }

        if (Objects.isNull(searcher)) {
            user.sendError("不允许搜索" + what + "！");
            return Optional.empty();
        }
        final List<T> searchResults = searcher.apply(inputValue);
        if (searchResults.isEmpty()) {
            user.sendError("用关键字 " + inputValue + " 没有搜索到任何" + what);
            return Optional.empty();
        } else if (searchResults.size() == 1) {
            final T element = searchResults.iterator().next();
//            user.sendMessage("用关键字 " + inputValue + " 搜索到一个" + what + "：" + element.getSimpleDescription() + "，已自动选择");
            return Optional.of(element);
        } else {
            user.sendMessage("用关键字 " + inputValue + " 搜索到 " + searchResults.size() + " 个" + what + "：\n" +
                    CollectionUtil.toString(searchResults, T::getSimpleDescription, "\n") + "\n" +
                    "要选择哪一个" + what + "呢？告诉小明它的编号吧！");
            String codeString = user.nextMessageOrExit().serialize();
            final int lastSpiltterPosition = codeString.lastIndexOf("#");
            if (lastSpiltterPosition != 0) {
                codeString = codeString.substring(lastSpiltterPosition + 1);
            }

            if (StringUtil.isEmpty(codeString)) {
                user.sendError(what + "编号不能为空！");
                return Optional.empty();
            } else {
                final Optional<Long> optionalCode = NumberUtil.parseLong(codeString);

                if (optionalCode.isPresent()) {
                    final long roleCode = optionalCode.get();
                    final Optional<T> optionalT = getter.apply(roleCode);
                    if (!optionalT.isPresent()) {
                        user.sendError("没有找到编号为 #" + roleCode + " 的" + what);
                        return Optional.empty();
                    } else {
                        return optionalT;
                    }
                } else {
                    user.sendError("「" + codeString + "」并不是一个合理的数字哦");
                    return Optional.empty();
                }
            }
        }
    }

    public static Optional<Role> chooseRole(XiaomingUser user, String input) {
        final PermissionSystem permissionSystem = PermissionPlugin.INSTANCE.getPermissionSystem();

        return choose(user, input, "角色", permissionSystem.getRoles().values(),
                permissionSystem::getRole,
                permissionSystem::searchRolesByTag,
                permissionSystem::searchRoles);
    }

    public static Optional<Authorizer> chooseAccount(XiaomingUser user, String input) {
        final PermissionSystem permissionSystem = PermissionPlugin.INSTANCE.getPermissionSystem();

        return choose(user, input, "账号", permissionSystem.getAuthorizers().values(),
                permissionSystem::getAuthorizer,
                permissionSystem::searchAccountsByTag,
                permissionSystem::searchAccounts);
    }
}
