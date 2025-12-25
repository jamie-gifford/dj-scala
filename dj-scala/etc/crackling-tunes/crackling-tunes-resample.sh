#!/bin/bash

for src in ./* ; do
    dst=${src%\.flac}-441.flac
    if [ "$src" != "$dst" ] ; then
        echo Resampling $src
        # -v 0.99 to avoid clipping during resample/dither
        sox -v 0.99 "$src" "$dst" rate 44100
    fi    
done    

