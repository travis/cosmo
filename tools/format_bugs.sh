#!/bin/bash
ARGS_NUM=2
E_OPTERROR=65

if [ $# -lt "$ARGS_NUM" ]  # Script invoked with no command-line args?
then
  echo "Usage: `basename $0` milestone output_format"
  exit $E_OPTERROR        # Exit and explain usage, if no argument(s) given.
fi

MILESTONE=$1
OUTPUT_FORMAT=$2
curl -sS "https://bugzilla.osafoundation.org/buglist.cgi?query_format=advanced&product=Cosmo&target_milestone=$MILESTONE&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&ctype=atom" | xsltproc buglist_to_$OUTPUT_FORMAT.xslt -

