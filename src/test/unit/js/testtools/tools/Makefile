# these are tests for just the tools themselves.
# you probably don't care.

TOP := ..
include $(TOP)/buildscripts/externalconfig.mak
include $(TOP)/buildscripts/dirnames.mak

####
# JsLink
####

#TESTFILE=../burst/fix_ecma.js
#TESTFILE=../burst/URI.js ../burst/Alg.js
TESTFILE=../burst/Text.js ../burst/Lang.js ../burst/Alg.js

jslink: JsLinker.class
	perl -w jslink.pl $(TESTFILE)
test1:
	java $(RHINO_CP) org.mozilla.javascript.tools.shell.Main $(TESTFILE)

test: JsLinker.class
	java $(RHINO_CP):. JsLinker $(TESTFILE)

JsLinker.class: JsLinker.java
	javac $(RHINO_CP) JsLinker.java

####
# JsFakeDom
####

# test just BUFakeDom
testfake:
	$(JS) FakeDom/BUFakeDom.js FakeDom/test_BUFakeDom.js


#####
# JsTestManager
#####

# test just jsunit_wrap.js
testjsunit:
	$(JS) -f $(JSUNIT_WRAP) -f $(TOOLS_JTM_DIR)/test_jsunit_wrap.js -f $(JSUNIT_POST)

#####
# JsSrcFilter
#####
# test src2doc.pl

SRC2DOC     := $(PERL) $(TOOLS_JSFILTER_DIR)/src2doc.pl
SRC2CODE    := $(PERL) $(TOOLS_JSFILTER_DIR)/src2code.pl
JS_SRC_DIR  := ../burst

sdtest:
	$(SRC2DOC) $(JS_SRC_DIR)/Lang.js

#	$(SRC2DOC) $(JS_SRC_DIR)/webui/widgets/SortTable.js

# test src2code.pl
sctest:
	$(SRC2CODE) $(JS_SRC_DIR)/Lang.js
