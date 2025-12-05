package sequential_problems;

import com.hstairs.ppmajal.extraUtils.PlannerUtils;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class H1InterferenceFreeTest {

    // ------------------------ Helpers ------------------------
    private void assertInterferenceFree(String dom, String prob, boolean expectedIF) throws Exception {
        PlannerUtils pu = new PlannerUtils();
        boolean isIF = pu.isInterferenceFree(dom, prob);
        String msg = String.format("[IF-Check] %s | expected=%s, actual=%s", dom, expectedIF, isIF);
        System.out.println(msg);
        if (expectedIF) {
            assertTrue("Expected domain to be Interference-Free: " + dom, isIF);
        } else {
            assertFalse("Expected domain to be Non-Interference-Free: " + dom, isIF);
        }
    }

    /**
     * Verifica il comportamento delle euristiche hadd e hmax su un dominio
     * specificamente progettato per essere Interference-Free.
     * In un dominio IF, teoricamente hadd dovrebbe eguagliare hmax.
     * Vengono anche verificate le stime e la dimensione del piano ottimale.
     */
    @Test
    public void testH1OnIFDomain() throws Exception {
        PlannerUtils pu = new PlannerUtils();
        String dom = "unit_test_instances/h1_if/domain.pddl";
        String prob = "unit_test_instances/h1_if/problem.pddl";

        int hadd = pu.heuristicEstimate(dom, prob, "hadd");
        int hmax = pu.heuristicEstimate(dom, prob, "hmax");
        System.out.println("[H1][IF] hadd=" + hadd + ", hmax=" + hmax);
        assertEquals("On IF example, expected hadd = 3", 3, hadd);
        assertEquals("On IF example, expected hmax = 3", 3, hmax);
        assertEquals("On interference-free example, hadd should match hmax", hadd, hmax);

        int planSize = pu.getPlanSize(dom, prob, "hadd");
        System.out.println("[H1][IF] planSize=" + planSize);
        assertEquals("On IF example, expected optimal plan size = 3", 3, planSize);
    }

    /**
     * Verifica il comportamento delle euristiche hadd e hmax su un dominio
     * che non è Interference-Free
     * In questi domini, si verifica la relazione standard hadd >= hmax.
     * Vengono anche verificate le stime e la dimensione del piano ottimale.
     */
    @Test
    public void testH1OnNonIFDomain() throws Exception {
        PlannerUtils pu = new PlannerUtils();
        String dom = "unit_test_instances/h1_non_if/domain.pddl";
        String prob = "unit_test_instances/h1_non_if/problem.pddl";

        int hadd = pu.heuristicEstimate(dom, prob, "hadd");
        int hmax = pu.heuristicEstimate(dom, prob, "hmax");
        System.out.println("[H1][NonIF] hadd=" + hadd + ", hmax=" + hmax);
        assertEquals("On non-IF example, expected hadd = 3", 3, hadd);
        assertEquals("On non-IF example, expected hmax = 3", 3, hmax);
        assertTrue("On non-interference-free example, hadd should be >= hmax", hadd >= hmax);

        int planSize = pu.getPlanSize(dom, prob, "hadd");
        System.out.println("[H1][NonIF] planSize=" + planSize);
        assertEquals("On non-IF example, expected optimal plan size = 3", 3, planSize);
    }

    /**
     * Test di regressione su car_linear_mt_sc)
     * Verifica la relazione hadd >= hmax e la correttezza del calcolo hrmax.
     * Controlla la dimensione del piano ottimale.
     */
    @Test
    public void testH1OnProcessesDomain() throws Exception {
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

    /**
     * Test di regressione sul benchmark classico Gripper.
     * Verifica la relazione hadd >= hmax e la dimensione del piano ottimale.
     */
    @Test
    public void testH1OnGripper() throws Exception {
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

    /**
     * Test di regressione sul benchmark classico Blocks World.
     * Verifica la relazione hadd >= hmax e la dimensione del piano ottimale.
     */
    @Test
    public void testH1OnBlocks() throws Exception {
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

    /**
     * Test di regressione su un dominio che fa uso di quantificazione e condizioni.
     * Verifica la relazione hadd >= hmax e la dimensione del piano ottimale.
     */
    @Test
    public void testH1OnQuantificationConditional() throws Exception {
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

    /**
     * Test di regressione su una grande istanza del benchmark Depots.
     * Verifica la relazione hadd >= hmax e la dimensione del piano ottimale.
     */
    @Test
    @Ignore
    public void testH1OnDepotsBig() throws Exception {
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

    /**
     * Test di regressione sul benchmark ZenoTravel per le euristiche.
     * Verifica la relazione hadd >= hmax e la dimensione del piano ottimale.
     */
    @Test
    public void testH1OnZenoTravelHeuristics() throws Exception {
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

    /**
     * Test di regressione su un'istanza del benchmark Settlers.
     * Verifica la relazione hadd >= hmax.
     */
    @Test
    @Ignore
    public void testH1OnSettlers() throws Exception {
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

    /**
     * Test di regressione su un dominio con espressioni numeriche complesse.
     * Verifica la correttezza del calcolo delle euristiche hadd e hmax (incluso hadd >= hmax)
     * in presenza di funzionalità PDDL avanzate.
     */
    @Test
    public void testH1OnComplexExpressions() throws Exception {
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

    /**
     * Test di regressione su un dominio che include funzioni trigonometriche.
     * Verifica la correttezza del calcolo delle euristiche hadd e hmax (incluso hadd >= hmax)
     * e la dimensione del piano ottimale.
     */
    @Test
    public void testH1OnTrigonometricFunctions() throws Exception {
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

    // ------------------------ Interference-Free checks (split per domain) ------------------------
    @Test
    public void testIF_h1_if() throws Exception {
        assertInterferenceFree("unit_test_instances/h1_if/domain.pddl", "unit_test_instances/h1_if/problem.pddl", true);
    }

    @Test
    public void testNonIF_h1_non_if() throws Exception {
        assertInterferenceFree("unit_test_instances/h1_non_if/domain.pddl", "unit_test_instances/h1_non_if/problem.pddl", false);
    }

    @Test
    public void testNonIF_Processes() throws Exception {
        assertInterferenceFree("unit_test_instances/car_linear_mt_sc/domain.pddl", "unit_test_instances/car_linear_mt_sc/sample.pddl", false);
    }

    @Test
    public void testIF_Gripper_Propositional() throws Exception {
        assertInterferenceFree("unit_test_instances/gripper/domain.pddl", "unit_test_instances/gripper/prob02.pddl", true);
    }

    @Test
    public void testIF_Blocks_Propositional() throws Exception {
        assertInterferenceFree("unit_test_instances/blocks/domain.pddl", "unit_test_instances/blocks/task01.pddl", true);
    }

    @Test
    public void testIF_h1_if_prop() throws Exception {
        assertInterferenceFree("unit_test_instances/h1_if_prop/domain.pddl", "unit_test_instances/h1_if_prop/problem.pddl", true);
    }

    @Test
    public void testIF_h1_non_if_prop_due_to_numeric_only() throws Exception {
        assertInterferenceFree("unit_test_instances/h1_non_if_prop/domain.pddl", "unit_test_instances/h1_non_if_prop/problem.pddl", true);
    }

    @Test
    public void testNonIF_h1_numeric_non_if_decrease() throws Exception {
        assertInterferenceFree("unit_test_instances/h1_numeric_non_if_decrease/domain.pddl", "unit_test_instances/h1_numeric_non_if_decrease/problem.pddl", false);
    }

    @Test
    public void testIF_h1_if_non_trivial() throws Exception {
        assertInterferenceFree("unit_test_instances/h1_if_non_trivial/domain.pddl", "unit_test_instances/h1_if_non_trivial/problem.pddl", true);
    }

    @Test
    public void testIF_prop_only_case2() throws Exception {
        assertInterferenceFree("unit_test_instances/h1_if_prop_only2/domain.pddl", "unit_test_instances/h1_if_prop_only2/problem.pddl", true);
    }

    @Test
    public void testIF_goal_numeric_coachievers_no_pre() throws Exception {
        assertInterferenceFree("unit_test_instances/h1_if_goal_numeric_coachievers_no_pre/domain.pddl", "unit_test_instances/h1_if_goal_numeric_coachievers_no_pre/problem.pddl", true);
    }

    @Test
    public void testNonIF_numeric_pre_mismatch() throws Exception {
        assertInterferenceFree("unit_test_instances/h1_non_if_numeric_pre_mismatch/domain.pddl", "unit_test_instances/h1_non_if_numeric_pre_mismatch/problem.pddl", false);
    }

    @Test
    public void testNonIF_numeric_decrease_strict2() throws Exception {
        assertInterferenceFree("unit_test_instances/h1_non_if_numeric_decrease_strict2/domain.pddl", "unit_test_instances/h1_non_if_numeric_decrease_strict2/problem.pddl", false);
    }

    @Test
    public void testIF_numeric_implication_holds() throws Exception {
        assertInterferenceFree("unit_test_instances/h1_if_numeric_implication_holds/domain.pddl", "unit_test_instances/h1_if_numeric_implication_holds/problem.pddl", true);
    }
}