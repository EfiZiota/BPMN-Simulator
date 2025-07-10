package simulation_gui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelOpenerGUI extends JFrame {
	
	private  final String SPECIFIC_FILE_PATH = Config.getExcelFilePath();
    private JLabel staticLabel;

    public ExcelOpenerGUI() {
    	
        setTitle("Συμπλήρωση Δεδομένων");
        setVisible(true);
        setSize(400,200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        staticLabel = new JLabel("Συμπληρώστε στο Excel τα δεδομένα χρόνου και κόστους του μοντέλου σας."
        		+ "\n Αποθηκεύστε το αρχείο και επιστρέψτε στην οθόνη.");

       
        JButton add_button = new JButton("+");
        JButton next_button = new JButton("Next");
        JButton cancel_button = new JButton("Άκυρο");
        
        //Άνοιγμα αρχειού excel με τα δεδομένα του μοντέλου που έχει επιλεγεί
        //με σκοπό την συμπλήρωση των δεδομένων που χρειάζονται για την προσομοίωση
        add_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	openSpecificExcelFile();
            }
        });

        next_button.addActionListener(new ActionListener(){
        	@Override
            public void actionPerformed(ActionEvent e) {
            	new Simulation_Results();
            	setVisible(false);
            }
        	
        });
        
        cancel_button.addActionListener(new ActionListener(){
        	@Override
            public void actionPerformed(ActionEvent e) {
        		setVisible(false);
        		new BPMNFileAdder();
            }
        	
        });
        
       
        JPanel panel = new JPanel();
        panel.add(staticLabel);
        panel.add(add_button);
        panel.add(next_button);
        panel.add(cancel_button);

        setContentPane(panel);

        pack();
        setLocationRelativeTo(null);
    }
    
    //Συνάρτηση που χρησιμοποιείται ώστε να ανοίγει το excel αρχείο του οποίου το μονοπάτι είναι αποθηκευμένο
    private void openSpecificExcelFile() {
        File specificFile = new File(SPECIFIC_FILE_PATH);
      
        
        try {
        	openExcelFileInSystemDefaultApp(specificFile);
        	
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error opening Excel file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openExcelFileInSystemDefaultApp(File file) {
        try {
            Desktop.getDesktop().open(file);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error opening Excel file with the system default app: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
   

}
