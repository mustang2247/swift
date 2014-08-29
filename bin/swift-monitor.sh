#!/bin/bash
echo $$ > $SWIFT_PID
exec $@
