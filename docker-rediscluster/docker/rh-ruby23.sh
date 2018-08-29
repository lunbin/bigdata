#!/bin/bash
source /opt/rh/rh-ruby23/enable
export X_SCLS="`scl enable rh-ruby23 'echo $X_SCLS'`"
export PATH=$PATH:/opt/rh/rh-ruby23/root/usr/local/bin
