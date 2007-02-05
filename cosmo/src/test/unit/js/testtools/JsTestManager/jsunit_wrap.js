/**
@file jsunit_wrap.js

A wrapper for JsUnit.

This file provides a wrapper for two extant JsUnit implementations:

  - Edward Hieatt JsUnit, http://jsunit.net , MPL/GPL/LGPL <br>
    (we have tested and developed against 1.3.3)
  - Jorg Schaible JsUnit, http://jsunit.berlios.de , GPL <br>
    (we have tested and developed against 1.2b1, which differs a lot from CVS)

This allows tests to be written which will run unchanged against either
of the JsUnit implementations. There is a partial effort by the authors
of those two projects (they are by no means hostile to each other), but
that effort has not yet come to fruition, and in any event there are
enough other issues with both of them to merit some separation, at least
for now.

This file also provides its own built-in implementation, if neither JsUnit
is available. It works in either a shell or a browser.

The wrapper determines which JsUnit implementation is in use through object detection.

It defines a javascript global called <var>jum</var> which represents the
test environment. Methods on this object are used for assertions and any IO.

The sequence of script file loading should be as follows:
- your chosen jsunit library
- your library to test
- any dependencies jsunit_wrap.js itself needs or benefits from (see below)
- jsunit_wrap.js (this file)
- your test definitions
- a call to jum.init()
- a call to jum.runAll() (If not using a gui test runner, as with Hieatt JsUnit)

In the case of Hieatt JsUnit, the above scripts would be loaded via 'script' elements
in an HTML page. In the case of Schaible JsUnit, the above scripts would be loaded
on a javascript interpreter command line (such as via a "-f" option), or programmatically 
(for example by using a global load() function supplied by the environment).

We attempt to be well-behaved with respect to the underlying JsUnit implementation
which is in use, so it should be possible to mix and match tests written to this
API with ones using the implementation-specific API.

Some of the features of this wrapper are:
- normalizes the semantics of assertTrue and assertFalse
- normalizes the semantics of assertEquals and offers deep equality
- improves automatic test finding
- offers async test completion (for callback testing)

<h3>Determining what tests get run</h3>

For Hieatt JsUnit, if <var>exposeTestFunctionNames</var> is defined, we do nothing
and rely on that. Otherwise we supply an implementation for that function.
Either way, <var>exposeTestFunctionNames</var> is defined, so that Hieatt JsUnit's own
attempts at reflection are never utilitized.

For Schaible JsUnit, if <var>AllTests.suite</var> is defined, we do nothing
and rely on that. Otherwise we supply a definition.

If the <var>jum.TEST_FUNCTION_NAMES</var> is set, it is assumed to be a set of
global function names of tests. If it is set, we do no reflection.

<h3>Test Declaration and Discovery</h3>

JUnit has a confusing set of terminology which JsUnit implementations retain
(and are not to blame for). In JUnit
- a "TestCase" is a Java class which inherits from the JUnit base class. It has
 one or more test methods (each with a method name starting with "test"), and optionally
 the special methods setUp() and tearDown().
- a "TestSuite" instance is a collection of TestCase instances. The containment can
  be hierarchical. Typically the implementation's TestSuite concrete class would be used
 directly. 

Schaible JsUnit mimics JUnit fairly closely, requiring the programming writing tests
to subclass TestCase (using ECMAScript prototypes). There is also a TestSuite "class".
In Schaible JsUnit, the TestSuite class does not extend TestCase.
(There is some unnecessary confusion in the implementation concerning new() and
constructors, but the idea is preserved.)

Schaible JsUnit does no reflection at the global level. It does do reflection-based
discovery of test methods given a TestCase instance.
 
Hieatt JsUnit does not retain this terminology. It simply assumes there are any
number of functions defined at the global level in some html page. A test "suite"
is a collection of html pages.

Hieatt JsUnit attempts to use reflection to discover all test functions defined 
in an html page.

In our implementation, we introduce the notion of test "group", which is a collection
of test functions. It is more akin to the JUnit "TestCase" in implmenetation, though
given the name "TestCase", that is more often thought of as a single test.

We also have a single-level containment hierarchy, a collection of all test groups.

For us, a test "name" is systematically related to a test function name (in both directions).

Thus we draw a distinction between:
- a test group name
- a test function name
- a test name

In Schaible JsUnit, we create a TestCase for each group. We then create a TestSuite which
contains just that single TestCase.

<h3>Debug Output</h3>

There is a method <code>jum.debug</code> for debug output to be called from your tests.

You may or may not want any debug output from your library under test to be
handled the same way. If so, it is up to you to override your own debug output
functionality in your library under test, to call <code>jum.debug</code> when <var>jum</var> is defined.

<h3>Finding and Running Tests</h3>

<h3>Dependencies</h3>

If you want to be able to do deep comparison in <var>assertEquals</var>, an
uneval implementation must be available.
That means you need either <var>burst.Lang.uneval</var>, or you must be on a
Mozilla javascript (which has Object.toSource()).

<h3>TODO</h3>
@todo deal with asserts done in async callbacks (See http://groups.yahoo.com/group/jsunit/message/144)

<h3>Issues</h3>

Hieatt assertTrue and assertFalse require that their argument is typeof boolean.
Schaible assertTrue and assertFalse do an eval.

Neither implementation does deep equality in their assertEquals. That means it is
difficult to test results against an Array or simple object like {a: 1, b: 2}.

Both are weak at reflection.

Some issues with both:
- no version variable
- not clean with respect to Mozilla strict warnings

Some issues with Hieatt JsUnit:
- the suite entity is implemented as a set of html pages
- exception suppression
- must be in top window or it fails
- testpage must be an absolute url (or at least an absolute path)

Some issues with Schaible JsUnit:
- requires OO scaffolding for test cases
- unclear interpretation of TestCase and TestSuite 
- bizarre use of the new operator (at least in 1.2beta1)

@author Copyright 2003 Mark D. Anderson (mda@discerning.com)
@author Licensed under the Academic Free License 1.2 http://www.opensource.org/licenses/academic.php
*/


/*
* define a single API for test assertions
*/

