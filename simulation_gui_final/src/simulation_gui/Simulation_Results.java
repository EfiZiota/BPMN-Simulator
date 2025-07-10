package simulation_gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jfree.chart.ChartPanel;


public class Simulation_Results extends JFrame{
	
	private final String SPECIFIC_FILE_PATH = Config.getExcelFilePath();
	protected String selectedSheet, selectedParticipant;
	private JLabel staticLabel,staticLabel1;
	private JComboBox<String> sheetDropdown = new JComboBox<>();
	private JComboBox<String> participantDropdown = new JComboBox<>();
	private JTextField numberInput = new JTextField(10); 
    private JComboBox<String> timeUnitDropdown = new JComboBox<>(new String[]{"seconds", "minutes", "hours"});
    private ChartPanel chart;
	private Object piechart, table, queue;
	private Component tableactivities;
	
	public String getSelectedParticipant() {
		return selectedParticipant;
	}

	
	public Simulation_Results() {
		
		
		   try(FileInputStream fis = new FileInputStream(SPECIFIC_FILE_PATH);
	        		
	                Workbook workbook1 = WorkbookFactory.create(fis)) {
	       
			   
			        List<String> sheetNames = new ArrayList<>();
			        
			        //Μεταφορά των ονομάτων κάθε φύλλου του excel αρχείου που χρησιμοποιείται στην λίστα sheetNames
			        for (int i = 0; i < workbook1.getNumberOfSheets(); i++) {
			            sheetNames.add(workbook1.getSheetName(i));
			        }
			
			        //Προσθήκη στην Dropdownlist sheetDropdown των ονομάτων της λίστας sheetNames
			        //ώστε να μπορεί να επιλέξει ο χρήστης τον συμμετέχοντα για τον οποίο θέλει να γίνει η προσομοίωση
			        for (String sheetName : sheetNames) {
			        	sheetDropdown.addItem(sheetName);
			        }
			        
			      
	        }catch (Exception e) {
	    		e.printStackTrace(); 
	    	}
		   
	
	        setTitle("Αποτελέσματα Προσομοίωσης");
	        setVisible(true);
	        setSize(400,200);
	        setLocationRelativeTo(null); // Center the frame
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   
	    
	        JButton calculate_button = new JButton("Επόμενο");
	        JButton cancel_button = new JButton("Άκυρο");
	        staticLabel1 = new JLabel("Επιλέξτε το Φύλλο του Excel για το οποίο θέλετε να γίνει η προσομοίωση.");
	        
	        JPanel panel = new JPanel();
	      
	        panel.add(staticLabel1);
	        panel.add(sheetDropdown);
	        panel.add(participantDropdown);
	        panel.add(new JLabel("Συμπληρώστε τον χρόνο για τον οποίο επιθυμείται να \"τρέξει\" η προσομοίωση."));
	        panel.add(numberInput);
	        panel.add(new JLabel("Μονάδα χρόνου:"));
	        panel.add(timeUnitDropdown);
	        panel.add(calculate_button);
	        panel.add(cancel_button);
	       
	        setContentPane(panel);
	        
	        //Αποθηκεύεται στο selectedSheet το στοιχείο της αναπτυσσόμενη λίστα που έχει επιλεχθεί
	        String selectedSheet = sheetDropdown.getSelectedItem().toString();
	        
	        //Συνάρτηση με την οποία ενημερώνεται η participantDropdown αναπτυσσόμενη λίστα με τους συμμετέχοντες του μοντέλου
	        //που αναγράφονται στο excel αρχείο, ώστε να επιλέγει ο χρήστης τον συμμετέχοντα για οτν οποίο θέλει να γίνει η προσομοίωση
	        updateParticipantDropdown(selectedSheet, participantDropdown);
          
	        
	        
	        calculate_button.addActionListener(new ActionListener(){

				@Override
	            public void actionPerformed(ActionEvent e) {
	        		
					//Αποθήκευση του επιλεγμένου συμμετέχοντα
	                selectedParticipant = participantDropdown.getSelectedItem().toString();
	               
	                double number;
	                //Έλεγχος αν ο χρήστης έχει δώσει τον χρόνο για τον οποίο θα τρέξει η προσομοίωση 
	                if(!numberInput.getText().isEmpty()) {
	                	number = Double.parseDouble(numberInput.getText());	//αν έχει δωθεί χρόνος τον μετατρέπει σε αριθμό για να τον επεξεργαστεί
	                }
	                else {
	                	number=0;	// αν δεν έχει δωθεί ορίζεται σε 0
	                }
	                
	                
	                String timeUnit = timeUnitDropdown.getSelectedItem().toString();
	                
	             // Μεταροπή του αριθμού που έδωσε ο χρήστης σε δευτερόλεπτα αν δεν έιναι ήδη
                    switch (timeUnit) {
                        case "minutes":
                            number *= 60;
                            break;
                        case "hours":
                            number *= 3600;
                            break;
                        case "seconds":
                        default:
                            break;
                    }
                    
                    //Δίνονται στη κλάση ExcelCollector τα δεδομένα σχετικά:
                    //με το φύλλο του excel που θα προσομειωθεί
                    //τον χρόνο που θα "τρέξει" η προσομοίωση αφού έχει μετατραπεί σε δευτερόλεπτα
                    //τον συμμετέχοντα του μοντέλου για τον οποίο θα γίνει η προσομοίωση
                    ExcelCollector excelCollector = new ExcelCollector(selectedSheet, number, selectedParticipant);
                    
                    //Επιστροφή με βάση τα δεδομένα που στάλθηκαν στη κλάση ExcelCollector των αποτελεσμάτων που προέκυψαν από την προσομοίωση
                    chart = excelCollector.getChart(); // διάγραμμα που εμφανίζει το χρόνο κάθε διεργασίας στο μοντέλο
                    piechart = excelCollector.getPiechart(); //διάγραμμα πίτας που δείχνει το ποσοστό του συνολικού χρόνου που απέσπασε κάθε διαδικασία
                    table = excelCollector.getTable(); // πίνακας με δεδομένα σχετικά με τις διεργασίες που προσομοιώθηκαν
                    queue = excelCollector.getTableQueue(); // πίνακας με δεδομένα σχετικά με τις ουρές που δημιουργούνται σε διάφορα σημεία
                    
                 //Εμφάνιση των διαγραμμάτων και πινάκων στην οθόνη του χρήστη
                    if (chart != null && piechart != null) {
                    	
                        setContentPane(chart);  
                        
                        JFrame frame = new JFrame("Pie Chart Example");
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        frame.getContentPane().add((Component) piechart, BorderLayout.CENTER);
                        frame.pack();
                        frame.setVisible(true);
                        
                        JFrame chartFrame = new JFrame("Chart");
                        chartFrame.getContentPane().add(chart);
                        chartFrame.pack();
                        chartFrame.setVisible(true);
                    }
                    
                    //Προσαρμογή και των δύο πινάκων αποτελεσμάτων στο ίδιο panel
                  JScrollPane scrollPane2 = new JScrollPane((Component) table);
                  JScrollPane scrollPane1 = new JScrollPane((Component) queue);
                  
                    // Create the frame
                    JFrame frame = new JFrame("Αποτελέσματα Πινάκων");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setLayout(new GridLayout(2, 1)); 
               
                    frame.getContentPane().add(scrollPane1);
                    frame.getContentPane().add(scrollPane2);

                    frame.pack();
                    frame.setVisible(true);
                    
                    new Simulation_Results();
				}
	        	
	        });
	        
	        cancel_button.addActionListener(new ActionListener(){
	        	@Override
	            public void actionPerformed(ActionEvent e) {
	        		setVisible(false);
	        		new ExcelOpenerGUI();
	            }
	        	
	        });
	}
	    
	
    private  void updateParticipantDropdown(String selectedSheet, JComboBox<String> participantDropdown) {
    	boolean exists = false;
    	
        participantDropdown.removeAllItems(); // Καθαρισμός των υπάρχοντων στοιχείων της αναπτυσσόμενης λίστας
        
        try (FileInputStream fis = new FileInputStream(SPECIFIC_FILE_PATH);
        		
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheet(selectedSheet);
            
            //Μέσω του excel αρχείου που χρησιμοποιείται βρίσκονται οι συμμετέχοντες του μοντέλου και προστίθενται στην αναπτυσσόμενη λίστα participantDropdown
            if (sheet != null) {
                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row != null) {
                        Cell cell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        if (cell.getStringCellValue().startsWith("Participant")) {
                            participantDropdown.addItem(row.getCell(1).getStringCellValue());
                            exists = true;
                        }
                    }
                }
                
                
            }
            //Εάν πρόκειται για κάποιο μοντέλο απλής μορφής που δεν έχει συμμετέχοντες τότε ως στοιχείο προστίθεται το παρακάτω κείμενο
            if(!exists){
              	 participantDropdown.addItem("Δεν υπάρχουν συμμετέχοντες");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   
    
}



