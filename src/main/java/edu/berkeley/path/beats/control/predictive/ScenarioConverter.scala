package edu.berkeley.path.beats.control.predictive

import edu.berkeley.path.beats.simulator._
import edu.berkeley.path.ramp_metering._
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import edu.berkeley.path.ramp_metering.FreewayLink
import edu.berkeley.path.ramp_metering.PolicyParameters
import scala.Some
import edu.berkeley.path.ramp_metering.FreewayScenario
import edu.berkeley.path.ramp_metering.FundamentalDiagram
import scala.collection.immutable.TreeMap
import edu.berkeley.path.beats.jaxb.FundamentalDiagramSet
import edu.berkeley.path.ramp_metering.InitialConditions
import edu.berkeley.path.ramp_metering.PolicyParameters
import scala.Some
import edu.berkeley.path.ramp_metering.FreewayLink
import edu.berkeley.path.ramp_metering.Freeway
import edu.berkeley.path.ramp_metering.SimulationParameters
import edu.berkeley.path.ramp_metering.BoundaryConditions
import edu.berkeley.path.ramp_metering.FreewayScenario
import edu.berkeley.path.ramp_metering.FundamentalDiagram
import java.{lang, util}

/**
 * Created with IntelliJ IDEA.
 * User: jdr
 * Date: 10/24/13
 * Time: 12:05 PM
 * To change this template use File | Settings | File Templates.
 */

object ScenarioConverter {

  def convertScenario(net: Network,
                      fd: FundamentalDiagramSet,
                      demand: DemandSet,
                      splitRatios: SplitRatioSet,
                      ics: InitialDensitySet,
                      control: RampMeteringControlSet,
                      dt: Double) = {
    val mainline = extractMainline(net)
    val mlNodes = mainline.map {
      _.getBegin_node
    }
    val onramps = TreeMap(extractOnramps(net).map {
      onramp => mlNodes.indexOf(onramp.getEnd_node) -> onramp
    }: _*)
    val offramps = TreeMap(extractOfframps(net).map {
      offramp => mlNodes.indexOf(offramp.getBegin_node) -> offramp
    }: _*)
    val fds = fd.getFundamentalDiagramProfile.map {
      prof => net.getLinkWithId(prof.getLinkId) -> prof.getFundamentalDiagram.toList.head
    }.toMap
    val links = mainline.zipWithIndex.map {
      case (link, i) => {
        val fd = fds(link)
        val rmax = onramps.get(i) match {
          case Some(onramp) => fds(onramp).getCapacity
          case None => 0.0
        }
        val p = 0.8
        FreewayLink(FundamentalDiagram(fd.getFreeFlowSpeed, fd.getCapacity, fd.getCongestionSpeed), link.getLength, rmax, p)
      }
    }
    val freeway = Freeway(links.toIndexedSeq, onramps.keys.toIndexedSeq, offramps.keys.toIndexedSeq)
    val policyParams = extractPolicyParameters(dt, control, onramps.values.toList)
    val indexedDemand = demand.getDemandProfile.map {
      profile => {
        net.getLinkWithId(profile.getLinkIdOrg) -> profile.getDemand.toList.head
      }
    }.toMap
    val demands = (List(mainline.head) ++ onramps.values).map {
      ramp => {
        indexedDemand(ramp).getContent.split(",").map {
          _.toDouble
        }
      }
    }.toIndexedSeq.transpose

    val srIndex = splitRatios.getSplitRatioProfile.toList.map {
      profile => net.getLinkWithId(profile.getSplitratio.toList.head.getLinkOut) -> profile.getSplitratio.toList.head.getContent.split(",").map {
        _.toDouble
      }
    }.toMap
    val splits = offramps.values.map {
      srIndex(_).toIndexedSeq.map {
        1 - _
      }
    }.toIndexedSeq.transpose

    val bc = BoundaryConditions(demands, splits)
    val icLookup = ics.getDensity.toList.map {
      d => net.getLinkWithId(d.getLinkId) -> d.getContent.toDouble
    }.toMap
    val ic = InitialConditions(
      mainline.map {
        icLookup.getOrElse(_, 0.0)
      }.toIndexedSeq,
      (0 until mainline.length).map {
        i => {
          onramps.get(i) match {
            case None => 0.0
            case Some(onramp) => icLookup(onramp)
          }
        }
      }.toIndexedSeq
    )
    val index = control.control.toList.map{s => s.link -> (s.min_rate -> s.max_rate)}.toMap
    val simParams = SimulationParameters(bc, ic, Some(MeterSpec(onramps.values.map{index(_)}.toList)))
    (FreewayScenario(freeway, simParams, policyParams), onramps.values)
  }

  def extractPolicyParameters(dt: Double, control: RampMeteringControlSet, onramps: List[Link]) = {
    PolicyParameters(dt)
  }

  def extractMainline(net: Network) = {
    val mainlineSource = extractMainlineSource(net)
    val orderedMainlineBuffer = ListBuffer[Link](mainlineSource)
    var link = mainlineSource
    while (!link.isSink) {
      link = link.getEnd_node.getOutput_link.filter {
        _.getLinkType.getName == "Freeway"
      }.head
      orderedMainlineBuffer += link
    }
    orderedMainlineBuffer.toList
  }

  def extractOnramps(net: Network) = {
    net.getListOfLinks.toList.map {
      _.asInstanceOf[Link]
    }.filter {
      _.getLinkType.getName == "On-Ramp"
    }
  }

  def extractOfframps(net: Network) = {
    net.getListOfLinks.toList.map {
      _.asInstanceOf[Link]
    }.filter {
      _.getLinkType.getName == "Off-Ramp"
    }
  }

  def isMainlineSource(link: Link) = {
    link.isSource && link.getLinkType.getName == "Freeway"
  }

  def extractMainlineSource(net: Network) = {
    net.getListOfLinks.toList.map {
      _.asInstanceOf[Link]
    }.filter {
      isMainlineSource _
    }.head
  }

  implicit def toDoubleList( lst: List[Double] ) =
    seqAsJavaList( lst.map( i => i:java.lang.Double ) )
}


import ScenarioConverter.toDoubleList

class AdjointRampMeteringPolicyMaker extends RampMeteringPolicyMaker {
  def givePolicy(net: Network, fd: FundamentalDiagramSet, demand: DemandSet, splitRatios: SplitRatioSet, ics: InitialDensitySet, control: RampMeteringControlSet, dt: lang.Double): RampMeteringPolicySet = {
    val (scen, onramps) = ScenarioConverter.convertScenario(net, fd, demand, splitRatios, ics, control, dt)
    val flux = AdjointRampMetering.controlledOutput(scen, new AdjointRampMetering(scen.fw)).fluxRamp.transpose
    val set = new RampMeteringPolicySet
    onramps.zip(flux).foreach{ case (or, fl) => {
      val profile = new RampMeteringPolicyProfile
      profile.sensorLink = or
      profile.rampMeteringPolicy = fl.toList
      set.profiles.add(profile)
    }}
    set
  }
}