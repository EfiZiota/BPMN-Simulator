package simulation_gui;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;

public class ExcelParser extends BPMNFileAdder{
	

	private static int number=1;
	private int [] bpmnColumn = {0,1,2,3,5,6,7,9,10,11,12};

    
	public ExcelParser(String afilePath) {
		super(afilePath);
	}

	  public enum FileStatus {
	        FILE_EXISTS,
	        FILE_CREATED,
	        ERROR
	    }
	

	  //Συνάρτηση που επιστρέφει την κατάσταση του αρχείου excel
	public FileStatus checkAndCreateExcelFile() {
		
        try {
           
        	//Διαβάζει το αρχείο bpmn
            File bpmnFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(bpmnFile);

            
            //Δημιουργία μονοπατιού excel με το ίδιο όνομα με το αρχείο bpmn
            String fileNameWithoutExtension = bpmnFile.getName().replaceFirst("[.][^.]+$", "");
            String excelFileName = bpmnFile.getParent() + File.separator + fileNameWithoutExtension + ".xlsx";
            Path excelFilePath = Paths.get(excelFileName);
            
            // Έλεγχος ύπαρξης αρχείου
            if (Files.exists(excelFilePath)) {
            	
            	//Αν το αρχείο υπάρχει, ενημερώνεται ο χρήστης
                JOptionPane.showMessageDialog(null, "The file already exists at: " + excelFileName, "File Exists", JOptionPane.INFORMATION_MESSAGE);
                
                //Επιστρέφεται στην κλάση BPMNFileAdder η πληροφορία ότι το αρχείο υπάρχει
                return FileStatus.FILE_EXISTS;
                
            } else {
            	
            	//Αν το αρχείο δεν υπάρχει, δημιουργείται 
                Config.setExcelFilePath(excelFileName);

                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet();

                //Καθαρισμός των περιεχομένων των φύλλων
                clearSheetContents(sheet);
                
                //Συνάρτηση στην οποία γίνεται η μεταφορά των στοιχείων του bpmn αρχείου στο excel
                parseAndWriteToExcel(doc, sheet, workbook);
       	

                //Αποθήκευση του αρχείου excel με τα νέα δεδομένα
                try (FileOutputStream fileOut = new FileOutputStream(Config.getExcelFilePath())) {
                    workbook.write(fileOut);
                    workbook.close();
                    
                    //Ενημέρωση του χρήστη για τη δημιουργία του αρχείου
                    JOptionPane.showMessageDialog(null, "Excel file created: " + Config.getExcelFilePath());
                    
                    //Επιστροφή στην κλάση BPMNFileAdder η πληροφορία ότι το αρχείο υπάρχει
                    return FileStatus.FILE_CREATED;
                    
                } catch (IOException ex) {
                	//Ενημέρωση του χρήστη στη περίπτωση κάποιου σφάλματος στη δημιυοργία του αρχείου
                    JOptionPane.showMessageDialog(null, "Error creating Excel file: " + ex.getMessage());
                    
                    //Επιστροφή στην κλάση BPMNFileAdder η πληροφορία ότι το αρχείο δεν δημιουργήθηκε λόγω σφάλματος
                    return FileStatus.ERROR;
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error processing file: " + e.getMessage());
            return FileStatus.ERROR;
        }
        
   }



	
	private void clearSheetContents(Sheet sheet) {
		
		// Έλεγχος όλων των σειρών του αρχείου excel, ξεκινώντας από τη δεύτερη σειρά (γιατί στη πρώτη μπαίνουν οι τίτλοι)
		//με σκοπό να τεθεί η κενή τιμή σε όλα
	    for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
	        Row row = sheet.getRow(rowIndex);
	        if (row != null) {
	            for (Cell cell : row) {
	                cell.setCellValue("");
	            }
	        }
	    }
	    
	    
	}
	
   

	
	private void parseAndWriteToExcel(Document doc, Sheet sheet, Workbook workbook) {
		
		// Δημιουργώ ένα στυλ ώστε να τίθεται ως κείμενο το κέλι στο οποίο εφαρμόζεται
        CellStyle textStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        textStyle.setDataFormat(format.getFormat("@")); 
        
        //Μορφοποίηση του στυλ του αρχείου
		CustomizeExcel(sheet,workbook);
        
        
		ParseBpmnData mv = new ParseBpmnData(filePath, "");
		List<BPMNElement> ordertasks = mv.getTasksInOrder();
		
		int activityRow=0,gatewayRow=0,intermediateRow=0;

		for (BPMNElement element : ordertasks) {
			
			//Αν το στοιχείο της λίστα είναι τύπου participant 
			if(element instanceof Participant) {
				
				Participant participant = (Participant) element;
				
				// Στη πρώτη διαθέσιμη σειρά, η οποία αν δεν υπάρχει δημιουργείται
                Row row = sheet.getRow(activityRow+1);
               
                if (row == null) {
                    row = sheet.createRow(1+activityRow);        
                }
                
                activityRow++;
                
                //Στα κελιά της στήλης 0 και 1, τα οποία δημιουργούνται αν δεν υπάρχουν
                Cell cell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                Cell taskcell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                
             
                //Δημιουργία ενός στυλ για τα συγκεκριμένα κελιά
                CellStyle style = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                font.setBold(true); //Θέτουμε Bold το κείμενο
                font.setFontHeightInPoints((short) 15); // Μέγεθος γραμματοσειράς 15
                style.setFont(font); 

                style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex()); //Χρώμα κελιού ανοιχτό πορτοκαλί
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                
                // Εφαρμογή του στυλ που δημιουργήθηκε στα αντίστοιχα κελιά
                taskcell.setCellStyle(style); 
                cell.setCellStyle(style);
                
                // Αν το κελί είναι άδειο
                if (cell.getCellType() == CellType.BLANK || cell.getStringCellValue().isEmpty()) {
                	
                    // Θέτουμε ως τιμή στο αντίστοιχο κελί της πρώτης στήλης το id του participant
                    cell.setCellValue(participant.getParticipantId());
                } 
                else {
                	
                	//αν το κελί δεν είναι αδείο πρώτα του αδείαζω τη τιμή και μετά θέτω το id
                	cell.setCellValue("");
                	cell.setCellValue(participant.getParticipantId());
                 }
                
                // Αν το κελί δεν είναι άδειο
                if (taskcell.getCellType() == CellType.BLANK || taskcell.getStringCellValue().isEmpty()) {
                	
                	// Θέτουμε ως τιμή στο αντίστοιχο κελί της δεύτερης στήλης το όνομα του participant
                	taskcell.setCellValue(participant.getParticipantName());
                } 
                else {
                	
                 	//αν το κελί δεν είναι αδείο πρώτα του αδείαζω τη τιμή και μετά θέτω το όνομα
                	taskcell.setCellValue("");
                	taskcell.setCellValue(participant.getParticipantName());
                 }
			}
			
			//Αν το στοιχείο της λίστα είναι τύπου BpmnTask 
			if (element instanceof BpmnTask) { 
				
	            BpmnTask task = (BpmnTask) element;
	            //String taskId = task.getTaskId();
	            
	            	//Αν πρόκειται για activity
	            	if(task.getTaskId().startsWith("Activity")) {
	            		
	    		    	//Στην επόμενη διαθέσιμη σειρά
	                    Row row = sheet.getRow(activityRow+1);
	                   
	                    if (row == null) {
	                        row = sheet.createRow(1+activityRow);        
	                    }
	                    
	                    activityRow++;
	                    
	                  //Στα κελιά της στήλης 0,1,2 και 3, τα οποία δημιουργούνται αν δεν υπάρχουν
	                    Cell IDcell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                    Cell cell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                    Cell cellduration = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                    Cell cellcost = row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                    
	                    // Εφαρμογή του στυλ που δημιουργήθηκε στην αρχή της συνάρτησης στα  κελιά των παραπάνω στηλών
	                    cellduration.setCellStyle(textStyle);
	                    cellcost.setCellStyle(textStyle);
	                    IDcell.setCellStyle(textStyle);
	                    cell.setCellStyle(textStyle);
	                    
	                
	                    // Αν το κελί είναι άδειο
	                    if (IDcell.getCellType() == CellType.BLANK || IDcell.getStringCellValue().isEmpty()) {
	                        // Θέτουμε στο κελί της στήλης 0 το id του activity
	                    	IDcell.setCellValue(task.getTaskId());
	                    } 
	                    else {
	                     	//αν το κελί δεν είναι αδείο πρώτα του αδείαζω τη τιμή και μετά θέτω το id
	                    	IDcell.setCellValue("");
	                    	IDcell.setCellValue(task.getTaskId());
	                          
	                     }
	                    
	                    
	                    
	                   // Αν το κελί είναι άδειο
	                    if (cell.getCellType() == CellType.BLANK || cell.getStringCellValue().isEmpty()) {
	                        // Θέτουμε στο κελί της στήλης 1 το όνομα του activity
	                    	cell.setCellValue(task.getTaskName());
	                    } 
	                    else {
	                     	//αν το κελί δεν είναι αδείο πρώτα του αδείαζω τη τιμή και μετά θέτω το όνομα
	                    	cell.setCellValue("");
	                    	cell.setCellValue(task.getTaskName());
	                          
	                     }
	                    
	    		    }
	    		    else if(task.getTaskId().startsWith("Gateway") && task.getTaskName()!= null) { //αν πρόκειται για gateway
	    		    	
	    		    	// Στη πρώτη διαθέσιμη σειρά, η οποία αν δεν υπάρχει δημιουργείται
	                    Row row = sheet.getRow(gatewayRow+1);
	                   
	                    if (row == null) {
	                        row = sheet.createRow(1+gatewayRow);        
	                    }
	                    
	                    gatewayRow++;
	                    
	                  //Στα κελιά της στήλης 5,6 και 7, τα οποία δημιουργούνται αν δεν υπάρχουν
	                    Cell IDcell = row.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                    Cell cell = row.getCell(6, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                    Cell cellrate = row.getCell(7, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                    
	                    // Εφαρμογή του στυλ που δημιουργήθηκε στην αρχή της συνάρτησης στα κελιά της στήλης 7
	                    cellrate.setCellStyle(textStyle);
	                    
	                    
	                    //Δημιουργία ενός στυλ για τα συγκεκριμένα κελιά
	                    CellStyle style = workbook.createCellStyle();
	                    org.apache.poi.ss.usermodel.Font font = workbook.createFont();
	                    font.setBold(true); //Θέτουμε Bold το κείμενο
	                    style.setFont(font);
	                    style.setFillForegroundColor(IndexedColors.TURQUOISE.getIndex()); //Χρώμα κελιού τιρκουάζ
	                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	                    
	                    //Εφαρμογή του στυλ που δημιουργήθηκε στα κελιά της στήλης 6
	                    cell.setCellStyle(style);
	                    
	                    
	                    // Αν το κελί είναι άδειο
	                    if (IDcell.getCellType() == CellType.BLANK || IDcell.getStringCellValue().isEmpty()) {
	                        // Θέτουμε στο κελί της στήλης 5 το id του activity
	                    	IDcell.setCellValue(task.getTaskId());
	                    } else {
	                    	//αν το κελί δεν είναι αδείο πρώτα του αδείαζω τη τιμή και μετά θέτω το id
	                    	IDcell.setCellValue("");
	                    	IDcell.setCellValue(task.getTaskId());
	                        
	                    }
	                 
	                    // Αν το κελί είναι άδειο
	                    if (cell.getCellType() == CellType.BLANK || cell.getStringCellValue().isEmpty()) {
	                        // Θέτουμε στο κελί της στήλης 6 το όνομα του activity
	                    	 cell.setCellValue(task.getTaskName());
	                    } else {
	                    	//αν το κελί δεν είναι αδείο πρώτα του αδείαζω τη τιμή και μετά θέτω το όνομα
	                    	cell.setCellValue("");
	                    	cell.setCellValue(task.getTaskName());
	                        
	                    }
	                 
	                    
	    		    }
	    		    else if(task.getTaskTag().startsWith("intermediate") && task.getlinkEventDefinitionTags().isEmpty()) {//Αν πρόκειται για intermediate event
	    		    	
	    		    	// Στη πρώτη διαθέσιμη σειρά, η οποία αν δεν υπάρχει δημιουργείται
	                    Row row = sheet.getRow(intermediateRow+1);
	                   
	                    if (row == null) {
	                        row = sheet.createRow(1+intermediateRow);        
	                    }
	                    
	                    intermediateRow++;
	                    
	                    //Στα κελιά της στήλης 9,10,11 και 12, τα οποία δημιουργούνται αν δεν υπάρχουν
	                    Cell IDcell = row.getCell(9, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                    Cell cell = row.getCell(10, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                    Cell celldurationIE = row.getCell(11, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                    Cell cellcostIE = row.getCell(12, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                    
	                    // Εφαρμογή του στυλ που δημιουργήθηκε στην αρχή της συνάρτησης στα κελιά της στήλης 11 & 12
	                    celldurationIE.setCellStyle(textStyle);
	                    cellcostIE.setCellStyle(textStyle);
	                    
	                    //Δημιουργία ενός στυλ για τα κελιά
	                    CellStyle style = workbook.createCellStyle();
	                    org.apache.poi.ss.usermodel.Font font = workbook.createFont();
	                    font.setItalic(true); //Θέτουμε Italic το κείμενο
	                    style.setFont(font);

	                    
	                    //Εφαρμογή του στυλ που δημιουργήθηκε στα κελιά της στήλης 10
	                    cell.setCellStyle(style);
	                    
	                
	                    // Αν το κελί είναι άδειο
	                    if (IDcell.getCellType() == CellType.BLANK || IDcell.getStringCellValue().isEmpty()) {
	                        // Θέτουμε στο κελί της στήλης 9 το id του activity
	                    	IDcell.setCellValue(task.getTaskId());
	                    } else {
	                    	//αν το κελί δεν είναι αδείο πρώτα του αδείαζω τη τιμή και μετά θέτω το id
	                    	IDcell.setCellValue("");
	                    	IDcell.setCellValue(task.getTaskId());
	                        
	                    }
	                    
	                    // Αν το κελί είναι άδειο
	                    if (cell.getCellType() == CellType.BLANK || cell.getStringCellValue().isEmpty()) {
	                        // Θέτουμε στο κελί της στήλης 10 το όνομα του activity
	                    	 cell.setCellValue(task.getTaskName());
	                    } else {
	                    	//αν το κελί δεν είναι αδείο πρώτα του αδείαζω τη τιμή και μετά θέτω το όνομα
	                    	cell.setCellValue("");
	                    	cell.setCellValue(task.getTaskName());
	                        
	                    }
	                   
	    		    }
	    		}
			else if(element instanceof SequenceFlow) { //Αν το στοιχείο της λίστα είναι τύπου SequenceFlow
				
				
				SequenceFlow flow = (SequenceFlow) element; 
				
	           // String flowId = flow.getFlowId();
	            //if (taskId != null) { // Ensure taskId is not null before using it
	            	
				// Στη πρώτη διαθέσιμη σειρά, η οποία αν δεν υπάρχει δημιουργείται
                Row row = sheet.getRow(gatewayRow+1);
               
                if (row == null) {
                    row = sheet.createRow(1+gatewayRow);        
                }
                
                gatewayRow++;
                
                //Στα κελιά της στήλης 5,6 και 7, τα οποία δημιουργούνται αν δεν υπάρχουν
                Cell IDcell = row.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                Cell cell = row.getCell(6, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                Cell cellrate = row.getCell(7, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
               
                
                //Εφαρμογή του στυλ που δημιουργήθηκε στην αρχή της συνάρτησης στα κελιά της στήλης 7
                cellrate.setCellStyle(textStyle);
                
                
               
                
              //Δημιουργία ενός στυλ για τα κελιά
                CellStyle style = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                font.setItalic(true); //Θέτουμε Italic το κείμενο
                style.setFont(font);
                style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex()); //Χρώμα κελιού ανοιχτό τιρκουάζ
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                
              //Εφαρμογή του στυλ που δημιουργήθηκε στα κελιά της στήλης 6
                cell.setCellStyle(style);
           
             // Αν το κελί είναι άδειο
                if (IDcell.getCellType() == CellType.BLANK || IDcell.getStringCellValue().isEmpty()) {
                    // Θέτουμε στο κελί της στήλης 5 το id του activity
                	IDcell.setCellValue(flow.getFlowId());
                } else {
                	//αν το κελί δεν είναι αδείο πρώτα του αδείαζω τη τιμή και μετά θέτω το id
                	IDcell.setCellValue("");
                	IDcell.setCellValue(flow.getFlowId());
                    
                }
                
                // Αν το κελί είναι άδειο
                if (cell.getCellType() == CellType.BLANK || cell.getStringCellValue().isEmpty()) {
                    // Θέτουμε στο κελί της στήλης 6 το όνομα του activity
                	 cell.setCellValue(flow.getFlowName());
                } else {
                	//αν το κελί δεν είναι αδείο πρώτα του αδείαζω τη τιμή και μετά θέτω το όνομα
                	cell.setCellValue("");
                	cell.setCellValue(flow.getFlowName());
                    
                }
                	
                //Στην περίπτωση που τα flow δεν έχουν ονομαστεί από τον χρήστη και του έχει δωθεί η τιμή "Επιλογή"
                //Τότε τους δίνεται και ένας αριθμός με βάση το σύνολο των μονοπατιών του gateway
                if(flow.getFlowName().startsWith("Eπιλογή")) {
                	cell.setCellValue("Eπιλογή " + number);
                	number++;
                }
                
               
	          }

			//Εφαρμογή αυτομάτης προσαρμογής των στηλών στα κείμενα τους
			for(int position : bpmnColumn) {
				sheet.autoSizeColumn(position);
			}
			 
	   }
		
		    
	}



	private void CustomizeExcel(Sheet sheet, Workbook workbook) {
		
		 
		        for(int position : bpmnColumn) {
		        	
		        	// Στη πρώτη σειρά, η οποία αν δεν υπάρχει δημιουργείται
		            Row row1 = sheet.getRow(0);
		            
		            if (row1 == null) {
		                row1 = sheet.createRow(0);        
		            }
		            
		            //Στα αντίστοιχα κελιά των στηλών του πίνακα bpmnColumn
		            Cell cell1 = row1.getCell(position, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
		            
		            
		            //Δημιουργία ενός στύλ για τις επικεφαλίδες του αρχείου excel
		            CellStyle titleStyle = workbook.createCellStyle();
		            Font font = workbook.createFont();
		            font.setBold(true); //Θέτουμε Italic το κείμενο
		            font.setItalic(true); //Θέτουμε Italic το κείμενο
		            font.setFontHeightInPoints((short) 15); //Γραμματοσειρά νούμερο 15
		            titleStyle.setFont(font);

		            //Αναλόγως τη στήλη του Excel θέτουμε διαφορετικό τίτλο και χρωματισμό του κελιού
		            switch(position) {
			            case 0: 
			            	//Στην πρώτη στήλη που εμφανίζονται τα id των activities αναγράφεται ο τίτλος Activities Tasks ID
			            	//και χρησιμοποιείται ο χρωματισμός πορτοκαλί
			            	//παρομοίως λειτουργούν και τα υπόλοιπα
			            	cell1.setCellValue("Activities Tasks ID");
			            	 titleStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
				                titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			            	break;
			            case 1: 
			            	cell1.setCellValue("Activities Tasks");
			            	   titleStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
				                titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			               
			            	break;
			            case 2: 
			            	cell1.setCellValue("Durations");
			            	   titleStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
				                titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			            	break;
			            case 3: 
			            	cell1.setCellValue("Costs");
			            	   titleStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
				                titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			            	break;
			            case 5: 
			            	cell1.setCellValue("Gateways ID");
			            	 titleStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
				             titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			            	break;
			            case 6: 
			            	cell1.setCellValue("Gateways");
			            	 titleStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
				             titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			            	break;
			            case 7: 
			            	cell1.setCellValue("Rates");
			            	titleStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
				            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			            	break;
			            case 9: 
			            	cell1.setCellValue("Intermediate Events ID");
			            	   titleStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
				                titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			            	break;
			            case 10: 
			            	cell1.setCellValue("Intermediate Events");
			         	   titleStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
			                titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			            	break;
			            case 11: 
			            	cell1.setCellValue("Durations");
			         	   titleStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
			                titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			            	break;
			            case 12: 
			            	cell1.setCellValue("Costs");
			         	   titleStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
			                titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			            	break;
		            }
		            
		            
		            //Αναλόγως τι προστέθηκε στο στυλ titleStyle μέσω της switch, εφαρμόζεται το συνολικό στυλ στο αντίστοιχο κελί
		            cell1.setCellStyle(titleStyle);
		            
		            //Εφαρμογή αυτομάτης προσαρμογής των στηλών στα κείμενα τους
		            sheet.autoSizeColumn(position);
		        }
		        
	}
}
   
