package cn.chuanwise.xiaoming.permission;

import cn.chuanwise.xiaoming.permission.configuration.PermissionConfiguration;
import cn.chuanwise.xiaoming.permission.configuration.PluginConfiguration;
import cn.chuanwise.xiaoming.permission.permission.Role;
import cn.chuanwise.xiaoming.permission.record.PermissionHistory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RoleTest {
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
        final Role role = new Role();
        Assertions.assertEquals(Accessible.UNKNOWN, role.accessibleGlobal(PERMISSION));
    }

    @Test
    void testGrantPermission() {
        final Role role = new Role();
        role.grantGlobalPermission(operatorCode, PERMISSION);
        Assertions.assertEquals(Accessible.ACCESSIBLE, role.accessibleGlobal(PERMISSION));
    }

    @Test
    void testRevokePermission() {
        final Role role = new Role();
        role.revokeGlobalPermission(operatorCode, PERMISSION);
        Assertions.assertEquals(Accessible.UNACCESSIBLE, role.accessibleGlobal(PERMISSION));
    }

    @Test
    void testSubRolePermission() {
        final Role subRole = new Role();
        permissionSystem.addRole(operatorCode, subRole);
        subRole.grantGlobalPermission(operatorCode, PERMISSION);

        final Role parentRole = new Role();
        permissionSystem.addRole(operatorCode, parentRole);

        Assertions.assertTrue(subRole.assignGlobalParentRole(operatorCode, parentRole));

        Assertions.assertEquals(Accessible.ACCESSIBLE, subRole.accessibleGlobal(PERMISSION));
        Assertions.assertEquals(Accessible.UNKNOWN, parentRole.accessibleGlobal(PERMISSION));
    }

    @Test
    void testGlobalInheritance() {
        final Role subRole = new Role();
        permissionSystem.addRole(operatorCode, subRole);

        final Role parentRole = new Role();
        permissionSystem.addRole(operatorCode, parentRole);

        Assertions.assertTrue(subRole.assignGlobalParentRole(operatorCode, parentRole));

        Assertions.assertTrue(subRole.isGlobalChildOf(parentRole));
        Assertions.assertTrue(parentRole.isGlobalParentOf(subRole));
        Assertions.assertTrue(subRole.isGroupChildOf(groupTag, parentRole));
        Assertions.assertTrue(parentRole.isGroupParentOf(groupTag, subRole));
    }

    @Test
    void testGroupInheritance() {
        final Role subRole = new Role();
        permissionSystem.addRole(operatorCode, subRole);

        final Role parentRole = new Role();
        permissionSystem.addRole(operatorCode, parentRole);

        Assertions.assertTrue(subRole.assignGroupParentRole(operatorCode, groupTag, parentRole));

        Assertions.assertFalse(subRole.isGlobalChildOf(parentRole));
        Assertions.assertFalse(parentRole.isGlobalParentOf(subRole));
        Assertions.assertTrue(subRole.isGroupChildOf(groupTag, parentRole));
        Assertions.assertTrue(parentRole.isGroupParentOf(groupTag, subRole));
    }

    @Test
    void testSubRoleGroupPermission() {
        final Role subRole = new Role();
        permissionSystem.addRole(operatorCode, subRole);

        final Role parentRole = new Role();
        permissionSystem.addRole(operatorCode, parentRole);

        Assertions.assertTrue(subRole.assignGroupParentRole(operatorCode, groupTag, parentRole));
        Assertions.assertTrue(subRole.grantGroupPermission(operatorCode, groupTag, PERMISSION));

        Assertions.assertEquals(Accessible.ACCESSIBLE, subRole.accessibleGroup(groupTag, PERMISSION));
        Assertions.assertEquals(Accessible.UNKNOWN, parentRole.accessibleGroup(groupTag, PERMISSION));
    }

    @Test
    void testParentRoleGroupPermission() {
        final Role subRole = new Role();
        permissionSystem.addRole(operatorCode, subRole);

        final Role parentRole = new Role();
        permissionSystem.addRole(operatorCode, parentRole);

        Assertions.assertTrue(subRole.assignGroupParentRole(operatorCode, groupTag, parentRole));
        Assertions.assertTrue(parentRole.grantGroupPermission(operatorCode, groupTag, PERMISSION));

        Assertions.assertEquals(Accessible.ACCESSIBLE, subRole.accessibleGroup(groupTag, PERMISSION));
        Assertions.assertEquals(Accessible.ACCESSIBLE, parentRole.accessibleGroup(groupTag, PERMISSION));
    }

    @Test
    void testParentRoleGlobalPermission() {
        final Role subRole = new Role();
        permissionSystem.addRole(operatorCode, subRole);

        final Role parentRole = new Role();
        permissionSystem.addRole(operatorCode, parentRole);

        Assertions.assertTrue(subRole.assignGroupParentRole(operatorCode, groupTag, parentRole));
        Assertions.assertTrue(parentRole.grantGlobalPermission(operatorCode, PERMISSION));

        Assertions.assertEquals(Accessible.ACCESSIBLE, subRole.accessibleGroup(groupTag, PERMISSION));
        Assertions.assertEquals(Accessible.ACCESSIBLE, parentRole.accessibleGroup(groupTag, PERMISSION));
    }

    @Test
    void testSubRoleGlobalPermission() {
        final Role subRole = new Role();
        permissionSystem.addRole(operatorCode, subRole);

        final Role parentRole = new Role();
        permissionSystem.addRole(operatorCode, parentRole);

        Assertions.assertTrue(subRole.assignGroupParentRole(operatorCode, groupTag, parentRole));
        Assertions.assertTrue(subRole.grantGlobalPermission(operatorCode, PERMISSION));

        Assertions.assertEquals(Accessible.ACCESSIBLE, subRole.accessibleGroup(groupTag, PERMISSION));
        Assertions.assertEquals(Accessible.UNKNOWN, parentRole.accessibleGroup(groupTag, PERMISSION));
    }
}
