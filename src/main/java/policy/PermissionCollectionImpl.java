package policy;

import java.security.Permission;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

public class PermissionCollectionImpl extends PermissionCollection {

    ArrayList<Permission> permissions = new ArrayList<Permission>();
    @Override
    public void add(Permission permission) {
        permissions.add(permission);
    }

    @Override
    public boolean implies(Permission permission) {
        return permissions.stream()
                .anyMatch(p -> p.implies(permission));
    }

    @Override
    public Enumeration<Permission> elements() {
        return Collections.enumeration(permissions);
    }

    public boolean isReadOnly() {
        return false;
    }
}
