# Graphx Overlapping Community Detection

`graphx-overlapping-community` is a spark package thats finds the overlapping communities for large networks.It is based on label propagation algorithm but can detect communities that overlap.

The algorithm implemented here is based on 'Finding overlapping communities in networks by label propagation(http://iopscience.iop.org/article/10.1088/1367-2630/12/10/103018/meta)'

##Usage

```scala
import org.apache.spark.graphx.lib.OverlappingCommunityDetection

// find the overlapping communities with maxiteration 5 and max noOfCommunities per node 4 
val overlapCommunities = OverlappingCommunityDetection.run(graph,5,4)

```


