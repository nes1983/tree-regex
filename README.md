tree-io
=======

A fast regular expression engine that produces parse trees (ASTs). It does so in linear time in the size of the text matched and scales in O(m*log(m)) in the size of the pattern.

## Algorithm

The algorithm is described in

* Niko Schwarz. Scaleable Code Clone Detection. PhD thesis, University of Bern, February 2014. [PDF](http://scg.unibe.ch/archive/phd/schwarz-phd.pdf)
* Aaron Karper. Efficient regular expressions that produce parse trees. Master thesis, University of Bern, December 2014. [PDF](http://scg.unibe.ch/archive/masters/Karp14a.pdf)
