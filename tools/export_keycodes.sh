#!/bin/sh

xmodmap -pk | tr -d ' +' | perl -pe 's/\(.+?\)/ /g' | awk '$1=$1' | sed 's/ /,/g'