function JUM() {
   /*
    * public methods, must be implemented by subclass
    */

  /** display the String as debug output (if enabled by the test environment) */
  //:CLIMETHOD void debug(String line)

  /** display the String as info output (if enabled by the test environment) */
  //:CLIMETHOD void info(String line)

  /** display the String as warn output (if enabled by the test environment) */
  //:CLIMETHOD void warn(String line)

  /** 
   * Assert that eval(obj) tests as true. Overloaded for 1 or 2 args (message is optional).
   * (We wrap Hieatt assertTrue and assertFalse to accomplish this semantic; natively
   * it requires obj to be Boolean.)
   */
  //:CLIMETHOD void assertTrue(String message, Object obj)
  /** Variant for 1 arg. */
  //:CLIMETHOD void assertTrue(Object obj)

  /** Assert that eval(obj) tests as false. Overloaded for 1 or 2 args (message is optional) */
  //:CLIMETHOD void assertFalse(String message, Object obj)
  /** Variant for 1 arg. */
  //:CLIMETHOD void assertFalse(Object obj)

  /**
   * Assert that expected and actual are equal.
   *
   * Out of the box, Hieatt does ===. Schaible uses == except when <var>expected</var> is a Regexp,
   * and <var>actual</var> is a string, in which case it tests actual.match(expected).
   *
   * Neither attempts a "deep" equality test, which makes comparison to Array or Object difficult.
   *
   * If the function <var>burst.Lang.uneval</var> is not defined, we do nothing about all this, and
   * just call the underlying function.
   * If the function <var>burst.Lang.uneval</var> is available, it is applied to <var>expected</var>
   * or <var>actual</var> if they have typeof 'object'. In this way, a basic "deep" comparison
   * is possible.
   */
   //:CLIMETHOD void assertEquals(String message, Object expected, Object actual)
  /** Variant for 2 args. */
   //:CLIMETHOD void assertEquals(Object expected, Object actual)

   /**
    * Run all tests. Only has to implemented in non-gui test managers.
    */ 
   //:CLIMETHOD void runAll()




   /*
    * public methods, implemented in base class
    */

   /** 
    * Report that the specified test is not being tested (this is different from the test
    * being tried and failed).
    * By default, this is implemented as a particular message to jum.warn(), since this is not
    * a concept supported in the JsUnit implementations (or JUnit, for that matter).
    *
    * @param funcname Test function name (possibly excluding 'test' or 'test_').
    */
   //:CLIMETHOD void untested(String funcname)

    // This is not an ideal interface, but there is no way across JsUnit implementations
    // to know what the current test is while executing it.
    // There is also no waiting ability builtin, so a Function object has to be passed in.
   /**
    * Indicate that this test is not over and will be continued asynchronously.
    *
    * @param funcname Test function name (possibly excluding 'test' or 'test_').
    * @param id A unique string within the test function. Can be null if the test will have only one continuation. 
    * @param desc A description of what the expected continuation is doing, for use in any later error message
    */
   //:CLIMETHOD Object pause(String funcname, String id, String desc)

    /**
     * Ask the test manager to resume execution for the named test.
     *
     * @param funcname Test function name (possibly excluding 'test' or 'test_').
     * @param id The value matching what was used in pause()
     * @param func The function to call now as the resumption.
     */
    //:CLIMETHOD void resume(String funcname, String id, Function func)

     /**
     * Wait for the specified (other) test to have its resume done, then call this one.
     * Returns true if other is already done.
     * Throws if other is never heard of (no previous pause call).
     */
     //:CLIMETHOD Boolean waitFor(String other_funcname, String other_id, String funcname, String id, Function func)

   /** Initializes the test runner. Concrete method in the base class. Must be called after all tests scripts are loaded. */
   //:CLIMETHOD void init(Object scopeobj)




   /*
    * protected methods, must be implemented by subclass
    */

   /** Return true if and only if the object is an exception indicating a test failure (vs. a test error). protected abstract. */
   //:CLIMETHOD Boolean isFailureException(Object e)

   /** 
    * Return true if and only if the tests are (somehow) already chosen, 
    * so that the wrapper should not try to determine and set them. protected abstract.
    */
   //:CLIMETHOD Boolean areTestsChosen()

   /**
    * Set all the tests available.
    * protected abstract.
    * @param alltests An associative array whose structure is currently not documented (see the source).  
    */ 
   //:CLIMETHOD void setTests(Object alltests)


   /*
    * protected methods, implemented in base class
    */

   /**
    * Should be called after all (synchronous) tests are run. 
    * It first waits for completion of any outstanding asyncs (pause but no matching resume) for up to jum.DEFAULT_TIMEOUT_MILLIS.
    */
   //:CLIMETHOD void reportAsync()



   /*
    * protected variables, for use in base class methods
    */
  // from normalized func name to an Array of JUMAsync
  this.asyncs_by_funcname_ = {};
  // from normalized func name to an JUMAsync
  this.asyncs_by_key_ = {};

  this.inited_ = false;
}

// enum for test status
JUM.STATUS_UNKNOWN = 'unknown';
JUM.STATUS_FAILED = 'failed';
JUM.STATUS_ERROR = 'error';
JUM.STATUS_UNTESTED = 'untested';
JUM.STATUS_PASSED = 'passed';

// remove any leading 'test_' or 'test'
function jum_normalize_func_name(funcname) {
  if (funcname.substring(0,5) == 'test_') return funcname.substring('test_'.length);
  if (funcname.substring(0,4) == 'test') return funcname.substring('test'.length);
  return funcname;
}

JUM.prototype.getTests = function() {return this.alltests_};


JUM.prototype.untested = function(funcname) {
  funcname = jum_normalize_func_name(funcname);
  if (typeof this.untested_ == 'undefined') this.untested_ = 0;
  this.untested_++;
  jum.warn("skipping test '" + funcname + "'"); 
}

function JUMAsync(name, id, desc) {
  this.name_ = name;
  this.id_ = id;
  this.desc_ = desc;
  this.state_ = 'paused';
  this.status_ = JUM.STATUS_UNKNOWN;
  this.exception_ = null;
}

JUM.prototype.maybeInitAsync = function() {
  if (typeof this.failed_async_count_ == 'undefined') {
    this.failed_async_count_ = 0;
    this.error_async_count_ = 0;
    this.passed_async_count_ = 0;
  }
}

JUM.prototype.pause = function(funcname, id, desc) {
  this.maybeInitAsync();
  funcname = jum_normalize_func_name(funcname);
  var key = jum_to_key(funcname,id);
  if (typeof this.asyncs_by_key_[key] != 'undefined') 
     throw Error("continuation key '" + key + "' already in use");
  var asyncObj = this.asyncs_by_key_[key] = new JUMAsync(funcname, id, desc);
  if (typeof this.asyncs_by_funcname_[funcname] == 'undefined') 
    this.asyncs_by_funcname_[funcname] = [asyncObj];
  else
    this.asyncs_by_funcname_[funcname].push(asyncObj);
  jum.debug("   PAUSING test function with key '" + key + "'...");
  return asyncObj;
}


