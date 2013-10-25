package edu.berkeley.path.beats.test.simulator

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import edu.berkeley.path.beats.simulator._
import edu.berkeley.path.beats.control.predictive.ScenarioConverter
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.log4j.Logger

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
    val scenario = ObjectFactory.createAndLoadScenario("/Users/jdr/Desktop/bla.xml");

    scenario.initialize(30, 0, 1000, 30, "xml", "hi", 1, 1);

    val adjointScen = ScenarioConverter.convertScenario(
      scenario.getNetworkSet.getNetwork.get(0).asInstanceOf[Network],
      scenario.getFundamentalDiagramSet,
      scenario.getDemandSet.asInstanceOf[DemandSet],
      scenario.getSplitRatioSet.asInstanceOf[SplitRatioSet],
      scenario.getInitialDensitySet.asInstanceOf[InitialDensitySet],
      scenario.getSimdtinseconds
    )
    println(adjointScen)
  }

}
