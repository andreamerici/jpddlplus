package sequential_problems;

import com.hstairs.ppmajal.extraUtils.PlannerUtils;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class H1InterferenceFreeTest {

    private void assertIF(String dom, String prob, boolean expectedIF, int expectedIFCount) throws Exception {
        PlannerUtils pu = new PlannerUtils();
        boolean actualIF = pu.isInterferenceFree(dom, prob);
        int actualIFCount = pu.getInterferenceFreeConditionsCount(dom, prob);
        System.out.println(String.format("[IF-Check] %s | expectedIF=%s, actualIF=%s, expectedCount=%d, actualCount=%d",
                dom, expectedIF, actualIF, expectedIFCount, actualIFCount));
        assertEquals("Wrong IF status for " + dom, expectedIF, actualIF);
        assertEquals("Wrong IF conditions count for " + dom, expectedIFCount, actualIFCount);
    }

    /**
     * Test su domini vari
     */
    @Test
    public void testH1SETUP1() throws Exception {
        PlannerUtils pu = new PlannerUtils();
        String dom = "unit_test_instances/car_linear_mt_sc/domain.pddl";
        String prob = "unit_test_instances/car_linear_mt_sc/sample.pddl";

        int hadd = pu.heuristicEstimate(dom, prob, "hadd");
        int hmax = pu.heuristicEstimate(dom, prob, "hmax");
        int hrmax = pu.heuristicEstimate(dom, prob, "hrmax");
        System.out.println("[H1][Processes] hadd=" + hadd + ", hmax=" + hmax + ", hrmax=" + hrmax);
        assertTrue("hadd should be non-negative", hadd >= 0);
        assertTrue("hmax should be non-negative", hmax >= 0);
        assertTrue("hrmax should be non-negative", hrmax >= 0);
        assertTrue("On same instance, hadd should be >= hmax", hadd >= hmax);

        int planSize = pu.getPlanSize(dom, prob, "hadd");
        System.out.println("[H1][Processes] planSize=" + planSize);
        assertEquals("On Processes example, expected optimal plan size = 27", 27, planSize);
    }

    @Test
    public void testH1SETUP2() throws Exception {
        PlannerUtils pu = new PlannerUtils();
        String dom = "unit_test_instances/gripper/domain.pddl";
        String prob = "unit_test_instances/gripper/prob02.pddl";

        int hadd = pu.heuristicEstimate(dom, prob, "hadd");
        int hmax = pu.heuristicEstimate(dom, prob, "hmax");
        System.out.println("[H1][Gripper] hadd=" + hadd + ", hmax=" + hmax);
        assertTrue("hadd should be non-negative", hadd >= 0);
        assertTrue("hmax should be non-negative", hmax >= 0);
        assertTrue("On gripper, hadd should be >= hmax", hadd >= hmax);

        int planSize = pu.getPlanSize(dom, prob, "hadd");
        System.out.println("[H1][Gripper] planSize=" + planSize);
        assertEquals("On Gripper example, expected optimal plan size = 19", 19, planSize);
    }

    @Test
    public void testH1SETUP3() throws Exception {
        PlannerUtils pu = new PlannerUtils();
        String dom = "unit_test_instances/blocks/domain.pddl";
        String prob = "unit_test_instances/blocks/task01.pddl";

        int hadd = pu.heuristicEstimate(dom, prob, "hadd");
        int hmax = pu.heuristicEstimate(dom, prob, "hmax");
        System.out.println("[H1][Blocks] hadd=" + hadd + ", hmax=" + hmax);
        assertTrue("hadd should be non-negative", hadd >= 0);
        assertTrue("hmax should be non-negative", hmax >= 0);
        assertTrue("On blocks, hadd should be >= hmax", hadd >= hmax);

        int planSize = pu.getPlanSize(dom, prob, "hadd");
        System.out.println("[H1][Blocks] planSize=" + planSize);
        assertEquals("On Blocks example, expected optimal plan size = 6", 6, planSize);
    }

    @Test
    public void testH1SETUP4() throws Exception {
        PlannerUtils pu = new PlannerUtils();
        String dom = "unit_test_instances/quantification_conditional/domain.pddl";
        String prob = "unit_test_instances/quantification_conditional/problem.pddl";

        int hadd = pu.heuristicEstimate(dom, prob, "hadd");
        int hmax = pu.heuristicEstimate(dom, prob, "hmax");
        System.out.println("[H1][QuantCond] hadd=" + hadd + ", hmax=" + hmax);
        assertTrue("hadd should be non-negative", hadd >= 0);
        assertTrue("hmax should be non-negative", hmax >= 0);
        assertTrue("On quantification/conditional, hadd should be >= hmax", hadd >= hmax);

        int planSize = pu.getPlanSize(dom, prob, "hadd");
        System.out.println("[H1][QuantCond] planSize=" + planSize);
        assertEquals("On Quantification/Conditional example, expected optimal plan size = 1", 1, planSize);
    }

    @Test
    @Ignore
    public void testH1SETUP5() throws Exception {
        PlannerUtils pu = new PlannerUtils();
        String dom = "unit_test_instances/depotsbig/domain.pddl";
        String prob = "unit_test_instances/depotsbig/sample.pddl";

        int hadd = pu.heuristicEstimate(dom, prob, "hadd");
        int hmax = pu.heuristicEstimate(dom, prob, "hmax");
        System.out.println("[H1][DepotsBig] hadd=" + hadd + ", hmax=" + hmax);
        assertEquals("Su DepotsBig ci si attende hadd = 27", 27, hadd);
        assertTrue("hmax deve essere non negativo", hmax >= 0);
        assertTrue("In generale, su stessa istanza, vale hadd >= hmax", hadd >= hmax);

        int planSize = pu.getPlanSize(dom, prob, "hadd");
        System.out.println("[H1][DepotsBig] planSize=" + planSize);
        assertEquals("Su DepotsBig, attesa una dimensione ottimale del piano", 26, planSize);
    }

    @Test
    public void testH1SETUP6() throws Exception {
        PlannerUtils pu = new PlannerUtils();
        String dom = "unit_test_instances/zenotravel/domain.pddl";
        String prob = "unit_test_instances/zenotravel/sample.pddl";

        int hadd = pu.heuristicEstimate(dom, prob, "hadd");
        int hmax = pu.heuristicEstimate(dom, prob, "hmax");
        System.out.println("[H1][ZenoTravel] hadd=" + hadd + ", hmax=" + hmax);
        assertTrue("hadd deve essere non negativo", hadd >= 0);
        assertTrue("hmax deve essere non negativo", hmax >= 0);
        assertTrue("Su ZenoTravel, atteso hadd >= hmax", hadd >= hmax);

        int planSize = pu.getPlanSize(dom, prob, "hadd");
        System.out.println("[H1][ZenoTravel] planSize=" + planSize);
        assertEquals("Su ZenoTravel, attesa una dimensione ottimale del piano", 9, planSize);
    }

    @Test
    @Ignore
    public void testH1SETUP7() throws Exception {
        PlannerUtils pu = new PlannerUtils();
        String dom = "unit_test_instances/settlers/domain.pddl";
        String prob = "unit_test_instances/settlers/sample.pddl";

        int hadd = pu.heuristicEstimate(dom, prob, "hadd");
        int hmax = pu.heuristicEstimate(dom, prob, "hmax");
        System.out.println("[H1][Settlers] hadd=" + hadd + ", hmax=" + hmax);
        assertTrue("hadd deve essere non negativo", hadd >= 0);
        assertTrue("hmax deve essere non negativo", hmax >= 0);
        assertTrue("Su Settlers, atteso hadd >= hmax", hadd >= hmax);
    }

    @Test
    public void testH1SETUP8() throws Exception {
        PlannerUtils pu = new PlannerUtils();
        String dom = "unit_test_instances/complex_expressions/domain.pddl";
        String prob = "unit_test_instances/complex_expressions/sample.pddl";

        int hadd = pu.heuristicEstimate(dom, prob, "hadd");
        int hmax = pu.heuristicEstimate(dom, prob, "hmax");
        System.out.println("[H1][ComplexExpr] hadd=" + hadd + ", hmax=" + hmax);
        assertTrue("hadd deve essere non negativo anche con espressioni complesse", hadd >= 0);
        assertTrue("hmax deve essere non negativo anche con espressioni complesse", hmax >= 0);
        assertTrue("In generale, su stessa istanza, vale hadd >= hmax", hadd >= hmax);
    }

    @Test
    public void testH1SETUP9() throws Exception {
        PlannerUtils pu = new PlannerUtils();
        String dom = "unit_test_instances/trigonometric_functions/domain.pddl";
        String prob = "unit_test_instances/trigonometric_functions/sample.pddl";

        int hadd = pu.heuristicEstimate(dom, prob, "hadd");
        int hmax = pu.heuristicEstimate(dom, prob, "hmax");
        System.out.println("[H1][TrigFunctions] hadd=" + hadd + ", hmax=" + hmax);
        assertTrue("hadd deve essere non negativo", hadd >= 0);
        assertTrue("hmax deve essere non negativo", hmax >= 0);
        assertTrue("Atteso hadd >= hmax", hadd >= hmax);

        int planSize = pu.getPlanSize(dom, prob, "hadd");
        System.out.println("[H1][TrigFunctions] planSize=" + planSize);
        assertEquals("Su TrigonometricFunctions, attesa una dimensione ottimale del piano", 13, planSize);
    }

    /**
     * Test su domini custom
     */
    @Test
    public void testIF1() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if1/domain.pddl",
                "unit_test_instances/if/h1_if1/problem.pddl",
                true, 1);
    }

    @Test
    public void testIF2() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if2/domain.pddl",
                "unit_test_instances/if/h1_if2/problem.pddl",
                true, 2);
    }

    @Test
    public void testIF3() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if3/domain.pddl",
                "unit_test_instances/if/h1_if3/problem.pddl",
                true, 1);
    }

    @Test
    public void testIF4() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if4/domain.pddl",
                "unit_test_instances/if/h1_if4/problem.pddl",
                true, 1);
    }

    @Test
    public void testIF5() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if5/domain.pddl",
                "unit_test_instances/if/h1_if5/problem.pddl",
                true, 1);
    }

    @Test
    public void testIF6() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if6/domain.pddl",
                "unit_test_instances/if/h1_if6/problem.pddl",
                true, 1);
    }

    @Test
    public void testIF7() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if7/domain.pddl",
                "unit_test_instances/if/h1_if7/problem.pddl",
                true, 7);
    }

    @Test
    public void testIF8() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if8/domain.pddl",
                "unit_test_instances/if/h1_if8/problem.pddl",
                true, 0);
    }

    @Test
    public void testIF9() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if9/domain.pddl",
                "unit_test_instances/if/h1_if9/problem.pddl",
                true, 0);
    }

    @Test
    public void testIF10() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if10/domain.pddl",
                "unit_test_instances/if/h1_if10/problem.pddl",
                true, 1);
    }

    @Test
    public void testIF11() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if11/domain.pddl",
                "unit_test_instances/if/h1_if11/problem.pddl",
                true, 2);
    }

    @Test
    public void testIF12() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if12/domain.pddl",
                "unit_test_instances/if/h1_if12/problem.pddl",
                true, 1);
    }

    @Test
    public void testIF13() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if13/domain.pddl",
                "unit_test_instances/if/h1_if13/problem.pddl",
                true, 0);
    }

    @Test
    public void testIF14() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if14/domain.pddl",
                "unit_test_instances/if/h1_if14/problem.pddl",
                true, 1);
    }

    @Test
    public void testIF15() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if15/domain.pddl",
                "unit_test_instances/if/h1_if15/problem.pddl",
                false, 0);
    }

    @Test
    public void testIF16() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if16/domain.pddl",
                "unit_test_instances/if/h1_if16/problem.pddl",
                true, 1);
    }

    @Test
    public void testIF17() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if17/domain.pddl",
                "unit_test_instances/if/h1_if17/problem.pddl",
                true, 1);
    }

    @Test
    public void testIF18() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_if18/domain.pddl",
                "unit_test_instances/if/h1_if18/problem.pddl",
                true, 2);
    }

    @Test
    public void testNONIF1() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if1/domain.pddl",
                "unit_test_instances/if/h1_non_if1/problem.pddl",
                false, 0);
    }

    @Test
    public void testNONIF2() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if2/domain.pddl",
                "unit_test_instances/if/h1_non_if2/problem.pddl",
                false, 0);
    }

    @Test
    public void testNONIF3() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if3/domain.pddl",
                "unit_test_instances/if/h1_non_if3/problem.pddl",
                false, 0);
    }

    @Test
    public void testNONIF4() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if4/domain.pddl",
                "unit_test_instances/if/h1_non_if4/problem.pddl",
                false, 0);
    }

    @Test
    public void testNONIF5() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if5/domain.pddl",
                "unit_test_instances/if/h1_non_if5/problem.pddl",
                false, 0);
    }

    @Test
    public void testNONIF6() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if6/domain.pddl",
                "unit_test_instances/if/h1_non_if6/problem.pddl",
                false, 0);
    }

    @Test
    public void testNONIF7() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if7/domain.pddl",
                "unit_test_instances/if/h1_non_if7/problem.pddl",
                false, 2);
    }

    @Test
    public void testNONIF8() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if8/domain.pddl",
                "unit_test_instances/if/h1_non_if8/problem.pddl",
                false, 3);
    }

    @Test
    public void testNONIF9() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if9/domain.pddl",
                "unit_test_instances/if/h1_non_if9/problem.pddl",
                false, 0);
    }

    @Test
    public void testNONIF10() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if10/domain.pddl",
                "unit_test_instances/if/h1_non_if10/problem.pddl",
                false, 7);
    }

    @Test
    public void testNONIF11() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if11/domain.pddl",
                "unit_test_instances/if/h1_non_if11/problem.pddl",
                false, 2);
    }

    @Test
    public void testNONIF12() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if12/domain.pddl",
                "unit_test_instances/if/h1_non_if12/problem.pddl",
                false, 0);
    }

    @Test
    public void testNONIF13() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if13/domain.pddl",
                "unit_test_instances/if/h1_non_if13/problem.pddl",
                false, 0);
    }

    @Test
    public void testNONIF14() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if14/domain.pddl",
                "unit_test_instances/if/h1_non_if14/problem.pddl",
                false, 0);
    }

    @Test
    public void testNONIF15() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if15/domain.pddl",
                "unit_test_instances/if/h1_non_if15/problem.pddl",
                false, 0);
    }

    @Test
    public void testNONIF16() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if16/domain.pddl",
                "unit_test_instances/if/h1_non_if16/problem.pddl",
                false, 0);
    }

    @Test
    public void testNONIF17() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if17/domain.pddl",
                "unit_test_instances/if/h1_non_if17/problem.pddl",
                false, 0);
    }

    @Test
    public void testNONIF18() throws Exception {
        assertIF(
                "unit_test_instances/if/h1_non_if18/domain.pddl",
                "unit_test_instances/if/h1_non_if18/problem.pddl",
                false, 1);
    }
}