JUM.prototype.callAsync = function(funcname, func, asyncObj) {
    asyncObj.state_ = 'resuming';
    var suffix = " test function '" + funcname + "'";
    var threw = false;
    try{ func() }
    catch(e) {
      threw = true;
      if (this.isFailureException(e)) {
	 jum.debug("ASYNC FAILED" + suffix + ': ' + jum_except_string(e));
         ++this.failed_async_count_;
         asyncObj.status_ = JUM.STATUS_FAILED;
         bu_alert("got async failure in " + funcname + ": " + jum_except_string(e));
      }
      else {
         jum.debug("ASYNC ERROR" + suffix + ': ' + e.toString());
         ++this.error_async_count_;
         asyncObj.status_ = JUM.STATUS_ERROR;
         bu_alert("got async error in " + funcname + ": " + jum_except_string(e));
      }
      asyncObj.exception_ = e;
    }
    if (!threw) {
      jum.debug("ASYNC PASSED" + suffix);
      ++this.passed_async_count_;
      asyncObj.status_ = JUM.STATUS_PASSED;
    }
    asyncObj.state_ = 'resumed';
    return asyncObj.status_;
}

function jum_to_key(funcname, id) {
   return funcname + (id ? id : '0');
}

JUM.prototype.resume = function(funcname, id, func) {
  if (typeof func != 'function') throw Error("(jsunit_wrap.js) bad function to resume: " + func);
  funcname = jum_normalize_func_name(funcname);
  var key = jum_to_key(funcname,id);
  jum.debug("   RESUMING test function with key '" + key + "' and function: " + func);
  if (typeof this.asyncs_by_key_[key] == 'undefined') throw Error("(jsunit_wrap.js) No matching pause to resume for key '" + key + "'");
  var asyncObj = this.asyncs_by_key_[key];
  if (asyncObj.state_ != 'paused') throw Error("continuation with key '" + key + "' in state " + asyncObj.state_);
  this.callAsync(funcname, func, asyncObj); 
}

//function JUMWaitFailure() {}

function jum_has_settimeout() {
  if (typeof jum_global.setTimeout == 'undefined') return false;
  return true;
}

/*
* if the attempt_handler returns true, the function returns true.
* if the attempt_handler returns false and there is remaining time, it calls setTimeout
* and returns false.
* if the attempt_handler returns false and there is no remaining time, it
* calls timeout_handler and then throws an exception (if timeout_handler does not).
*/
function jum_wait(description, attempt_handler, timeout_handler, total_millis, interval_millis, remaining_millis) {
  // bu_alert('(jsunit_wrap.js) in jum_wait for "' + description + '", remaining_millis=' + remaining_millis + ' arguments.length=' + arguments.length);
  if (typeof total_millis == 'undefined') total_millis = jum.DEFAULT_TIMEOUT_MILLIS;
  if (typeof interval_millis == 'undefined') interval_millis = 1000;
  if (typeof remaining_millis == 'undefined') remaining_millis = total_millis;

  if (attempt_handler()) {
    return true;
  }
  if (typeof interval_millis != 'number' || interval_millis <= 0) 
    throw Error('(jsunit_wrap.js) bad interval_millis: ' + interval_millis);
  remaining_millis = remaining_millis - interval_millis;
  if (remaining_millis < 0) {
    if (timeout_handler) timeout_handler();
    throw Error("(jsunit_wrap.js) Not able to complete " + description + " in " + total_millis + " millis");
  }
  bu_alert('(jsunit_wrap.js) about to wait another ' + interval_millis + ' millis (' + remaining_millis + ' remaining) before: ' + description);
  setTimeout(function() {
     jum_wait(description, attempt_handler, timeout_handler, total_millis, interval_millis, remaining_millis);
  }, interval_millis);
  return false;
}

// returns detailed string comparison
function jum_compare_strings(s1, s2) {
  var maxlen = s1.length > s2.length ? s1.length : s2.length;
  var has1 = [];
  var has2 = [];
  for(var i=0;i<maxlen;++i) {
    if (i >= s1.length) {
       has1.push([i,'',0]);
       has2.push([i,s2[i],s2.charCodeAt(i)]);
    }
    else if (i >= s2.length) {
       has2.push([i,'',0]);
       has1.push([i,s1[i],s1.charCodeAt(i)]);
    }
    else if (s1[i] == s2[i]) {}
    else {
       has1.push([i,s1[i],s1.charCodeAt(i)]);
       has2.push([i,s2[i],s2.charCodeAt(i)]);
    }
  }
  return 's1: "' + s1 + '"\n' +
         's2: "' + s2 + '"\n' +
         'has1: ' + has1.join('|') + '\n' +
         'has2: ' + has2.join('|') + '\n'
     ;
 // return [has1, has2];
}

function jum_except_string(e) {
  // Sigh, hieatt exception does not inherit from Error, and does not implement toString()
  if (typeof JsUnitException != 'undefined' && e instanceof JsUnitException) {
    if (typeof jsUnitTestManager != 'undefined')
       return jsUnitTestManager.prototype._problemDetailMessageFor(e);
    return (e.comment ? e.comment + '\n' : '') + e.jsUnitMessage + (e.stackTrace ? '\nstack:\n' + e.stackTrace : '');
  }
  // Schaible AssertionFailedError inherits from Error, and subclasses implement toString.
  return (typeof e.message != 'undefined' ? '"' + e.message + '"' : 
           (typeof e.description != 'undefined' ? '"' + e.description + '"' :
             ('typeof=' + (typeof e) + ' toString=' + e) +
              (typeof e.stack != 'undefined' ? ' stack: ' + e.stack : '')));
}

function jum_async_except_string(key, asyncObj) {
  return '   Test "' + key + '": ' + jum_except_string(asyncObj.exception_) + "\n";
}

JUM.prototype.getActiveAsync = function() {
  var active = [];
  for(var key in this.asyncs_by_key_) {
    var asyncObj = this.asyncs_by_key_[key];
    if (asyncObj.state_ != 'resumed') active.push(key);
  }
  return active;
}

JUM.prototype.isAsyncDone = function(key) {
  var asyncObj = this.asyncs_by_key_[key];
  return asyncObj.state_ == 'resumed';
}

JUM.prototype.areAsyncDone = function() {
  // if no setTimeout, then we couldn't have any async stuff to wait for
  if (!jum_has_settimeout()) return true;

  var active = this.getActiveAsync();
  if (active.length == 0) {
    jum.debug("no asyncs remaining to wait for"); 
    return true;
  }
  return false;
}

