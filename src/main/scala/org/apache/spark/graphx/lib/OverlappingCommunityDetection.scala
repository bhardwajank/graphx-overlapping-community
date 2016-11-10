package org.apache.spark.graphx.lib 
import org.apache.spark.graphx.{EdgeTriplet, Graph, Pregel, VertexId}

import scala.collection.mutable
import scala.reflect.ClassTag

object OverlappingCommunityDetection {

  /**
    * Run Overlapping Community Detection for detecting overlapping communities in networks.
    *
    * OLPA is an overlapping community detection algorithm.It is based on standarad Label propagation
    * but instead of single community per node , multiple communities can be assigned per node.
    *
    * @tparam ED the edge attribute type (not used in the computation)
    * @param graph           the graph for which to compute the community affiliation
    * @param maxSteps        the number of supersteps of OLPA to be performed. Because this is a static
    *                        implementation, the algorithm will run for exactly this many supersteps.
    * @param noOfCommunities 	the maximum number of communities to be assigned to each vertex
    * @return a graph with list of vertex attributes containing the labels of communities affiliation
    */

  def run[VD, ED: ClassTag](graph: Graph[VD, ED], maxSteps: Int, noOfCommunities: Int) = {
    require(maxSteps > 0, s"Maximum of steps must be greater than 0, but got ${maxSteps}")
    require(noOfCommunities > 0, s"Number of communities must be greater than 0, but got ${noOfCommunities}")

    val threshold: Double = 1.0 / noOfCommunities

    val lpaGraph: Graph[mutable.Map[VertexId, Double], ED] = graph.mapVertices { case (vid, _) => mutable.Map[VertexId, Double](vid -> 1) }

    def sendMessage(e: EdgeTriplet[mutable.Map[VertexId, Double], ED]): Iterator[(VertexId, mutable.Map[VertexId, Double])] = {
      Iterator((e.srcId, e.dstAttr), (e.dstId, e.srcAttr))
    }

    def mergeMessage(count1: mutable.Map[VertexId, Double], count2: mutable.Map[VertexId, Double])
    : mutable.Map[VertexId, Double] = {
      val communityMap: mutable.Map[VertexId, Double] = new mutable.HashMap[VertexId, Double]
      (count1.keySet ++ count2.keySet).map(key => {

        val count1Val = count1.getOrElse(key, 0.0)
        val count2Val = count2.getOrElse(key, 0.0)
        communityMap += (key -> (count1Val + count2Val))
      })
      communityMap
    }

    def vertexProgram(vid: VertexId, attr: mutable.Map[VertexId, Double], message: mutable.Map[VertexId, Double]): mutable.Map[VertexId, Double] = {
      if (message.isEmpty)
        attr
      else {
        var coefficientSum = message.values.sum

        //Normalize the map so that every node has total coefficientSum as 1
        val normalizedMap: mutable.Map[VertexId, Double] = message.map(row => {
          (row._1 -> (row._2 / coefficientSum))
        })


        val resMap: mutable.Map[VertexId, Double] = new mutable.HashMap[VertexId, Double]
        var maxRow: VertexId = 0L
        var maxRowValue: Double = Double.MinValue

        normalizedMap.foreach(row => {
          if (row._2 >= threshold) {
            resMap += row
          } else if (row._2 > maxRowValue) {
            maxRow = row._1
            maxRowValue = row._2
          }
        })

        //Add maximum value node in result map if there is no node with sum greater then threshold
        if (resMap.isEmpty) {
          resMap += (maxRow -> maxRowValue)
        }

        coefficientSum = resMap.values.sum
        resMap.map(row => {
          (row._1 -> (row._2 / coefficientSum))
        })
      }
    }

    val initialMessage = mutable.Map[VertexId, Double]()

    val overlapCommunitiesGraph = Pregel(lpaGraph, initialMessage, maxIterations = maxSteps)(
      vprog = vertexProgram,
      sendMsg = sendMessage,
      mergeMsg = mergeMessage)

    overlapCommunitiesGraph.mapVertices((vertexId, vertexProperties) => vertexProperties.keys)
  }
}

