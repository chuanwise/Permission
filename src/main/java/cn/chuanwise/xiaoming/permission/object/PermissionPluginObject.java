package cn.chuanwise.xiaoming.permission.object;

import cn.chuanwise.xiaoming.bot.XiaomingBot;
import cn.chuanwise.xiaoming.permission.PermissionPlugin;
import cn.chuanwise.xiaoming.permission.PermissionSystem;

public interface PermissionPluginObject {
    default PermissionPlugin getPlugin() {
        return PermissionPlugin.INSTANCE;
    }

    default XiaomingBot getXiaomingBot() {
        return getPlugin().getXiaomingBot();
    }

    default PermissionSystem getPermissionSystem() {
        return getPlugin().getPermissionSystem();
    }
}