JUM.prototype.waitFor = function(other_funcname, other_id, funcname, id, funcobj, desc, timeout_millis) {
  var other_key = jum_to_key(other_funcname, other_id);
  var other_asyncObj = this.asyncs_by_key_[other_key];
  if (typeof other_asyncObj == 'undefined') throw Error('(jsunit_wrap.js) no such previous async object with key ' + other_key);

  if (!jum_has_settimeout()) {
    jum.warn("waitFor called when setTimeout is not available, so just calling function");
    funcobj();
    return true;
  }
  if (other_asyncObj.state_ == 'resumed') {
     jum.debug("other async '" + other_key + "' already done, so running this one now");
     funcobj();
  }
  if (typeof desc == 'undefined') desc = 'resume after ' + other_funcname + ' completes';
  var asyncObj = jum.pause(funcname, id, desc);
  if (typeof timeout_millis == 'undefined') timeout_millis = jum.DEFAULT_TIMEOUT_MILLIS;
  var this_ = this;
  return jum_wait('start test function ' + funcname + ' after ' + other_funcname,
       function() {  if (other_asyncObj.state_ == 'resumed') {jum.resume(funcname, id, funcobj); return true;} else return false;},
       null,
       timeout_millis);  
}

// for now, just used for reportAsync
    // deprecated and undocumented. use waitFor instead
    // if this function is considered async then (in a simple-minded implementation) it could
    // wait forever for itself, or at least deadlock with another call to waitAll.
    // if it is not considered async, then its completion can't be tracked.
    /*
     * Wait for all unfinished continuations to complete (that is, calls to <var>pause</var>
     * without matching calls to <var>resume</var> ).
     *
     * This is not supported if <var>setTimeout</var> is not available.
     *
     * @param funcname The current test function name (possibly excluding 'test' or 'test_').
     * @param funcobj The function to call when waiting is complete.
     * @param timeout_millis Optional. Number of millis to wait for completion. Defaults to jum.DEFAULT_TIMEOUT_MILLIS.
     * @return true if nothing to wait for. false if something to wait for and within timeout.
     * @throws Error if something to wait for and timeout has expired
     */
     // param abort_all Optional. If true, then if the timeout happens, all tests are abandoned.
     // CLIMETHOD void waitAll(String funcname, Function funcobj, Number timeout_millis)

JUM.prototype.waitAll = function(funcname, funcobj, timeout_millis) {
  if (!jum_has_settimeout()) {
    //throw new Error("waitAll not supported when setTimeout is not available");
    jum.warn("waitAll called when setTimeout is not available, so just calling function");
    funcobj();
    return true;
  }
  bu_alert('(jsunit_wrap.js) in waitAll(' + funcname + ')');
  if (typeof timeout_millis == 'undefined') timeout_millis = jum.DEFAULT_TIMEOUT_MILLIS;
  var this_ = this;
  return jum_wait('call function ' + funcname,
       function() {  if (this_.areAsyncDone()) {funcobj(); return true;} else return false;},
       function() { 
         var active = this_.getActiveAsync();
         var mess = "waitAll for function '" + funcname + "' failed to complete in " + timeout_millis + " millis; these are still oustanding: " + active.join(', ');
         jum.debug(mess);
        throw Error(mess);
       },
       timeout_millis);
}

JUM.prototype.attemptReportAsync = function() {
  if (this.areAsyncDone()) {this.reportAsync(); return true;}
  else {return false;}
}

// registered as a test function, as a "trick" for getting something run at the end
function jum_special_reportAsync() {
   var this_ = jum;
   jum_wait('reportAsync', 
            function() {return this_.attemptReportAsync()}, 
            function() {return this_.reportAsync()},
            jum.DEFAULT_TIMEOUT_MILLIS);
}

// called after all asyncs are completed, or we've timed out waiting for them.
JUM.prototype.reportAsync = function() {
  var total_count = 0;
  var active_count = 0;
  var unknown_count = 0;
  var status_counts = {};
  var failures = '';
  var errors = '';
  for(var key in this.asyncs_by_key_) {
    var asyncObj = this.asyncs_by_key_[key];
    ++total_count;
    if (asyncObj.state_ != 'resumed') ++active_count;

    switch(asyncObj.status_) {
    case JUM.STATUS_UNKNOWN: ++unknown_count; break;
    case JUM.STATUS_FAILED:
       failures += jum_async_except_string(key, asyncObj);
       break;
    case JUM.STATUS_ERROR:
       errors += jum_async_except_string(key, asyncObj);
       break;
    }

    if (typeof status_counts[asyncObj.status_] == 'undefined') status_counts[asyncObj.status_] = 1;
    else status_counts[asyncObj.status_]++;
  }
  if (total_count == 0) {jum.debug("no asyncs to report"); return;}
  var report = '';
  report += '==== Async Summary ====\n';
  report += 'Total async: ' + total_count + "\n";
  report += 'Never resumed: ' + active_count + "\n";
  report += 'In unknown state: ' + unknown_count + "\n";
  report += 'Async failed: ' + this.failed_async_count_ + "\n";
  report += 'Async error: ' + this.error_async_count_ + "\n";
  report += 'Async passed: ' + this.passed_async_count_ + "\n";
  if (failures != '') report += "Failures:\n" + failures;
  if (errors != '') report += "Errors:\n" + errors;
/*
  for(var s in status_counts) {
    report += 'In status "' + s + '": ' + status_counts[s] + "\n";
  }
*/
  jum.info(report);
  if (typeof window != 'undefined' && typeof window.alert != 'undefined'/* && jum.name != 'mda'*/) alert(report);
}


// Determine what JsUnit implementation is available using
// object detection on some random globals defined by them.
var jum_is_jsunit_hieatt = typeof JsUnitException === 'function';
var jum_is_jsunit_schaible = typeof TestDecorator === 'function';

jum = new JUM();

jum.DEFAULT_TIMEOUT_MILLIS = 10000;

// controls debug output from the implementation of this wrapper itself.
jum.DEBUG_INTERNAL = false;

// this is only for use in debugging our wrapper itself.
function jum_debug(msg) {if (jum.DEBUG_INTERNAL) jum.debug('JUMDEBUG: ' + msg);}


// utility to convert the indexed member of args to a Boolean.
// alters args and returns it too.
// defaults to changing the last member in args.
function jum_arg_to_boolean(args, ind) {
  if (typeof ind == 'undefined' || ind == null) ind = args.length - 1;
  args[ind] = eval(args[ind]) ? true : false;
  return args;
}

var jum_uneval;
if (typeof burst != 'undefined' && typeof burst.Lang != 'undefined') jum_uneval = burst.Lang.uneval;
else if (typeof [].toSource != 'undefined') jum_uneval = function(o) {return o.toSource()};

// utility to uneval all members of args, replacing them with String.
// alters args and returns it too.
function jum_uneval_args(args) {
  for(var i=0;i<args.length;++i) {
    var arg = args[i];
    //jum_debug("    " + funcname + ' ' + i + " " + arg);
    //NOTE: we have to consider String and string to be equal, so we can't just do
    // uneval of String object, because it puts quotes around it.
    //if (typeof arg == 'object') {
      args[i] = jum_uneval(arg);
      //}
  }
  return args;
}

