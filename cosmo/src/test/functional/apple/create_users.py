#!/usr/bin/env python
import cosmoclient
import sys, os, pdb

if __name__ == "__main__":
    url = sys.argv[1]
    client = cosmoclient.CosmoClient(url)
    client.set_basic_auth('root', 'cosmo')
    
    for i in [str(x) for x in range(4) if x is not 0]:
        client.add_user('appleuser'+i, 'appletest', 'Apple', 'User', 'appleuser%s@osafoundation.org' % i)
        if client.response.status != 201:
            if client.response.status == 204:
                print 'User appleuser%s already exists' % i
            else:
                print 'An error occured creating account'
        else:
            print 'Created user appleuser%s' % i
    
    
    
    
    


