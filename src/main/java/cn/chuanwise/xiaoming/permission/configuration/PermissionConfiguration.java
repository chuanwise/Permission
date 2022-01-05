package cn.chuanwise.xiaoming.permission.configuration;

import cn.chuanwise.xiaoming.permission.PermissionPlugin;
import cn.chuanwise.xiaoming.permission.permission.Authorizer;
import cn.chuanwise.xiaoming.permission.permission.Role;
import cn.chuanwise.xiaoming.preservable.SimplePreservable;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class PermissionConfiguration
        extends SimplePreservable<PermissionPlugin> {
    Map<Long, Authorizer> authorizers = new HashMap<>();
    Map<Long, Role> roles = new HashMap<>();

    public Optional<Authorizer> getAccount(long authorizerCodes) {
        return Optional.ofNullable(authorizers.get(authorizerCodes));
    }

    public Optional<Role> getRole(long roleCode) {
        return Optional.ofNullable(roles.get(roleCode));
    }
}