// return a function for assertEquals which does deep comparison, if jum_uneval is available.
function jum_wrap_assertEquals(func) {
  if (!jum_uneval) {return func;}
  return function() {func.apply(null, jum_uneval_args(arguments))};
}

/*
Edward Hieatt jsunit, http://jsunit.net

For basics, see jsunit/app/jsUnitCore.js

How Hieatt JsUnit looks for and runs tests....

It looks for 'function ' followed by 'test' if can get text of script
(even in comments such as this one).
Otherwise, looks for members with leading 'test'.

Eventually runs test via:     eval('this.containerTestFrame.' + this._testFunctionName + '();'); 

You can run a particular test page via a url like:
<pre>
  d:/foo/jsunit/testRunner.html?testpage=d:/myTests/aTest.html
or
  http://localhost/foo/jsunit/testRunner.html?testpage=../../myTests/aTest.html
</pre>

You can define a function window.exposeTestFunctionNames() which returns an Array of 
String function names.

It will check for a global variable 'suite' a function returning an instance of JsUnitTestSuite.
The object contains a set of html pages. An example might be:
<pre>
  function suite() { var x = new JsUnitTestSuite(); x.addTestPage('foo.html'); return x;}
</pre>

You can access arguments from the testRunner.html query string by <code>top.jsUnitParmHash['parameterName']</code>

*/
  // throw "attempting to use builtin JsUnit";
  // copied and modified from burst.BurstError.js
  function JUMAssertFailure(msg) {
    if(!(this instanceof JUMAssertFailure)){
    	return new JUMAssertFailure(msg);
    }
    this.isJumError = 1;
    this.message = (new String(msg)) || '';
    return this; // silence warnings
  }
  JUMAssertFailure.prototype = new Error();
  JUMAssertFailure.prototype.constructor = JUMAssertFailure;
  JUMAssertFailure.prototype.name = 'JUMAssertFailure';

  function jum_assertTrue(msg, cond) {
    if (arguments.length == 1) {cond = msg; msg = null;} 
    if (!eval(cond)) throw JUMAssertFailure("assertTrue('" + cond + "') failed" + (msg ? ': ' + msg : ''));
  }
  function jum_assertFalse(msg, cond) {
    if (arguments.length == 1) {cond = msg; msg = null;} 
    if (eval(cond)) throw JUMAssertFailure("assertFalse('" + cond + "') failed" + (msg ? ': ' + msg : ''));
  }
  function jum_assertEquals(msg, expected, actual) {
	 if (arguments.length == 2) {actual = expected; expected = msg; msg = null;} 
	 if (expected == actual) return ;
	 var expected_u = expected;
	 var actual_u = actual;
	 if(jum_uneval){
		 expected_u = jum_uneval(expected); 
		 actual_u = jum_uneval(actual); 
		 if(expected_u == actual_u){ return ; }
	 }
	var es = ("assertEquals" + (msg ? '(' + msg + ')' : '') + 
				" failed: expected |" + expected_u + "| (typeof=" + (typeof expected) + ")" +
				  ", but got |" + actual_u + "| (typeof=" + (typeof actual) + ")");
	throw new JUMAssertFailure(es);

	// jum.debug(JUMAssertFailure("foo!"));
	/*
	 throw JUMAssertFailure("assertEquals" + 
				(msg ? '(' + msg + ')' : '') + 
				" failed: expected |" + expected_u + "| (typeof=" + (typeof expected) + ")" +
				  ", but got |" + actual_u + "| (typeof=" + (typeof actual) + ")");
	*/
  }

  /*
   * interface methods
   */
  jum.verbose = false;
  jum.name = 'mda';
  jum.debug = function(line){
    if(jum.verbose){
    	jum.my_output_('DEBUG', line);
    }
  };
  jum.info = function(line){
    jum.my_output_('INFO', line);
  };
  jum.warn = function(line){
    jum.my_output_('WARN', line);
  };

  jum.assertTrue = jum_assertTrue;
  jum.assertFalse = jum_assertFalse;
  // not wrapped because native one does the right thing and wants to show types.
  jum.assertEquals = jum_assertEquals;
  jum.isFailureException = function(e) {return (e instanceof JUMAssertFailure)};

  jum.areTestsChosen = function() {return !!jum.alltests_;};
  jum.getChosenTestGroupName = function() {return null};
  jum.setTests = function(alltests) {jum.alltests_ = alltests};
  
  jum.runAll = function() {
    this.initCounts_();

    if (!this.alltests_) {
      var mess = "(jsunit_wrap.js) setTests never run before runAll"; 
      alert(mess); 
      throw Error(mess);
    }
    var groupnames = jum_get_groupnames(this.alltests_);
    jum.report_("\n\n=============================================\n"
    			+"There are " + groupnames.length + " test groups...\n"
    			+ "=============================================");
    for(var i=0;i<groupnames.length;++i) {
      jum.runOneGroup_(groupnames[i]);
    }

    this.summaryReport_();
  };


  jum.runGroup = function(groupname) {
    this.initCounts_();
    this.runOneGroup_(groupname);
    this.summaryReport_();
  };


  /*
   * private variables and methods
   */
  jum.alltests_ = null;
  jum.report_ = function(line){
     jum.my_output_('TEST', line);
  };

  jum.my_println_ = function(line) {
    jum.initOutput_();
    if (jum.my_println_ != arguments.callee) {
    	jum.my_println_(line);
    }
  };

  jum.my_output_ = function(categ, line) {
     if(typeof categ != 'undefined' && categ){ line = categ + ': ' + line; }
     this.my_println_(line);
  };

  jum.set_output_window_ = function(win) {
    if (typeof this.output_win_ != 'undefined' && !this.output_win_.closed) {this.output_win_.close()}
    this.output_win_ = win; 

    this.output_element_ = win.document.getElementById('output');
    if (!this.output_element_) throw Error("no element with id 'output' in output window");
  };

  jum.create_output_window_ = function() {
    var win = window.open('','','width=600,height=500,status=no,resizable=yes,scrollbars=yes');
    var doc = win.document;
    doc.write('<html><head><title>Test Output</title></head><body><h1>Test Output</h1><pre id="output"></pre></body></html>');
    doc.close();
    win.focus(); 
    return win;
  };

  jum.initOutput_ = function() {
     // sigh, KJS has no "load" global, just: "debug", "print", "version" 
     if((typeof load != 'undefined' || typeof debug != 'undefined') && typeof print != 'undefined'){
        jum.my_println_ = function(line){
            print(line)
        };
     }else if (typeof window != 'undefined'){
     	jum.set_output_window_(jum.create_output_window_());
        jum.my_println_ = function(line) {
			// break up multiple lines
			var lines = line.split(/\n/);
			if (lines.length > 1) {
			    //alert('(jsunit_wrap.js) splitting multi-line: ' + line);
			    for(var i=0;i<lines.length;++i) jum.my_println_(lines[i]);
			    return;
			}
		  	if (this.output_win_.closed) {
	             alert("(jsunit_wrap.js) output window is closed; can't output: " + line);
		     alert('(jsunit_wrap.js) throwing exception since no test output window');
		     throw Error('(jsunit_wrap.js) no test output window');
		     //return;
	          }
	          this.output_win_.focus();
	          // on Moz, a Text child is enough.
		  	// on IE, the \n does nothing and it is a long run-on line.
		  	var doc = this.output_win_.document;
	          var text_node = doc.createTextNode(line/* + "\n"*/);
	          var div_node = doc.createElement('div');
		  		div_node.appendChild(text_node);
	          this.output_element_.appendChild(div_node);
        };
     } else {
     	throw Error("(jsunit_wrap.js) no way to display test debug output: no global 'window' or 'print' symbols");
     }
  };

  jum.initCounts_ = function() {
    this.all_count_ = 0;
    this.failed_count_ = 0;
    this.error_count_ = 0;
    this.passed_count_ = 0;
    this.untested_ = 0;
  };

  jum.summaryReport_ = function() {
    var skipped_count = this.untested_;
    var passed_count = this.passed_count_ - skipped_count;

    jum.report_('=======================');
    jum.report_('Total tests: ' + this.all_count_);
    jum.report_('Failed: ' + this.failed_count_);
    jum.report_('Error: ' + this.error_count_);
    jum.report_('Skipped: ' + skipped_count);
    jum.report_('Passed: ' + passed_count);
  };

  var JUM_CATCH_EXCEPTIONS = true;

  jum.runOneGroup_ = function(groupname) {
    var groupdef = jum_get_groupdef(jum.alltests_, groupname);
    var testdefs = jum_get_testdefs(groupdef);

    this.all_count_ += testdefs.length;

    jum.debug("group '" + groupname + "' has " + testdefs.length + " tests to run...");
    for (var i=0;i<testdefs.length; ++i) {
       var testdef = testdefs[i];
       var testname = testdef.testname;
       jum.debug("   test '" + testname + "' ...");
       var func = testdef.funcobj;

       var displayname = groupname + '.' + testname;
       var prefix = '   ';
       var suffix = " test '" + displayname + "'";
       var threw = false;

       if (JUM_CATCH_EXCEPTIONS) {
       try{ 
	 	func();
	 }catch(e){
         threw = true;
         if(e instanceof JUMAssertFailure) {
         	var str='';
         	if(e["fileName"]){str+=e.fileName+':';}
			if (e["lineNumber"]){str+=e.lineNumber+' ';}
			str+=e.message;
            jum.report_(prefix + 'FAILED' + suffix + ': ' + str);
            ++this.failed_count_;
         }else{
         	var str='';
         	if(e["fileName"]){str+=e.fileName+':';}
			if (e["lineNumber"]){str+=e.lineNumber+' ';}
			str+=e.message;
	     	jum.report_(prefix + 'ERROR' + suffix + ' toString: ' + e.toString() + ' message: ' + e.message + 
			 (typeof e.description != 'undefined' ? ' description: ' + e.description : '')
			 + str);

		 // FIXME: this shouldn't be necessaray, but seems to be in rhino to get any reasonable info
		 for(var x in e){
		 	jum.debug(x+": "+e[x]);
		 }
            ++this.error_count_;
         }
       }
       }
       else {func()}
       if (!threw) {
          jum.debug(prefix + 'PASSED' + suffix);
          ++this.passed_count_;
       }
    }
  };

