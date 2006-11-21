package org.osaf.cosmo.service.impl;

import org.osaf.cosmo.model.User;

public class AutomaticAccountActivationService extends
        AbstractCosmoAccountActivationService {

    public String getActivationToken() {
        return null;
    }

    public void initiateActivationProcess(User user) {
        user.activate();
    }

    public void destroy() {
        // TODO Auto-generated method stub

    }

    public void init() {
        // TODO Auto-generated method stub

    }

}
