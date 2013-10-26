package edu.berkeley.path.beats.test.simulator

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import edu.berkeley.path.beats.simulator._
import edu.berkeley.path.beats.control.predictive.{RampMeteringControl, RampMeteringControlSet, AdjointRampMeteringPolicyMaker, ScenarioConverter}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.log4j.Logger
import scala.collection.immutable.TreeMap

/**
 * Created with IntelliJ IDEA.
 * User: jdr
 * Date: 10/24/13
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(classOf[JUnitRunner])
class AdjointTest extends FunSuite with ShouldMatchers {
  val logger = Logger.getLogger(classOf[AdjointTest])
  test("woohoo") {
    val scenario = ObjectFactory.createAndLoadScenario("/Users/jdr/Desktop/bla.xml")

    scenario.initialize(30, 0, 1000, 30, "xml", "hi", 1, 1)

    val meters = {
      val meters = new RampMeteringControlSet

    val net = scenario.getNetworkSet.getNetwork.get(0).asInstanceOf[Network]

    val mainline = ScenarioConverter.extractMainline(net)
    val mlNodes = mainline.map {
      _.getBegin_node
    }
    TreeMap(ScenarioConverter.extractOnramps(net).map {
      onramp => mlNodes.indexOf(onramp.getEnd_node) -> onramp
    }: _*).values.foreach{or => {
      val meter = new RampMeteringControl
      meter.min_rate = 0.0
      meter.max_rate = 1.0
      meters.control.add(meter)
    }}
      meters
    }
    val pm = new AdjointRampMeteringPolicyMaker

    val policy = pm.givePolicy(
      scenario.getNetworkSet.getNetwork.get(0).asInstanceOf[Network],
      scenario.getFundamentalDiagramSet,
      scenario.getDemandSet.asInstanceOf[DemandSet],
      scenario.getSplitRatioSet.asInstanceOf[SplitRatioSet],
      scenario.getInitialDensitySet.asInstanceOf[InitialDensitySet],
      meters,
      scenario.getSimdtinseconds
    )
    policy.print();
  }

}
