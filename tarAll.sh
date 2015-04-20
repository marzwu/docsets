#!/bin/bash  
#cd ~/Downloads  
for f in *.docset
do  
	n=${f:0:${#f}-7}
	tar --exclude='.DS_Store' -cvzf $n.tgz $n.docset
    echo $f  
done  