package p2p.forum;

import java.net.InetAddress;

/**
 * holds the details of the user
 * used to identify the user and access them over network
 */
public class User {
    String name;
    InetAddress inetAddress;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return inetAddress.equals(user.inetAddress);

    }

    @Override
    public int hashCode() {
        return inetAddress.hashCode();
    }
}
