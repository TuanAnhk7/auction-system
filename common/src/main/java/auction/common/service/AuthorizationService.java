package auction.common.service;

import auction.common.model.user.Admin;
import auction.common.model.user.Role;
import auction.common.model.user.User;

import java.util.List;

public class AuthorizationService {
    public boolean hasRole(User user, Role requiredRole) {
        return user != null && user.getRole() == requiredRole;
    }

    public boolean hasPrivilege(Admin admin, String privilege) {
        return admin != null && admin.getPrivileges().contains(privilege);
    }
}