jum.init = function(scopeobj) { jum_choose_tests(scopeobj); this.inited_ = true;}

/** By default match as 'test_' + groupname + '_' + testname */
// old test function regexp: /^test(_([^_]+)){2,}$/
jum.TEST_FUNCTION_REGEXP = /^test_([^_]+)_?([^_]+)/;

jum.DEFAULT_GROUPNAME = 'all';

if (typeof window != 'undefined' && window != this) alert("(jsunit_wrap.js) window != this: " + window + " != " + this);

//var jum_global = this;
var jum_global = (typeof window != 'undefined' ? window : this);


function jum_is_ie() {
  if (typeof navigator == 'undefined') return false;
  var ua = navigator.userAgent.toLowerCase();
  return ua.indexOf('opera') == -1 && ua.indexOf('msie') != -1 && (typeof navigator.__ice_version == 'undefined');
}

function jum_is_moz() {
  if (typeof navigator == 'undefined') return false;
  var ua = navigator.userAgent.toLowerCase();
  return ua.indexOf('gecko') != -1 && ua.indexOf('rv:') != -1;
}

function jum_is_opera() {
  if (typeof navigator == 'undefined') return false;
  var ua = navigator.userAgent.toLowerCase();
  return ua.indexOf('opera') != -1;
}

function jum_are_scripts_parseable() {
  return !jum_is_opera();
}

function jum_are_global_functions_enumerable() {
  return !jum_is_ie();
  // TEMPORARY
  // enumerable if in a shell (non-browser)
  return typeof window == 'undefined';
}

function jum_has_xmlhttp() {return jum_is_ie() || jum_is_moz()}

// IE5.0 only has "Microsoft.XMLHTTP"
var jum_xmlhttp_progid = "Microsoft.XMLHTTP"; // "Msxml2.XMLHTTP"
// make it global so can choose whether to create multiple instances
var jum_xmlhttp = null;

function jum_xmlhttp_load_url(url) {
  if (!jum_xmlhttp) {
    //alert('(jsunit_wrap.txt) about to create xmlhttp');
    jum_xmlhttp = jum_is_ie() ? new ActiveXObject(jum_xmlhttp_progid) : new XMLHttpRequest();
  }
  //else {alert('(jsunit_wrap.txt) about to re-use xmlhttp');}
  jum_xmlhttp.open('GET', url, false);
  jum_xmlhttp.send(null);
  var text = jum_xmlhttp.responseText;
  //alert('(jsunit_wrap.txt) xmlhttp on ' + url + ' got text.length=' + text.length);
  return text;
}

