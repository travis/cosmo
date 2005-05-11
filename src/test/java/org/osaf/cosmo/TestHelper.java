package org.osaf.cosmo;

import org.osaf.cosmo.model.Role;
import org.osaf.cosmo.model.User;

/**
 */
public class TestHelper {
    static int rseq = 0;
    static int useq = 0;

    private TestHelper() {
    }

    /**
     */
    public static Role makeDummyRole() {
        String serial = new Integer(++rseq).toString();

        Role role = new Role();
        role.setName("dummy" + serial);

        return role;
    }

    /**
     */
    public static User makeDummyUser() {
        String serial = new Integer(++useq).toString();

        User user = new User();
        user.setUsername("dummy" + serial);
        user.setEmail(user.getUsername() + "@osafoundation.org");
        user.setPassword(user.getUsername());

        return user;
    }
}
