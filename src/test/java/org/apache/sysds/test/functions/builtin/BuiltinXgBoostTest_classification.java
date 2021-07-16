package org.apache.sysds.test.functions.builtin;

import org.apache.sysds.common.Types;
import org.apache.sysds.lops.LopProperties;
import org.apache.sysds.runtime.matrix.data.MatrixBlock;
import org.apache.sysds.runtime.matrix.data.MatrixValue;
import org.apache.sysds.test.AutomatedTestBase;
import org.apache.sysds.test.TestConfiguration;
import org.apache.sysds.test.TestUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class BuiltinXgBoostTest_classification extends AutomatedTestBase {
    private final static String TEST_NAME = "xgboost_classification";
    private final static String TEST_DIR = "functions/builtin/";
    private static final String TEST_CLASS_DIR = TEST_DIR + BuiltinXgBoostTest_classification.class.getSimpleName() + "/";
    double eps = 1e-10;

    @Override
    public void setUp() {
        TestUtils.clearAssertionInformation();
        addTestConfiguration(TEST_NAME, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME, new String[]{"C"}));
    }

    @Parameterized.Parameter()
    public int rows;
    @Parameterized.Parameter(1)
    public int cols;
    @Parameterized.Parameter(2)
    public int sml_type;
    @Parameterized.Parameter(3)
    public int num_trees;
    @Parameterized.Parameter(4)
    public double learning_rate;
    @Parameterized.Parameter(5)
    public int max_depth;
    @Parameterized.Parameter(6)
    public double lambda;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {8, 2, 1, 2, 0.3, 6, 1.0},
        });
    }

    @Test
    public void testXgBoost() {
        executeXgBoost(Types.ExecMode.SINGLE_NODE);
    }

    private void executeXgBoost(Types.ExecMode mode) {
        Types.ExecMode platformOld = setExecMode(mode);
        try {
            loadTestConfiguration(getTestConfiguration(TEST_NAME));

            String HOME = SCRIPT_DIR + TEST_DIR;
            fullDMLScriptName = HOME + TEST_NAME + ".dml";
            programArgs = new String[]{"-args", input("X"), input("y"), input("R"), String.valueOf(sml_type),
                    String.valueOf(num_trees), output("M")};

            double[][] y = {
                    {1.0},
                    {1.0},
                    {0.0},
                    {1.0},
                    {1.0},
                    {0.0},
                    {1.0},
                    {0.0}};

            double[][] X = {
                    {12.0, 1.0},
                    {15.0, 0.0},
                    {24.0, 0.0},
                    {20.0, 1.0},
                    {25.0, 1.0},
                    {17.0, 0.0},
                    {16.0, 1.0},
                    {32.0, 1.0}};

            double[][] R = {
                    {1.0, 2.0}};

            writeInputMatrixWithMTD("X", X, true);
            writeInputMatrixWithMTD("y", y, true);
            writeInputMatrixWithMTD("R", R, true);

            runTest(true, false, null, -1);

            HashMap<MatrixValue.CellIndex, Double> actual_M = readDMLMatrixFromOutputDir("M");

            // root node of first tree
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(1,2)), 1.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(2,2)), 1.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(3,2)), 1.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(4,2)), 2.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(5,2)), 2.0, eps);
            TestUtils.compareScalars(String.valueOf(actual_M.get(new MatrixValue.CellIndex(6, 2))), "null");

            // random node of first tree
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(1,12)), 31.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(2,12)), 1.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(3,12)), 3.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(4,12)), 2.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(5,12)), 2.0, eps);
            TestUtils.compareScalars(String.valueOf(actual_M.get(new MatrixValue.CellIndex(6, 12))), "null");

            // random leaf node of first tree
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(1,15)), 62.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(2,15)), 1.0, eps);
            TestUtils.compareScalars(String.valueOf(actual_M.get(new MatrixValue.CellIndex(3, 15))), "null");
            TestUtils.compareScalars(String.valueOf(actual_M.get(new MatrixValue.CellIndex(4, 15))), "null");
            TestUtils.compareScalars(String.valueOf(actual_M.get(new MatrixValue.CellIndex(5, 15))), "null");
            TestUtils.compareScalars(String.valueOf(actual_M.get(new MatrixValue.CellIndex(6, 15))), "null");

            // root node of second tree
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(1,17)), 1.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(2,17)), 2.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(3,17)), 1.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(4,17)), 2.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(5,17)), 2.0, eps);
            TestUtils.compareScalars(String.valueOf(actual_M.get(new MatrixValue.CellIndex(6, 17))), "null");

            // random node of second tree
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(1,18)), 2.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(2,18)), 2.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(3,18)), 2.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(4,18)), 1.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(5,18)), 1.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(6,18)), 14.0, eps);

            //random leaf node of second tree
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(1,39)), 63.0, eps);
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(2,39)), 2.0, eps);
            TestUtils.compareScalars(String.valueOf(actual_M.get(new MatrixValue.CellIndex(3, 39))), "null");
            TestUtils.compareScalars(String.valueOf(actual_M.get(new MatrixValue.CellIndex(4, 39))), "null");
            TestUtils.compareScalars(String.valueOf(actual_M.get(new MatrixValue.CellIndex(5, 39))), "null");
            TestUtils.compareScalars(actual_M.get(new MatrixValue.CellIndex(6,39)), -0.6666666666666666, eps);


        } catch (Exception ex) {
            System.out.println("[ERROR] Xgboost test failed, cause: " + ex);
            throw ex;
        } finally {
            rtplatform = platformOld;
        }
    }
}