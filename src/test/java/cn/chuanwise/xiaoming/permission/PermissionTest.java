package cn.chuanwise.xiaoming.permission;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class PermissionTest {
    @Test
    public void plainTextTest() {
        final String plainText = "taixue.?.??.teacher";
        final Permission permission = Permission.compile(plainText);
        Arrays.asList(
//                "taixue.nanshufang.teacher",
//                "taixue.yingzaosi.teacher",
//                "taixue.*.teacher",
//                "taixue.**",
                "taixue.?.sadsad.asdsad.teacher",
                "-taixue.?.sadsad.asdsad.teacher",
//                "taixue.yingzaosi.teacher",
//                "taixue.*.teacher",
//                "taixue.**",
                "tail"
        )
                .stream()
                .map(Permission::compile)
                .forEach(ownedPermission -> {
                    System.out.println(ownedPermission.toString() + " >> " + ownedPermission.acceptable(permission));
        });
    }

    @Test
    void testRegexCheck() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Permission.compile("xx.[?]"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Permission.compile("xx.[?\\]"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Permission.compile("xx.qwq.[?\\]"));
    }

    @Test
    void testEmptyCheck() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Permission.compile("xx.."));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Permission.compile("xx.qwq..qwqw"));
    }
}
