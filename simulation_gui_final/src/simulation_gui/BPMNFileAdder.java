package simulation_gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import simulation_gui.ExcelParser.FileStatus;

public class BPMNFileAdder extends JFrame {
	
    protected static JTextField filePathTextField;
    protected static String filePath;



	public String getFilePath() {
		return filePath;
	}


	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}


	public BPMNFileAdder(String filePath) {
		this.filePath = filePath;
	}


	public static JTextField getFilePathTextField() {
		return filePathTextField;
	}


	public BPMNFileAdder() {
        setTitle("BPMN File Adder");
        setSize(400, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Δημιουργία του GUI όπου γίνεται η επιλογή του bpmn αρχείου ή του υπάρχοντος αρχείου excel που θα χρησιμοποιηθεί για την προσομοίωση
        createUI();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createUI() {
    	
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        filePathTextField = new JTextField();
        filePathTextField.setEditable(false);

        JButton openButton = new JButton("Open");
        JButton browseButton = new JButton("Browse");
        JButton addButton = new JButton("Add to Project");

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseButtonClicked(); 
            }
        });
        

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openButtonClicked();
                
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	
            
            	filePath = addButtonClicked();
            	
            	//Έλεγχος αν έχει επιλεχθεί αρχείο bpmn ή αρχείο excel
            	if(!(filePath==null) && filePath.endsWith(".bpmn"))
            	{
	               //Στην περίπτωση που έχει επιλεγεί .bpmn αρχείο γίνεται χρήση της κλάσης ExcelParser
            	  //ώστε να δημιουργηθεί ένα αρχείο excel στο οποίο θα μεταφερθούν τα δεδομένα του bpmn μοντέλου που επιλέχθηκε
	                ExcelParser excelParser = new ExcelParser(filePath);
	                
	                //Σε περίπτωση που έχει επιλεγεί ένα αρχείο bpmn για το οποίο έχει ήδη δημιουργηθεί στο παρελθόν excel αρχείο
	                //ενημερωνεί το χρήστη ότι υφίσταται ώστε να το αναζητήσει και να μην δημιουργήσει καινούργιο
	                //με αποτέλεσμα να χαθούν τα υπάρχοντα δεδομένα του
	                ExcelParser.FileStatus status = excelParser.checkAndCreateExcelFile();
	                //Αν δεν υπάρχει το αρχείο τότε δημιουργείται και μεταφέρεται στο επόμενο περιβάλλον
	                if(!(status == FileStatus.FILE_EXISTS)) {
	                	 new ExcelOpenerGUI();
	                	 setVisible(false);
	                }
	                
            	}
            	else {
            		//Στην περίπτωση που επιλεγεί ένα αρχείο excel κρατάει το μονοπάτι του και συνεχίζει στο επόμενο βήμα
            		//αφού υπάρχουν ήδη περασμένα τα δεδομένα του μοντέλου σε αυτό
            		Config.setExcelFilePath(filePath);
            		 new ExcelOpenerGUI();
            		 setVisible(false);
            	}
            }
        });

        panel.add(new JLabel("Select BPMN File:"));
        panel.add(filePathTextField);
        panel.add(openButton);
        panel.add(browseButton);
        panel.add(addButton);

        add(panel);
    }

    //Συνάρτηση που ελέγχει ότι όταν επιλέγεται το κουμπί open δεν μπορεί να γίνει επιλογή άλλου αρχείου από τον υπολογιστή
    //εκτός από αρχεία τύπου .xlsx
    private void openButtonClicked() {
    	
    	//Δημιουργία φίλτρου ώστε να εμφανίζονται μόνο αρχεία .xlsx για επιλογή
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Files", "xlsx");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);

      //Εμφάνιση στην οθόνη του μονοπατιού του αρχείου που επιλέχθηκε ώστε να βεβαιώνεται ο χρήστης ότι επέλεξε σωστά
        //πριν συνεχίσει στο επόμενο βήμα
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathTextField.setText(selectedFile.getAbsolutePath());
        }
    }
    
    //Συνάρτηση που ελέγχει ότι όταν επιλέγεται το κουμπί browse δεν μπορεί να γίνει επιλογή άλλου αρχείου από τον υπολογιστή
    //εκτός από αρχεία τύπου .bpmn
    private void browseButtonClicked() {
    	
    	//Δημιουργία φίλτρου ώστε να εμφανίζονται μόνο αρχεία .bpmn για επιλογή
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("BPMN Files", "bpmn");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);

        //Εμφάνιση στην οθόνη του μονοπατιού του αρχείου που επιλέχθηκε ώστε να βεβαιώνεται ο χρήστης ότι επέλεξε σωστά
        //πριν συνεχίσει στο επόμενο βήμα
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathTextField.setText(selectedFile.getAbsolutePath());
        }
    }

    //Συνάρτηση που ελέγχει αν όντως επιλέχθηκε αρχείο και ποιο
    //πρωτού προχωρήσει στο επόμενο βήμα ο χρήστης
    private String addButtonClicked() {
        String filePath = filePathTextField.getText();
        
        if (!filePath.isEmpty()) {
               
            try {
                return filePath; //επιστροφή αρχείου αν υπάρχει
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding file to project", "Error", JOptionPane.ERROR_MESSAGE); //Ενημέρωση αν δημιουργήθηκε κάποιο πρόβλημα με την επιστρόφη του αρχείου
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a file first", "Error", JOptionPane.ERROR_MESSAGE); //Σύσταση στο χρήστη με μήνυμα να επίλεξει αρχείο αν δεν το έχει κάνει
        }
        
		return null;
		
       
        
    }

}
