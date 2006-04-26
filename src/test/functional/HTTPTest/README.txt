Notes on running HTTPTest

Usage
-----

    python cosmo_fullsuite.py host=localhost port=8080 path=/cosmo

Tinderbox Usage
---------------

The Cosmo Functional Test Tinderbox uses HTTPTest in the following manner.

  - Download and extract the latest OSAF Server Bundle
  - Start the OSAF Server using all defaults
  - Export the latest HTTPTest code
  - run "python cosmo_fullsuite.py"
  - Stop the OSAF Server

