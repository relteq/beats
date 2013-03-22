package edu.berkeley.path.beats.control;

import edu.berkeley.path.beats.jaxb.{Density, FundamentalDiagramProfile, Link=>BeatsLink}
import edu.berkeley.path.beats.simulator.{ControlAlgorithm, Scenario}
import java.{lang, util}
import org.wsj.PolicyMaker._
import org.wsj._
import org.wsj.SimpleFreewayLink
import scala.Some
import org.wsj.FreewayBC
import org.wsj.OnRamp
import org.wsj.PolicyMaker.MaxRampFlux
import org.wsj.FreewayIC
import org.wsj.FundamentalDiagram


/**
 * Created with IntelliJ IDEA.
 * User: jdr
 * Date: 3/20/13
 * Time: 4:18 PM
 * To change this template use File | Settings | File Templates.
 */
class ControllerAlgorithm_CRM_Adjoint() extends ControlAlgorithm() {
  println("creating adjoint...")

  def toFD(fd: FundamentalDiagramProfile) = {
        val f = fd.getFundamentalDiagram.get(0)
        val v = f.getFreeFlowSpeed.floatValue()
        val pj = f.getJamDensity.floatValue()
        val fmax = f.getCapacity.floatValue()
        FundamentalDiagram(v, fmax, pj)
    }

  def networkToFreeway(scenario: Scenario): WSJSimulatedFreeway = {
        val fds = {
                val fds = List(scenario.getFundamentalDiagramProfileSet.getFundamentalDiagramProfile.toArray:_*).asInstanceOf[List[FundamentalDiagramProfile]]
        fds.map{fd => (fd.getLinkId.toInt -> toFD(fd))}.toMap
        }
        val initialDensities = {
                val ids = List(scenario.getInitialDensitySet.getDensity.toArray:_*).asInstanceOf[List[Density]]
        ids.map {id => (id.getLinkId.toInt -> id.getContent.toDouble)}.toMap
        }
        // fuck it, betas are all 1
        val ps = Map(-1-> 0.0, -2 -> 4.0)
        val network = scenario.getNetworkList.getNetwork.get(0)
        val links = List((network.getLinkList.getLink.toArray):_*).asInstanceOf[List[BeatsLink]]
        val onramps = links.filter{_.getType == "onramp"}
        val mainlines = links.filter {_.getType == "freeway"}
        val orderedRampIds = onramps.sortBy {ramp => {
            val id = ramp.getId.toInt
            val absId = math.abs(id)
            absId
        }}.map{_.getId.toInt}
        val orderedMLIds = mainlines.map {ml => {
            val id = ml.getId.toInt
            id
        }
        }.sorted
        val onRamps = orderedRampIds.map {(id) =>{
            val fd = fds(id)
            val l0 = initialDensities(id)
            val rmax = fd.fMax
            val p = ps(id)
            OnRamp(-1, rmax, p, id)
        }}
        val lks = orderedMLIds.zip(onRamps).map{case (mlId, onRamp) => {
            new SimpleFreewayLink(
                    scenario.getLinkWithId(mlId.toString).getLength.floatValue(),
                    fds(mlId),
                    Some(onRamp),
                    mlId
            )
        }}
        new WSJSimulatedFreeway(lks)
    }

  override def compute(initialDensity: util.Map[String, lang.Double], splitRatios: util.Map[String, Array[lang.Double]], rampDemands: util.Map[String, Array[lang.Double]], scenario: Scenario) = {
    val dt = scenario.getSimDtInSeconds
    val freeway: WSJSimulatedFreeway = networkToFreeway(scenario)
    val ic: Profile[FreewayIC, SimpleFreewayLink] = {
      freeway.fwLinks.map{link=> {
        link ->
          FreewayIC(
            initialDensity.get(link.id.toString) / link.length,
            initialDensity.get(link.onRamp.get.id.toString)
          )
      }}.toMap
    }
    val bc: ProfilePolicy[FreewayBC, SimpleFreewayLink] = {
      val T = rampDemands.values().iterator().next().length
      (0 until T).map {t => {
        freeway.fwLinks.map{link => {
          link ->
            FreewayBC(
              rampDemands.get(
                link.onRamp.get.id.toString
              )(t) / dt,
              1.0
            )
        }}.toMap
      }}
    }
    val rampMetering = new AdjointRampMetering(freeway, bc, ic)
    rampMetering.setDt(dt)
    val optimizer = new SimpleUpdateROptimizer(rampMetering, 0, 1)
    optimizer.baseOptimizer.alpha = .15
    //val optimizer = new IpOptUpdateROptimizer(rampMetering, 0, 1)
    optimizer.baseOptimizer.maxEvaluations = 1000
    rampMetering.initialUScale = 1.0
    rampMetering.R = .1
    rampMetering.optimizer = optimizer
    val u: ProfilePolicy[MaxRampFlux, OnRamp] = rampMetering.givePolicy
    val T = u.length
    val uPairs = rampMetering.orderedRamps.map{or =>{
      or.id.toString -> {
        u.map{uu => new lang.Double(uu(or).flux)}.toArray
      }
    }}
    val uMap = new java.util.HashMap[lang.String, Array[lang.Double]]()
    uPairs.foreach{case (k,v) => uMap.put(k,v)}
    println(rampMetering.controlProfileToArray(u).map {_.mkString(" ")}.mkString("\n"))
    uMap
  }
}
