#!/bin/bash
set -x
java -jar ../../beats.jar conf/I80_beats.xml output 0 3600 2 1

