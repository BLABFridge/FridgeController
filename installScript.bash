#!/bin/bash

#Write the current directory as the directory in the minicom dfl file, this is where minicom should log the data
echo "pu downdir $(pwd)" > minirc.dfl.requiredConfig
#move the minicom file to the correct directory, this requiredConfig file should not be platform dependant (untested)
mv ./minirc.dfl.requiredConfig /etc/minicom/minirc.dfl

#Do any more install stuff here
