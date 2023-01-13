package policy;

import java.net.SocketPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.util.PropertyPermission;

public class CustomPolicy extends Policy {
    private static PermissionCollection perms;

    public CustomPolicy() {
        super();
        if (perms == null) {
            perms = new PermissionCollectionImpl();
            addPermissions();
        }
    }

    @Override
    public PermissionCollection getPermissions(CodeSource codesource) {
        return perms;
    }

    private void addPermissions() {
        SocketPermission socketPermission = new SocketPermission("localhost:1099", "connect, resolve");
        PropertyPermission propertyPermission = new PropertyPermission("*", "read, write");

        perms.add(socketPermission);
        perms.add(propertyPermission);
    }
}
