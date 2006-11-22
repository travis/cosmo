package org.osaf.cosmo.service.account;

import org.osaf.cosmo.model.User;

public class AutomaticAccountActivator extends
        AbstractCosmoAccountActivator {

    public String generateActivationToken() {
        return null;
    }

    public void sendActivationMessage(User user) {
        user.activate();
    }

    public void destroy() {
        // TODO Auto-generated method stub

    }

    public void init() {
        // TODO Auto-generated method stub

    }

}
