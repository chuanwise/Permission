package cn.chuanwise.xiaoming.permission;

import cn.chuanwise.xiaoming.permission.permission.PermissionScope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ScopeTest {
    private final Permission PERMISSION = Permission.compile("permission.admin.grant");

    @Test
    void testUnknownAccessible() {
        final PermissionScope scope = new PermissionScope();
        Assertions.assertEquals(Accessible.UNKNOWN, scope.accessible(PERMISSION));
    }

    @Test
    void testGrantPermission() {
        final PermissionScope scope = new PermissionScope();
        scope.grant(PERMISSION);
        Assertions.assertEquals(Accessible.ACCESSIBLE, scope.accessible(PERMISSION));
    }

    @Test
    void testRevokePermission() {
        final PermissionScope scope = new PermissionScope();
        scope.revoke(PERMISSION);
        Assertions.assertEquals(Accessible.UNACCESSIBLE, scope.accessible(PERMISSION));
    }
}
