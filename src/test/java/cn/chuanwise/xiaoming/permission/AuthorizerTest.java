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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthorizerTest {
    private final Permission PERMISSION = Permission.compile("permission.admin.grant");
    private final int operatorCode = 1437100907;
    private final String groupTag = "groupTag";
    private PermissionSystem permissionSystem;

    @BeforeAll
    void init() {
        final PermissionPlugin plugin = PermissionPlugin.INSTANCE;
        plugin.configuration = new PluginConfiguration();
        plugin.history = new PermissionHistory();
        permissionSystem = new PermissionSystem(new PermissionConfiguration());
        plugin.permissionSystem = permissionSystem;
    }

    @Test
    void testNoPermission() {
        final Authorizer authorizer = new Authorizer();
        Assertions.assertEquals(Accessible.UNKNOWN, authorizer.accessibleGlobal(PERMISSION));
    }

    @Test
    void testGlobalGrantPermission() {
        final Authorizer authorizer = new Authorizer();
        authorizer.grantGlobalPermission(operatorCode, PERMISSION);
        Assertions.assertEquals(Accessible.ACCESSIBLE, authorizer.accessibleGlobal(PERMISSION));
    }

    @Test
    void testGlobalRevokePermission() {
        final Authorizer authorizer = new Authorizer();
        authorizer.grantGlobalPermission(operatorCode, PERMISSION);
        authorizer.revokeGlobalPermission(operatorCode, PERMISSION);
        Assertions.assertEquals(Accessible.UNKNOWN, authorizer.accessibleGlobal(PERMISSION));
    }

    @Test
    void testGlobalRoleGlobalPermission() {
        final Authorizer authorizer = new Authorizer();

        final Role role = new Role();
        permissionSystem.addRole(operatorCode, role);
        role.grantGlobalPermission(operatorCode, PERMISSION);

        authorizer.assignGlobalRole(operatorCode, role);

        Assertions.assertEquals(Accessible.ACCESSIBLE, authorizer.accessibleGlobal(PERMISSION));
    }

    @Test
    void testGlobalRoleGroupPermission() {
        final Authorizer authorizer = new Authorizer();

        final Role role = new Role();
        permissionSystem.addRole(operatorCode, role);
        role.grantGlobalPermission(operatorCode, PERMISSION);

        authorizer.assignGlobalRole(operatorCode, role);

        Assertions.assertEquals(Accessible.ACCESSIBLE, authorizer.accessibleGroup(groupTag, PERMISSION));
    }

    @Test
    void testGroupRoleGroupPermission() {
        final Authorizer authorizer = new Authorizer();

        final Role role = new Role();
        permissionSystem.addRole(operatorCode, role);
        role.grantGroupPermission(operatorCode, groupTag, PERMISSION);

        authorizer.assignGlobalRole(operatorCode, role);

        Assertions.assertEquals(Accessible.UNKNOWN, authorizer.accessibleGlobal(PERMISSION));
        Assertions.assertEquals(Accessible.ACCESSIBLE, authorizer.accessibleGroup(groupTag, PERMISSION));
    }
}