function jum_get_script_elements() {
  if (true) return window.document.documentElement.getElementsByTagName("script");

  var scripts = window.document.getElementsByTagName("script");
  var num = scripts.length;
  // ok in Opera, IE. undefined in Mozilla
  alert('window.document.scripts.length=' + (window.document.scripts ? window.document.scripts.length : undefined));
  // ok in Opera, IE. just 1 for Moz.
  alert('window.document.getElementsByTagName("script").length=' + window.document.getElementsByTagName("script").length + ' (was ' + num + ')');
  // ok in IE. just 1 for Moz.
  alert('window.document.getElementsByTagName("SCRIPT").length=' + window.document.getElementsByTagName("SCRIPT").length);
  // ok in IE, Moz, Opera. 
  alert('window.document.documentElement.getElementsByTagName("script").length=' + window.document.documentElement.getElementsByTagName("script").length);
     // non-null in Opera. null for Moz, IE.
     //  alert('window.document.firstChild.firstChild=' + (window.document.firstChild ? window.document.firstChild.firstChild : undefined));
  if (false)
     scripts = window.document.scripts;    // IE-specific
  else if (false)
     scripts = window.document.getElementsByTagName('script');
  else if (true) {
    scripts = [];
    // html/head
    //var head = window.document.firstChild.firstChild; // doesn't work in Mozilla
    var head = window.document.getElementsByTagName('head')[0];
    var child_count = 0;
    for(var n=head.firstChild; n; n=n.nextSibling) {
      ++child_count;
      if (n.nodeType == 1 && n.nodeName && n.nodeName.toLowerCase() == 'script') scripts.push(n);
    }
    //if (scripts.length == 0) throw Error("no script elements found, to determine test functions, out of " + child_count + " head children")
    if (scripts.length == 0) {throw new Error("no script elements found, to determine test functions, out of " + child_count + " head children");}
  }
  return scripts;
}

/*
* To find functions by iterating through the global object enumeration:
*   allowed by: Opera, Mozilla, spidermonkey, rhino
*   not allowed by: IE
*
* To read script content:
*   IE and Mozilla have script.text only if no 'src', but have xmlhttp for retrieval.
*   Opera supports script.text sometimes, even if it came from a remote 'src'?
*   Konqueror has no document.scripts (use getElementsByTagName)
*     (note this is a bug in hieatt jsunit 1.3.3).
*   non-browser shells of course have no script elements.
*
* A benefit of parsing script content is that it preserves declaration order,
* unlike iterating through global symbols. A difficulty is that it is necessary
* to parse carefully to avoid functions in comments, nested functions, etc.
* So as we parse we test each symbol found to make sure it is an actual function.
*/
function jum_parse_window_scripts() {
  var scripts = jum_get_script_elements();
  alert("(jsunit_wrap.js) about to examine " + scripts.length + " scripts for test function names");
  var func_names = [];
  for(var i=0;i<scripts.length;++i) {
    var script = scripts[i];
    //alert("script.src=" + script.src + " script.text=" + (script.text ? script.text.length : undefined));
    var source_text;
    if (script.text) {
      source_text = script.text;
      //alert("have script.text, script.text.length=" + source_text.length + " script.src=" + script.src);
    }
    else if (script.src) {
      if (jum_has_xmlhttp())
        source_text = jum_xmlhttp_load_url(script.src);
      else {
        alert("unsupported: script element has src='" + script.src + "' and no text and loading src is unsupported");
        throw Error("unsupported loading script src, for " + script.src);
      }
      //alert("(jsunit_wrap.js) script.src=" + script.src + " read contents length=" + source_text.length);
    }
    else {
      //alert("script with neither src nor text");
    }

    // Our regexp does not exclude matches in comments, etc., so we check if it is a function member.
    // The ugliness is below is because IE5.0 doesn't support capturing regexp /g loops
    var a = [];
    var re = /function\s+([\-\_\$\w]+)/g;
    /* 
    while(true) {
      var matches = re.exec(source_text);
      if (!matches) break;
      var funcname = matches[1];
    */

    var matches = source_text.match(re);
    //alert("found " + (matches ? matches.length : 0) + " matches to function regexp in " + script.src);
    if (matches) {
      for(var j=0;j<matches.length;++j) {
         var funcname = matches[j];
	 var matches2 = funcname.match(/function\s+([-_$\w]+)/);
	 funcname = matches2[1];
         if (typeof jum_global[funcname] == 'function') a.push(funcname);
         else {
	      //if (script.src.indexOf('test') != -1) alert("found function named '" + funcname + "' by parsing but not a global function, so skipping");
         }
        //source_text = source_text.substring(re.lastIndex);
      }
    }

    //alert('(jsunit_wrap.js) script ' + (script.src ? script.src : 'inline') + ' has ' + a.length + " named functions: " + a);
    func_names = func_names.concat(a);
  }
  //alert("(jsunit_wrap.js) returning " + func_names.length + " function names (not all are tests)"); // + func_names.join(','));
  return func_names;
}

/*
* Return members of the object (by default jum_global) that are functions.
*/
function jum_get_object_function_names(scopeobj) {
  if (!scopeobj) scopeobj = jum_global;
  var a = [];
  var count = 0;
  for(var k in scopeobj) {
    count++;
	
    // if (k.indexOf("jum") == -1 && k.indexOf("JUM") == -1) alert("examining: " + k);
    // Safari 1.2 will crash on typeof window[k] for a bunch of symbols.
    if (k == 'removeEventListener' || k == 'history' || k == 'name' || k == 'onselect') {continue;}
    // sigh, in Mozilla even this will cause an exception: typeof window['fullScreen']
    if (k == 'fullScreen' || k == 'scrollMaxX' || k == 'scrollMaxY') {continue;}
	// Sigh, I'm not even going to try to find all the dangerous symbols.
	// just apply the symbol test function now.
    
    if (!jum_is_test_function(k)) {continue;}
    
    if (typeof scopeobj[k] == 'function') {
    	a.push(k);
    }
  }
  jum_debug("returning all function members in object: " + a); 
  return a;
}

function jum_is_test_function(funcname, re) {
  if (!re) re = jum.TEST_FUNCTION_REGEXP;

  var parts = funcname.match(re);
  if (!parts) {
     //warn in some cases
     if (funcname.indexOf('jum_')==-1 && funcname.indexOf('test')!=-1)
	jum_debug("global function named '" + funcname + "' has 'test' but did not match pattern, so skipping");
     return false;
  }
  return true;
}

/*
* Filter the provided array of function names to ones whose names
* names match the provided regexp (by default jum.TEST_FUNCTION_REGEXP).
*/
function jum_just_test_functions(func_names, re) {

  var test_func_names = [];
  var all_names = [];
  for(var i=0;i<func_names.length;++i) {
    var funcname = func_names[i];
    //jum_debug("checking symbol '" + funcname + "', number " + i);
    all_count++;
    all_names.push(funcname);

    if (jum_is_test_function(funcname, re)) {
      test_func_names.push(funcname);
    }
  }
  var test_count = test_func_names.length;
  var all_count = all_names.length;
  //jum.info("(jsunit_wrap.js) found " + all_count + " symbols, and " + test_count + " tests");
  // bu_alert("(jsunit_wrap.js) all symbols: " + all_names.join(' '));
  return test_func_names;
}

