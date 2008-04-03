/*
 * Copyright 2007 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

dojo.provide("cosmotest.data.test_UserStore");

dojo.require("cosmo.testutils");
dojo.require("cosmo.util.auth");
dojo.require("cosmo.data.UserStore");
dojo.require("dojox.uuid.generateRandomUuid");

USERNAME_ROOT = "root";
PASSWORD_ROOT = "cosmo";

doh.registerGroup(
    "cosmotest.data.test_UserStore.js",
    [
        // test UserStore
        {
            name: "test",
            timeout: 10000, 

            setUp: function(){
                cosmo.util.auth.setCred(USERNAME_ROOT, PASSWORD_ROOT);
            },

            runTest: function(){
                this.store = new cosmo.data.UserStore();

                var d = this._createUser(this.store);
                d.addCallback(dojo.hitch(this, this._getUser));
                d.addCallback(dojo.hitch(this, this._modifyUser));
                d.addCallback(dojo.hitch(this, this._deleteUser));
//                var d = store.loadItem({item: {username: "travis"}});
//                d.addCallback(function(user){var x = user; 
//                                             debugger});
                
//                d.addCallback(dojo.hitch(this, this._deleteUsers));
//                d.addCallback(this._assertCount(cosmotest.test_cmp._initUserCount));
                return cosmo.testutils.defcon(d);
            },
            
            _assertCount: function(n){

            },

            _getUsers: function(){
                var d = cosmo.cmp.getUsers();
                d.addCallback(function(userList){
                    doh.assertEqual(
                        cosmotest.test_cmp._initUserCount,
                        userList.length);
                    return userList;
                });
                return d;
            },

            _createUser: function(store){
                var u = dojox.uuid.generateRandomUuid().slice(0, 8);
                store.newItem(
                    {username: u,
                     email: u + "@example.com",
                     password: u,
                     firstName: u,
                     lastName: u
                    }
                );
                var d = store.save();
                d.addCallback(function(result){
                    console.log("TODO: test save result");
                    return result;
                });
//                d.addCallback(this._assertCount(cosmotest.test_cmp._initUserCount + 1));

                // Return username of created user for test chaining.
                d.addCallback(function(result){
                    return u;
                });
                return d;
            },

            _getUser: function(username){
                var d = this.store.loadItem({item: {username: username}});
                d.addCallback(function(user){
                    return user;
                });

                d.addCallback(function(user){
                    doh.assertEqual(username, user.username);
                    doh.assertEqual(username + "@example.com", user.email);
                    doh.assertEqual(username, user.firstName);
                    doh.assertEqual(username, user.lastName);
                    return user;
                });
                return d;
            },

            _modifyUser: function(user){
                var resultUser;
                this.store.setValue(user, "username", "mod" + user.username)
                this.store.setValue(user, "email", "mod" + user.email);
                this.store.setValue(user, "password", "mod" + user.username);
                this.store.setValue(user, "firstName", "mod" + user.firstName);
                this.store.setValue(user, "lastName", "mod" + user.lastName);

                var d = this.store.save();
                d.addCallback(function(results){return user});
/*                d.addCallback(function(newUser){
                    doh.assertEqual("mod" + user.username, newUser.username);
                    doh.assertEqual("mod" + user.email, newUser.email);
                    doh.assertEqual("mod" + user.firstName, newUser.firstName);
                    doh.assertEqual("mod" + user.lastName, newUser.lastName);
                    resultUser = newUser;
                    return newUser;
                });*/

                // Double check username is actually changed
