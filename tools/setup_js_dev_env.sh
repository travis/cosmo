#!/bin/bash
cd cosmo-js;
mvn package;
cd ../;
mkdir cosmo/src/main/webapp/js-dev;
cd cosmo/src/main/webapp/js-dev;
ln -s ../../../../../cosmo-js/src/cosmo;
ln -s ../../../../../cosmo-js/dojo-src/* ./;

