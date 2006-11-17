This whole directory is necessary only if you are a maintainer, not a programmer
using the burst library.

This directory only contains tools written by us, or config files/extensions
written by us for other people's tools.

We do not redistribute any tools written by others here; you have to download/install them
yourself.

readme.txt          - this file

JsFakeDom - a fake document object
  BUFakeDom.js        - implementation of a fake DOM, for command-line testing

JsSrcFilter - extracting code or doc from javascript
  Doxyfile_api.txt    - doxygen config file when generating doc for a tree
  Doxyfile_single.txt - doxygen config file when generating doc for a single file
  src2code.pl         - extract just code from source files, with some compression
  src2doc.pl          - extract just comments from source, with some generation 
  src2doc_extend.pm   - hook module for src2doc.pl

JsTestManager - wrapper/replacement for JsUnit implementations
  jsunit_post.js      - part of the JsUnit wrapper
  jsunit_wrap.js      - a wrapper for the hieatt and shaible JsUnit implementations
  test_jsunit_wrap.js - a little standalone test of just the JsUnit wrapper
  testnames.pl        - perl utility to grep for test function names (not used)

JsLink - unfinished attempt at a javascript linker