// filter test function names by groupname
function jum_function_names_in_group(test_func_names) {
  var a = [];
  for(var i=0;i<test_func_names.length;++i) {
     var n = test_func_names[i];
     if (n.indexOf(groupname) != -1) a.push(n);
  }
  return a;
}

// declare the tests if need be
function jum_choose_tests(scopeobj) {
  if (jum.areTestsChosen()) {
    bu_alert("tests already chosen");
    return;
  }

  if (arguments.length == 0 || !scopeobj) scopeobj = jum_global;

  var test_func_names;
  // see if user has supplied a set of desired test function names
  // a flat Array of test function names that may have alaready been provided (see testnames.pl)
  if (typeof jum.TEST_FUNCTION_NAMES !== 'undefined') {
     test_func_names = jum.TEST_FUNCTION_NAMES;
     // iterate over and ensure they are actually in the scopeobj
     var filtered = [];
     var removed = [];
     for(var i=0;i<test_func_names.length;++i) {
       var name = test_func_names[i];
       if (typeof scopeobj[name] == 'undefined') {removed.push(name)}
       else filtered.push(name);
     }
     if (removed.length > 0) bu_alert("removed " + removed.length + " non-existent test function names: " + removed.join(','));
     test_func_names = filtered;
  } else {
  	
     if (jum_are_global_functions_enumerable()) {
       test_func_names = jum_get_object_function_names(scopeobj);
     }
     else if (jum_are_scripts_parseable()) {
       test_func_names = jum_parse_window_scripts();
     }
     else {throw new Error("no way to determine test functions");}
     
     // filter for ones whose names match the pattern
     //bu_alert("(jsunit_wrap.js) got " + test_func_names.length + " symbols before filtering for test functions");
     test_func_names = jum_just_test_functions(test_func_names);
  }

  bu_alert("(jsunit_wrap.js) got " + test_func_names.length + " test function names");

  // see if user has selected a desired group
  var groupname = jum.getChosenTestGroupName();
  if (groupname && groupname == '') groupname = null;
  
  if (groupname) {
     jum.info("(jsunit_wrap.js) running group " + groupname);
     test_func_names = jum_function_names_in_group(test_func_names);
  }
  
  // convert to alltests
  var alltests = jum_new_alltests();
  jum_initialize_alltests(alltests, scopeobj, test_func_names);
  
  // append our special test at the end
  if (jum_has_settimeout()) {
     jum_add_test(alltests, 'special', 'reportAsync', 'jum_special_reportAsync', jum_special_reportAsync);
  }
  else {
     jum.info('no setTimeout, not checking async completion');
     bu_alert('(jsunit_wrap.js) no setTimeout, not checking async completion');
  }

  // set tests
  jum.setTests(alltests);
}

function jum_get_groupdef(alltests, groupname) {
	return alltests.groups_by_name_[groupname];
}

//function jum_get_groupdef_testnames(groupdef) {return groupdef.test_names_}
function jum_get_testdefs(groupdef) {
	return groupdef.tests_array_;
}
function jum_get_groupnames(alltests) {
	return alltests.group_names_;
}

// get a flat Array of all function names in the alltests object
function jum_get_all_function_names(alltests) {
  var fnames = [];
  for(var i=0;i<alltests.groups_array_.length;++i) {
    var groupdef = alltests.groups_array_[i];
    var groupname = groupdef.groupname;
    jum_debug('concat of names for ' + groupname);
    //bu_alert("adding " + groupdef.function_names_.length + " functions to already " + fnames.length);
    // ENVBUG: konqeror 3.05 has buggy Array.concat
    if (false) {
       var new_len = fnames.length + groupdef.function_names_.length;
       fnames = fnames.concat(groupdef.function_names_);
       if (new_len !== fnames.length) throw Error("(jsunit_wrap.js) buggy Array.concat; now fnames.length = " + fnames.length);
    }
    else {
      for(var j=0;j<groupdef.function_names_.length;++j) {fnames.push(groupdef.function_names_[j])}
    } 
    // bu_alert("(jsunit_wrap.js) now have " + fnames.length + " names after adding " + groupdef.function_names_.length + " for group " + groupname);
  }
  bu_alert("(jsunit_wrap.js) returning " + fnames.length + " names from " + alltests.groups_array_.length + " groups");
  return fnames;
}

/*
* Returns an alltests object created from the array of test function names.
*
* @param re The regexp to apply to function names. Default is jum.TEST_FUNCTION_REGEXP.
* @param default_groupname. The groupname to assign to a test when the regexp matches only 
* 	the testname, not the groupname and testname. Default is 'all'.
*/
function jum_initialize_alltests(alltests, scopeobj, test_func_names, re, default_groupname) {
  if (!re) {
  	re = jum.TEST_FUNCTION_REGEXP;
  }
  if (!default_groupname) {
  	default_groupname = jum.DEFAULT_GROUPNAME;
  }
  if (!scopeobj) { scopeobj = jum_global;}
  
  for(var i=0;i<test_func_names.length;++i) {
    var funcname = test_func_names[i];
    var parts = funcname.match(re);
    if (!parts) {
    	throw Error("function name '" + funcname + "' does not match regexp " + re);
    }
    
    var groupname,testname=null;
    if (parts.length == 2) {
    	testname = parts[1]; 
    	groupname = default_groupname;
    } else {
    	groupname = parts[1];
    	testname = parts[2];
    }
    
    var funcobj = scopeobj[funcname];
    jum_add_test(alltests, groupname, testname, funcname, funcobj);
  }
	
  return alltests;
}

function jum_new_alltests() {
  return {
    groups_by_name_ : {},
    groups_array_ : [],
    group_names_ : []
  }
}

function jum_add_test(alltests, groupname, testname, funcname, funcobj) {
  
  var groupdef=null;
  
  if (typeof alltests.groups_by_name_[groupname] != 'undefined') {
    groupdef = alltests.groups_by_name_[groupname];
  } else {
    groupdef = alltests.groups_by_name_[groupname] = {
		groupname: groupname, 
		tests_by_testname_: {}, 
		tests_array_ : [], 
		test_names_ : [],
		function_names_ : []
	};
    alltests.groups_array_.push(groupdef);
    alltests.group_names_.push(groupname);
  }
  var testdef = {testname: testname, funcname: funcname, funcobj: funcobj};
  
  groupdef.tests_by_testname_[testname] = testdef;
  groupdef.tests_array_.push(testdef);
  groupdef.test_names_.push(testname);
  groupdef.function_names_.push(funcname);
  
  if (groupdef.function_names_.length == 0) throw new Error("(jsunit_wrap.js) buggy Array.push");
}


// vim:ts=8:et
