package cn.chuanwise.xiaoming.permission.record;

import cn.chuanwise.util.CollectionUtil;
import cn.chuanwise.xiaoming.permission.PermissionPlugin;
import cn.chuanwise.xiaoming.permission.permission.Role;
import cn.chuanwise.xiaoming.preservable.SimplePreservable;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Data
public class PermissionHistory
        extends SimplePreservable<PermissionPlugin> {
    List<AbstractRecord> records = new ArrayList<>();

    public void addRecord(AbstractRecord record) {
        records.add(record);
    }

    public List<AbstractRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }

    @Deprecated
    protected void setRecords(List<AbstractRecord> records) {
        this.records = records;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractRecord> Optional<T> findFirst(Class<T> recordClass, Predicate<T> filter) {
        return (Optional<T>) CollectionUtil.findFirst(records, record -> {
            if (!recordClass.isInstance(record)) {
                return false;
            }
            return filter.test((T) record);
        }).toOptional();
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractRecord> Optional<T> findLast(Class<T> recordClass, Predicate<T> filter) {
        return (Optional<T>) CollectionUtil.findLast(records, record -> {
            if (!recordClass.isInstance(record)) {
                return false;
            }
            return filter.test((T) record);
        }).toOptional();
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractRecord> List<T> searchRecords(Class<T> recordClass, Predicate<T> filter) {
        return (List<T>) records.stream()
                .filter(record -> recordClass.isInstance(record) && filter.test((T) record))
                .collect(Collectors.toList());
    }

    public Optional<Role.CreateRecord> createRecordOfRole(long roleCode) {
        return findFirst(Role.CreateRecord.class, record -> record.getRoleCode() == roleCode);
    }
}