/*                d.addCallback(function(){return cosmo.cmp.getUser(user.username)});
                d.addCallbacks(
                    function(result){
                        doh.assertTrue(false);
                    },
                    function(result){
                        // Return "new" (that is, modified) user to get back into the callback chain
                        return resultUser;
                    }
                );*/
                return d;
            },

            _deleteUser: function(user){
//                this.store.loadItem({item: user});
                this.store.deleteItem(user);
                var d = this.store.save();
                // Make sure user is gone
//                d.addCallback(function(){return cosmo.cmp.getUser(user.username)});
/*                d.addCallbacks(
                    function(result){
                        doh.assertTrue(false);
                    },
                    function(result){
                        return true;
                    }
                );

                d.addCallback(this._assertCount(cosmotest.test_cmp._initUserCount));*/

                return d;
            },

            _deleteUsers: function(){
                var usernames = [];
                var deferreds = [];
                for (var i = 0; i < 3; i++){
                    var u = dojox.uuid.generateRandomUuid().slice(0, 8);
                    deferreds.push(cosmo.cmp.createUser(
                        {username: u,
                         email: u + "@example.com",
                         password: u,
                         firstName: u,
                         lastName: u
                        }
                    ));
                    usernames.push(u);
                }
                var dl = new dojo.DeferredList(deferreds);

                // Make sure they were created
                dl.addCallback(function(){
                    var checkdefs = [];
                    for (var i in usernames){
                        var d = cosmo.cmp.getUser(usernames[i]);
                        d.addCallback(function(user){
                            doh.assertTrue(!!user);
                        });
                        checkdefs.push(d);
                    }
                    return new dojo.DeferredList(checkdefs);
                });
                dl.addCallback(this._assertCount(cosmotest.test_cmp._initUserCount + 3));


                dl.addCallback(function(){
                    return cosmo.cmp.deleteUsers(usernames);
                });
                // Make sure they were deleted
                dl.addCallback(function(){
                    var checkdefs = [];
                    for (var i in usernames){
                        var d = cosmo.cmp.getUser(usernames[i]);
                        d.addCallback(function(user){
                            doh.assertTrue(false);
                        });
                        checkdefs.push(d);
                    }
                    return new dojo.DeferredList(checkdefs);
                });
                dl.addCallback(function(resultsList){
                    // Since none of the users exist, all results should be errors
                    for (var i in resultsList){
                        jum.assertFalse(!!resultsList[i][0]);
                        jum.assertTrue(!!resultsList[i][1]);
                    }
                    return true;
                });
                dl.addCallback(this._assertCount(cosmotest.test_cmp._initUserCount));
                return dl;
            },

            tearDown: function(){
                cosmo.util.auth.clearAuth();
            }
        }/*,
        {
            name: "authenticated",
            timeout: 10000, 

            setUp: function(){
            },

            runTest: function(){
                var d = this._signup();
                d.addCallback(dojo.hitch(this, this._getAccount));
                d.addCallback(dojo.hitch(this, this._modifyAccount));
                
                return cosmo.testutils.defcon(d);
            },
            
            _signup: function(){
                var u = dojox.uuid.generateRandomUuid().slice(0, 8);
                var d = cosmo.cmp.signup({
                    username: u,
                    password: u,
                    email: u + "@gmail.com",
                    firstName: u,
                    lastName: u
                });
                d.addCallback(function(user){
                    cosmo.util.auth.setCred(user.username, u);
                    return user;
                });
                return d;
            },

            _getAccount: function(user){
                var d = cosmo.cmp.getAccount();
                d.addCallback(function(newUser){
                    doh.assertEqual(user.username, newUser.username);
                    doh.assertEqual(user.email, newUser.email);
                    doh.assertEqual(user.firstName, newUser.firstName);
                    doh.assertEqual(user.lastName, newUser.lastName);
                    return newUser;
                });
                return d;
            },
            
            _modifyAccount: function(user){
                var modUser = {
                    username: user.username,
                    email: "mod" + user.email,
                    firstName: "mod" + user.firstName,
                    lastName: "mod" + user.lastName,
                    password: "newpassword"
                };
                var d = cosmo.cmp.modifyAccount(modUser);
                d.addCallback(function(){
                    return cosmo.cmp.getAccount();
                });
                d.addCallback(function(resultUser){
                    doh.assertEqual(modUser.username, resultUser.username);
                    doh.assertEqual(modUser.email, resultUser.email);
                    doh.assertEqual(modUser.firstName, resultUser.firstName);
                    doh.assertEqual(modUser.lastName, resultUser.lastName);
                    return resultUser;
                });
                return d;
            },

            tearDown: function(){
                cosmo.util.auth.clearAuth();
            }
        }*/
    ],
    function(){ //setUp
        cosmo.util.auth.setCred(USERNAME_ROOT, PASSWORD_ROOT);
    },
    function(){ //tearDown
    }
);
