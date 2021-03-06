import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import service.MyService;

/**
 * The MyTest class<br>
 *
 * @author Zihao Long
 * @version 1.0, 2022-03-14 17:46
 * @since ExcelPDFReports 0.0.1
 */
public class MyTest {

    /**
     * The test case table
     * +------+-------+------------------------------------+------------------------+
     * |  ID  |  TCI  |               Inputs               |      Exp. Results      |
     * |      |       +--------------+---------------------+------------------------+
     * |      |       |   is folder  | is PDF for students |         output         |
     * +------+-------+--------------+---------------------+------------------------+
     * |  DT1 | Rule1 |       T      |          T          |    PDF for Students    |
     * +------+-------+--------------+---------------------+------------------------+
     * |  DT2 | Rule2 |       T      |          F          | PDF for Internal Check |
     * +------+-------+--------------+---------------------+------------------------+
     * |  DT3 | Rule3 |       F      |          T          |    PDF for Students    |
     * +------+-------+--------------+---------------------+------------------------+
     * |  DT4 | Rule4 |       F      |          F          | PDF for Internal Check |
     * +------+-------+--------------+---------------------+------------------------+
     * | DT5* |   -   |  empty file  |          F          |        Exception       |
     * +------+-------+--------------+---------------------+------------------------+
     * | DT6* |   -   | empty folder |          F          |        Exception       |
     * +------+-------+--------------+---------------------+------------------------+
     */


    /**
     * The regular data
     */
    private static Object[][] regularData = new Object[][]{
            // id, academicYear, excelFilePath, savingFolderPath, selectedStr
            {"DT1", "2022", "/Users/aihuishou/Desktop/test2/input_excel", "/Users/aihuishou/Desktop/test2/output_files/DT1", "Project Descriptions for Students"},
            {"DT2", "2022", "/Users/aihuishou/Desktop/test2/input_excel", "/Users/aihuishou/Desktop/test2/output_files/DT2", "Project Descriptions for Internal Check"},
            {"DT3", "2022", "/Users/aihuishou/Desktop/test2/input_excel/project.csv", "/Users/aihuishou/Desktop/test2/output_files/DT3", "Project Descriptions for Students"},
            {"DT4", "2022", "/Users/aihuishou/Desktop/test2/input_excel/project.csv", "/Users/aihuishou/Desktop/test2/output_files/DT4", "Project Descriptions for Internal Check"},
    };

    /**
     * The fault model data, the excelFilePath maps an empty file or folder
     *
     */
    private static Object[][] faultModelData = new Object[][]{
            // id, academicYear, excelFilePath, savingFolderPath, selectedStr
            {"DT5*", "2022", "/Users/aihuishou/Desktop/test2/input_excel/empty_data.csv", "/Users/aihuishou/Desktop/test2/output_files/DT5", "Project Descriptions for Internal Check"},
            {"DT6*", "2022", "/Users/aihuishou/Desktop/test2/empty_folder", "/Users/aihuishou/Desktop/test2/output_files/DT6", "Project Descriptions for Internal Check"},
    };

    @DataProvider(name = "regularData")
    public Object[][] getRegularData() {
        return regularData;
    }

    @DataProvider(name = "faultModelData")
    public Object[][] getFaultModelData() {
        return faultModelData;
    }

    /**
     * Regular test<br>
     *
     * @param [id, academicYear, excelFilePath, savingFolderPath, selectedStr]
     * @return void
     * @author Zihao Long
     */
    @Test(dataProvider = "regularData")
    public void regularTest(String id, String academicYear, String excelFilePath, String savingFolderPath, String selectedStr) throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        MyService.genBySelection(academicYear, excelFilePath, savingFolderPath, selectedStr);
    }

    /**
     * Fault model test<br>
     * expected exception: java.lang.RuntimeException: No data found in the Excel file.<br>
     *
     * @param [id, academicYear, excelFilePath, savingFolderPath, selectedStr]
     * @return void
     * @author Zihao Long
     */
    @Test(dataProvider = "faultModelData", expectedExceptions = RuntimeException.class)
    public void faultModelTest(String id, String academicYear, String excelFilePath, String savingFolderPath, String selectedStr) throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        MyService.genBySelection(academicYear, excelFilePath, savingFolderPath, selectedStr);
    }
}
