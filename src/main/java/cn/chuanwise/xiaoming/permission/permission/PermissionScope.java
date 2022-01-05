package cn.chuanwise.xiaoming.permission.permission;

import cn.chuanwise.api.Flushable;
import cn.chuanwise.util.CollectionUtil;
import cn.chuanwise.util.ConditionUtil;
import cn.chuanwise.xiaoming.permission.Permission;
import cn.chuanwise.xiaoming.permission.Accessible;
import cn.chuanwise.xiaoming.permission.object.PermissionPluginObject;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PermissionScope
        implements PermissionPluginObject, Flushable {
    final List<Permission> permissions = new ArrayList<>();
    final List<Long> roleCodes = new ArrayList<>();

    public boolean grant(int position, Permission permission) {
        final Accessible accessible = accessible(permission);
        switch (accessible) {
            case ACCESSIBLE:
                return false;
            case UNACCESSIBLE:
            case UNKNOWN:
                if (position >= 0 && position <= permissions.size()) {
                    permissions.removeIf(x -> x.acceptable(permission) == Accessible.UNACCESSIBLE);
                    return permissions.add(permission);
                } else {
                    return false;
                }
            default:
                throw new NoSuchElementException();
        }
    }

    public boolean grant(Permission permission) {
        return grant(permissions.size(), permission);
    }

    public boolean grant(int position, String permission) {
        return grant(position, getPermissionSystem().createPermission(permission, getPlugin()));
    }

    public boolean grant(String permission) {
        return grant(getPermissionSystem().createPermission(permission, getPlugin()));
    }

    public boolean revoke(Permission permission) {
        final Accessible accessible = accessible(permission);
        switch (accessible) {
            case UNKNOWN:
                permissions.add(Permission.reversed(permission));
                return true;
            case ACCESSIBLE:
                return permissions.removeIf(p -> p.acceptable(permission) == Accessible.ACCESSIBLE);
            case UNACCESSIBLE:
                return false;
            default:
                throw new NoSuchElementException();
        }
    }

    public boolean revoke(int position) {
        return Objects.nonNull(permissions.remove(position));
    }

    public boolean revoke(String permission) {
        return revoke(getPermissionSystem().createPermission(permission, getPlugin()));
    }

    public boolean assign(int position, Role role) {
        ConditionUtil.notNull(role, "role");

        final long roleCode = role.getRoleCode();
        if (roleCodes.contains(roleCode)) {
            return false;
        } else {
            if (position >= 0 && position <= permissions.size()) {
                return roleCodes.add(role.roleCode);
            } else {
                return false;
            }
        }
    }

    public boolean assign(Role role) {
        return assign(roleCodes.size(), role);
    }

    public boolean assign(long roleCode) {
        final Role role = getPermissionSystem().getRole(roleCode).orElseThrow(NoSuchElementException::new);
        return assign(role);
    }

    public boolean assign(int position, long roleCode) {
        final Role role = getPermissionSystem().getRole(roleCode).orElseThrow(NoSuchElementException::new);
        return assign(position, role);
    }

    public boolean dismiss(Role role) {
        ConditionUtil.notNull(role, "role");

        final long roleCode = role.getRoleCode();
        return roleCodes.remove(roleCode);
    }

    public boolean dismiss(long roleCode) {
        final Role role = getPermissionSystem().getRole(roleCode).orElseThrow(NoSuchElementException::new);
        return dismiss(role);
    }

    public boolean dismiss(int position) {
        return Objects.nonNull(permissions.remove(position));
    }

    @Override
    public void flush() {
        final List<Long> newRoleCodes = roleCodes.stream()
                .map(getPlugin().getPermissionSystem()::getRole)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparingInt(x -> -x.getPriority()))
                .map(Role::getRoleCode)
                .collect(Collectors.toList());

        roleCodes.clear();
        roleCodes.addAll(newRoleCodes);
    }

    public Optional<Long> getRoleCode(int position) {
        return CollectionUtil.get(roleCodes, position).toOptional();
    }

    public Optional<Role> getRole(int position) {
        return getRoleCode(position).flatMap(getPermissionSystem()::getRole);
    }

    public Accessible accessible(Permission required, Function<Role, Accessible> calculator) {
        ConditionUtil.checkArgument(Objects.nonNull(required), "permission is null!");

        for (Permission permission : permissions) {
            final Accessible accessible = permission.acceptable(required);
            if (accessible != Accessible.UNKNOWN) {
                return accessible;
            }
        }

        if (Objects.nonNull(calculator)) {
            for (Role role : getRoles()) {
                final Accessible accessible = calculator.apply(role);
                if (accessible != Accessible.UNKNOWN) {
                    return accessible;
                }
            }
        }

        return Accessible.UNKNOWN;
    }

    public Accessible accessible(Permission required) {
        return accessible(required, null);
    }

    public List<Role> getRoles() {
        return roleCodes.stream()
                .map(getPermissionSystem()::getRole)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<Long> getRoleCodes() {
        return Collections.unmodifiableList(roleCodes);
    }

    public List<Permission> getPermissions() {
        return Collections.unmodifiableList(permissions);
    }

    public Optional<Integer> indexOfPermission(Permission permission) {
        return CollectionUtil.indexOf(permissions, permission);
    }

    public Optional<Integer> indexOfPermission(String permission) {
        return indexOfPermission(getPermissionSystem().createPermission(permission, getPlugin()));
    }

    public Optional<Integer> indexOfRole(long roleCode) {
        return CollectionUtil.indexOf(roleCodes, roleCode);
    }

    public Optional<Integer> indexOfRole(Role role) {
        return CollectionUtil.indexOf(roleCodes, role.getRoleCode());
    }

    public Optional<Permission> getPermission(int position) {
        return CollectionUtil.get(permissions, position).toOptional();
    }

    public boolean isEmpty() {
        return roleCodes.isEmpty() && permissions.isEmpty();
    }
}