package cn.chuanwise.xiaoming.permission;

import cn.chuanwise.xiaoming.permission.configuration.PermissionConfiguration;
import cn.chuanwise.xiaoming.permission.configuration.PluginConfiguration;
import cn.chuanwise.xiaoming.permission.permission.Authorizer;
import cn.chuanwise.xiaoming.permission.permission.Role;
import cn.chuanwise.xiaoming.permission.record.PermissionHistory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DefaultRoleTest {
    private final Permission PERMISSION = Permission.compile("permission.admin.grant");
    private final int operatorCode = 1437100907;
    private final String groupTag = "groupTag";
    private final PermissionPlugin plugin = PermissionPlugin.INSTANCE;
    private PermissionSystem permissionSystem;

    @BeforeAll
    void init() {
        plugin.configuration = new PluginConfiguration();
        plugin.history = new PermissionHistory();
        permissionSystem = new PermissionSystem(new PermissionConfiguration());
        plugin.permissionSystem = permissionSystem;
    }
}
