package simulation_gui;

public class Config {
	
	//Κλάση στην οποία αποθηκεύεται το μονοπάτι του excel που χρησιμοποιείται στη προσομοίωση
	
	   private static String excelFilePath = "default/path/to/your/excel/file.xlsx";

	    public static String getExcelFilePath() {
	        return excelFilePath;
	    }

	    public static void setExcelFilePath(String path) {
	        excelFilePath = path;
	    }
}