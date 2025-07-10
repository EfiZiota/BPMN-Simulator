package simulation_gui;

import java.awt.Color;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.PieSeries.PieSeriesRenderStyle;
import org.knowm.xchart.XChartPanel;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class ExcelCollector {
	
	private static final String SPECIFIC_FILE_PATH = Config.getExcelFilePath();
	private String selectedSheet, selectedParticipant;
	private ArrayList<Double> sums = new ArrayList<Double>();
	private ArrayList<String> greater = new ArrayList<String>();
	private ArrayList<String> less = new ArrayList<String>();
	private Multimap<String, String> durationsMM = ArrayListMultimap.create();
	private HashMap<String, String> durations = new HashMap<String, String>();
	private HashMap<String, String> durationsCalculated = new HashMap<String, String>();
	private HashMap<String, String> costs = new HashMap<String, String>();
	private HashMap<String, String> costsCalculated = new HashMap<String, String>();
	private HashMap<String, String> rates = new HashMap<String, String>();
	private HashMap<String, String> exlcusivePathsDurations = new HashMap<String, String>();
	HashMap<String, Double> activitiesdurations = new HashMap<String, Double>();
	private HashMap<String, String> durationsIE = new HashMap<String, String>();
	private HashMap<String, String> durationsIECalculated = new HashMap<String, String>();
	private HashMap<String, String> costsIE = new HashMap<String, String>();
	private HashMap<String, String> costsIECalculated = new HashMap<String, String>();
	private HashMap<String, Double> serviceEnd = new HashMap<String, Double>();
	private HashMap<String, ArrayList<Double>> totalservicesEnd = new HashMap<String, ArrayList<Double>>();
	private HashMap<String, ArrayList<Double>> servicesArrivals = new HashMap<String, ArrayList<Double>>();
	private HashMap<String, Integer> queueData = new HashMap<String, Integer>();
	private HashMap<String, Double> linewaitingData = new HashMap<String, Double>();
	private HashMap<String, Integer> entriesData = new HashMap<String, Integer>();
	
	private ArrayList<Double> probabilities = new ArrayList<Double>();
	private ArrayList<String> paths = new ArrayList<String>();
	private ArrayList<Double> serviceDuration = new ArrayList<Double>();
	private ArrayList<Double> firstarrivals = new ArrayList<Double>();

	private  ArrayList<Integer> counts = new ArrayList<Integer>();
	private  String[] chosenPaths = new String[10]; // Array to store the chosen paths
	    
	private double T=0.0 , sumsamples=0.0, simulationDuration=0;
	private int numberofTokens=0;
	private double loopT;
	private ChartPanel chart;
	private JPanel piechart;
	private double total=0;
	private Object tableTokens, tableQueue;
	private JTable tableactivities;
	private double arrival;
	private int countarrivals=0;
	private String bpmnfile;
	private double previous;
	private double waitingTime;
	private String previousTask;
	private Double previousarrival;
	
	
	
	public ChartPanel getChart() {
		return chart;
	}

	public JPanel getPiechart() {
		return piechart;
	}

	public Object getTableQueue() {
		return tableQueue;
	}

	 public JTable getTableactivities() {
		return tableactivities;
	}


	public Object getTable() {
		return tableTokens;
	}

	public ExcelCollector(String selected, double number, String aselectedParticipant){
		selectedSheet = selected;
		simulationDuration = number;
		selectedParticipant = aselectedParticipant;
		double sum=0;
		
		//Έλεγχος του τύπου του αρχείου που έχει μεταφερθεί για να στέλνεται στη κλάση ParseBpmnData το αρχείο του μοντέλου και όχι το excel του
		if(Config.getExcelFilePath().endsWith(".xlsx")) {
			bpmnfile = Config.getExcelFilePath();
            String fileNameWithoutExtension = bpmnfile.replaceFirst("[.][^.]+$", "");
			bpmnfile = fileNameWithoutExtension + ".bpmn";
		}
		else {
			bpmnfile= Config.getExcelFilePath();
		}
		
		//Χρησιμοποιείται το αρχείο bpmn και ο επιλεγμένος συμμετέχοντας από τον χρήστη
		//ώστε να δημιουργηθεί η λίστα των tasks για τα οποία μας ενδιαφέρει να γίνει η προσομοίωση
		 ParseBpmnData mv = new ParseBpmnData(bpmnfile, selectedParticipant);
		 List<BPMNElement> ordertasks = mv.getTasksInOrderMFV(); // η λίστα των tasks
		 Map<String, List<BPMNElement>>  exclusivesPaths = mv.getPathsDurations(); //τα paths των exclusive gateways που υπάρχουν στον συγκεκριμένο συμμετέχοντα
		 
		
		 
    try (FileInputStream fis = new FileInputStream(SPECIFIC_FILE_PATH);
    		
         Workbook workbook1 = WorkbookFactory.create(fis)) {

        // Θέτουμε το φύλλο του excel στο οποίο θα δουλέψουμε με βάση την επιλογή του χρήστη
        Sheet sheet1 = workbook1.getSheet(selectedSheet);
        
        //Συνάρτηση στην οποία γίνεται έλεγχος εάν τα δεδομένα που έχει εισάγει ο χρήστης είναι σωστά ή όχι
        //ώστε να μην δημιουργηθούν λανθασμένα συμπεράσματα μετέπειτα
        DataValidationCheck(workbook1, sheet1);


        HashMap<String, String>[] maps = new HashMap[]{durations, costs, durationsIE, costsIE, durationsCalculated, costsCalculated};

        //Έλεγχος ώστε αν δεν υπάρχουν δεδομένα χρόνων να μην γίνεται η διαδικασία προσομοίωσης
        Boolean empty=true;
	    for (Map.Entry<String, String> entry : durations.entrySet()) {
	    	if(!entry.getValue().equals("0")) {
	    		empty=false;
	    	}
	    }
        
        
	    //Στοιχεία για τη δημιουργία τυχαίου αριθμού όσον το δυνατόν "καλύτερης" τυχαιότητας
		    long seed = 0;
		    long a = 1103515245; // Multiplier
		    long c = 1013904223; // Increment
		    long m = (long) Math.pow(2, 32); // Modulus
	
		    //Δημιουργία τυχαίου αριθμού
		    LCG arrivalGenerator = new LCG(seed, a, c, m);

		    T=0;
		    
		if(!empty) {
		//Επανάληψη μέχρις ότου να ξεπεραστεί ο χρόνος που έχει θέσει ο χρήστης για την προσομοίωση
		  while(T< simulationDuration) { 	
			 
			 //Δημιουργία τυχαίου δείγματος με βάση τον τυχαίο αριθμό
			 double u1 = arrivalGenerator.next(); 
			 
			 	//ορισμός των χρόνων άφιξης μίας διεργασίας στο σύστημα
		    	arrival += inverseTransformSampleΕxponential(u1, 0.2);
		    	
		    	//Σύνολο αφίξεων στο σύστημα
		    	countarrivals++;
		    	
		    	//Αποθήκευση αφίξεων στο σύστημασ
		    	firstarrivals.add(arrival);
		    	   
				    LCG generator = new LCG(seed, a, c, m);
				  
				    double u = generator.next(); 
				    double sample; 
				    int whichMap=0;
		    
				    //Ενημέρωση seed για την επόμενη επανάληψη
		            seed = generator.getSeed();
		            
		 
			for (HashMap<String, String> map : maps) {
			
			    whichMap++;
			
			    for (Map.Entry<String, String> entry : map.entrySet()) {
			    		//Εάν ελέγχεται το πρώτο Hashmap που περιέχει το εύρος χρόνων των activities
			    		if(whichMap==1) {
				    		if(entry.getKey().startsWith("Activity")) {
					    	if (entry.getValue() != null && !entry.getValue().isEmpty()) {
					            if (entry.getValue().contains("-")) {
					                String[] partsDuration = entry.getValue().split("-"); 
					                
					                //Διαχωρισμός του εύρους κάθε χρόνου σύμφωνα με τα δεδομένα που έχει εισάγει ο χρήστης
					                double floorNumberDuration = Double.parseDouble(partsDuration[0]);
					                double ceilNumberDuration = Double.parseDouble(partsDuration[1]);
					                
					                u = generator.next();
					                //Δημιουργία με την μέθοδο της ομοιόμορφης κατανομής ενός τυχαίου δείγματος που βρίσκεται ανάμεσα σοτ εύρος που έχει οριστεί από τον χρήστη
					                sample = inverseTransformSample(u, floorNumberDuration, ceilNumberDuration);
					                
					                if(whichMap==1) {
					                	//Μέθοδος για την αλλαγή των values στο Hashmap durationsCalculated
					                	 double currentValue = Double.parseDouble(durationsCalculated.getOrDefault(entry.getKey(), "0"));
					                     double newValue = currentValue + sample;
					                     
					                  	durationsCalculated.put(entry.getKey(), String.valueOf(sample));
					                  	
					                  	//Θέτω σε Hashmap την κάθε δραστηριότητα και το τυχαίο δείγμα που δημιουργήθηκε γι' αυτήν
					                	activitiesdurations.put(entry.getKey(), newValue);
					                }
					                else if(whichMap==2) {
					                	costsCalculated.put(entry.getKey(), String.valueOf(sample));
					                }
									else if(whichMap==3) {
										durationsIECalculated.put(entry.getKey(), String.valueOf(sample));	                	
									}
									else if(whichMap==4) {
										costsIECalculated.put(entry.getKey(), String.valueOf(sample));
									}
											                
					               
					            }
					        }
				    	}
			    	}
			    }
			    
			    //ορισμός της διάρκειας κάθε διαδικασίας σύμφωνα με τον τυχαίο αριθμό που προέκυψε από τα δεδομένα του Excel
			    for (Map.Entry<String, String> entry : map.entrySet()) { 
			    	if(whichMap==5) {
		
			            // Accessing methods of the BPMNElement interface
			            for (BPMNElement element : ordertasks) {
			                if (element instanceof BpmnTask) {
			                    BpmnTask task = (BpmnTask) element;
			                    if (task.getTaskId() != null && entry.getKey() != null) {
			                    	
			                        if (task.getTaskId().equals(entry.getKey())) {
			                            task.setdurationSample(Double.parseDouble(entry.getValue()));//θέτω την αντίστοιχη τιμή που έχει αποθηκευτεί στο hashmap durationsCalculated
			                            break;
			                        }
			                    }
			                } 
			            }
			            
			    	}
			    	else if(whichMap==6) {
			    		// Accessing methods of the BPMNElement interface
			            for (BPMNElement element : ordertasks) {
			                if (element instanceof BpmnTask) {
			                    BpmnTask task = (BpmnTask) element;
			                    if (task.getTaskId() != null && entry.getKey() != null) {
			                    	
			                        if (task.getTaskId().equals(entry.getKey())) {
			                            task.setCostSample(Double.parseDouble(entry.getValue()));//θέτω την αντίστοιχη τιμή που έχει αποθηκευτεί στο hashmap costsCalculated
			                            break;
			                        }
			                    }
			                }
			             
			            }
			    	}
			            
			          
			    }
			    
			}
		
		
			//Υπολογισμός συνολικής διάρκειας κάθε μονοπατιού ενός exclusive gateway
			CalculateExclusivePaths(exclusivesPaths,exlcusivePathsDurations);
		
		
		    int size=10;
			for (int i1 = 0; i1 < size; i1++) {
		        counts.add(0);
		    }
		
			boolean isGateway = false;
			
			int counter = 0;

			    //Τρέχουμε την λίστα με τη ροή του μοντέλου
				for (BPMNElement element : ordertasks) {
				
				    counter++;
			
			        if (element instanceof BpmnTask) {
			            BpmnTask task = (BpmnTask) element;
			            
			            //Για κάθε activity που έχει οριστεί κάποια διάρκεια αποθηκεύουμε 
			            if(task.getTaskId().startsWith("Activity") && task.getdurationSample()!=0) {
			            	  servicesArrivals.put(task.getTaskId(), firstarrivals);
			            }
			            
			            //Αν φτάσει σε endEvent σταματάει η διαδικασία
			            if (task.getTaskTag().equals("endEvent") && task.getterminateEventDefinitionTags().isEmpty()){
			            
			            	break;
			            }
			            else if(task.getTaskTag().equals("exclusiveGateway") && task.getOutgoingTags().size()>1) {//Αν πρόκειται για δεδομένα εντός exclusive gateway
			            	//Στην περίπτωση που ξεκινάει κάποιο Exclusive Gateway
			            	isGateway = true;
			            	
			            	
			            	for(int s=0; s<task.getOutgoingTags().size(); s++) {
			            		if(rates.containsKey(task.getOutgoingTags().get(s))) {
			            			//Θέτω στη λίστα probabilities τα ποσοστά των μονοπατιών του συγκεκριμένου exclusive 
			            			//με βάση τι έχει δηλώσει ο χρήστης στο excel
			            			probabilities.add(Double.parseDouble(rates.get(task.getOutgoingTags().get(s)))); 
			            			//Θέτω στη λίστα paths αντίστοιχα τα βέλη που αντιστοιχούν σε κάθε ποσοστό
			            			paths.add(task.getOutgoingTags().get(s));
			            		}
			            	}
			            
			                
			                Random random = new Random();
			                
			                for (int run = 0; run < 1; run++) {
			                	
			                	//Δημιουργία τυχαίου αριθμού μεταξύ 0 και 1
			                    double ran = random.nextDouble();
			
			                   
			                    double cumulativeProbability = 0.0;
			
			                  
			                    //Ελέγχεται αν ο τυχαίος αριθμός που παράχθηκε είναι μικρότερος του cumulativeProbability
			                    for (int t = 0; t < probabilities.size(); t++) {
			                        cumulativeProbability += probabilities.get(t);//Το ποσοστό που αντιστοιχεί στο συγκεκριμένο position προστίθεται στο cumulativeProbability
			                        if (ran < cumulativeProbability) {//Αν είναι μικρότερος
			                        	
			                            counts.get(t);
			                            int currentValue = counts.get(t);//Ορίζεται ως current value ο υπάρχον αριθμός του counts
			
			                            int newValue = currentValue + 1; //Αυξάνεται το ποσοστό του counts κατά ένα
			
			                            counts.set(t, newValue); //Τίθεται η καινούργια τιμή του counts στην θέση t
			                            
			                            chosenPaths[run] = paths.get(t); //Αποθήκευση του μονοπατιού που επιλέχθηκε
			                            break;
			                        }
			                    }
			                }
			            	
			                // Output the chosen paths for each run
			                for (int run = 0; run < chosenPaths.length; run++) {
			
			                    //Ελέγχεται για να βρεθεί το path που επιλέχθηκε με βάση τα ποσοστά στο hashmap exlcusivePathsDurations για να προστεθεί ο χρόνςο του path στο σύνολο
			                    if(exlcusivePathsDurations.containsKey(chosenPaths[run])) {
			                    	
			                    	
			                    	sumsamples += Double.parseDouble(exlcusivePathsDurations.get(chosenPaths[run])); //Προσθήκη συνολικού χρόνου του επιλεγμένου μονοπατιού στο σύνολο
			                    
			                    	
			                    	previous = serviceEnd.getOrDefault(task.getTaskId(), (double) 0);//Κρατείται η στιγμή που τελειώνει η προηγούμενη διεργασία από το συγκεκριμένο task
					            	
					            	//Αν ο χρόνος της διεργασίας που καταφθάνει είναι μικρότερος από το χρόνο που τελείωνει η προηγούμενη διεργασία από το συγκεκριμένο task
					            	if((loopT+arrival)< previous) {
					            		waitingTime = previous - (loopT+arrival); //ορίζεται ο χρόνος αναμονής της τωρινής διεργασίας μέχρι να τελειώσει η προηγούμενη διεργασία
					            	}
					            	else {
					            		waitingTime = 0; //αλλιώς δεν υπάρχει χρόνος αναμονής
					            	}
					            	
					            	//Η διάρκεια της τωρινής διεργασίας είναι ο συνολικός χρόνος του επιλεγμένου συν το χρόνο αναμονής
					            	loopT+= Double.parseDouble(exlcusivePathsDurations.get(chosenPaths[run])) + waitingTime; 
	
			                    	
			                    	if(exclusivesPaths.containsKey(chosenPaths[run])) {
			                    		
			                    		//Λίστα με το στοιχεία του επιλεγμένου path 
			                    		List<BPMNElement> tasksingateway = exclusivesPaths.get(chosenPaths[run]);
			                    		
			                    		//Υπολογισμός ουρών των tasks που συγκαταλέγονται εντός του επιλεγμένου μονοπατιού από το παρών gateway
			                    		for(BPMNElement item: tasksingateway) {
			                    			
			                    			if(((BpmnTask) item).getdurationSample()!=0) {

			        			            	//Προστίθεται στο serviceEnd το id του activity και η χρονική στιγμή που τελειώνει η εξυπηρέτηση της τωρινής διεργασίας
							            		serviceEnd.put(((BpmnTask) item).getTaskId(), (loopT+arrival));
							            		// Προστίθεται στο serviceDuration  η χρονική στιγμή που τελειώνει η εξυπηρέτηση της τωρινής διεργασίας
							            		serviceDuration.add(loopT+arrival);
							            		
							            		//Στις λίστες προστίθεται οι χρόνοι τέλους εξυπηρέτησης της τωρινής και της προηγούμενης διεργασίας
							                    ArrayList<Double> retrievedList = totalservicesEnd.get(((BpmnTask) item).getTaskId());
							                    ArrayList<Double> retrievedListprevious =  totalservicesEnd.get(previousTask);
							         
							                
							                    if (retrievedList == null) {//Αν δεν υπάρχουν χρόνοι για την τωρινή διεργασία
							                    
								                    //Θέτουμε στο hashmap totalservicesEnd ως key το id του task και ως value προστίθεται στη λίστα η χρονική στιγμή που τελειώνει η εξυπηρέτηση της τωρινής διεργασίας
								                    totalservicesEnd.put(((BpmnTask) item).getTaskId(), new ArrayList<>(serviceDuration));
								                    	
							                    }else {//Αν υπάρχουν χρόνοι για την τωρινή διεργασία
							                    
							                    	//Προσθέτουμε στη λίστα την χρονική στιγμή που τελειώνει η εξυπηρέτηση της τωρινής διεργασίας 
								                    retrievedList.add(loopT + arrival); 

								                    //Θέτουμε στο hashmap totalservicesEnd ως key το id του task και ως value προστίθεται στη λίστα η χρονική στιγμή που τελειώνει η εξυπηρέτηση της τωρινής διεργασίας
								                    totalservicesEnd.put(((BpmnTask) item).getTaskId(), retrievedList);
								                    
							                    }

							                    if(counter > 2) {//Από το δεύτερο task και μετά
							                    
								                    if (retrievedListprevious == null) {//Αν δεν υπάρχουν χρόνοι για την τωρινή διεργασία
		        				                    	//Θέτουμε στο hashmap servicesArrivals ως key το id του task και ως value προστίθεται στη λίστα η χρονική στιγμή που γίνεται η άφιξη της τωρινής διεργασίας στο task
	        						                    servicesArrivals.put(((BpmnTask) item).getTaskId(),retrievedListprevious);
								                    }
								                    else {//Αν υπάρχουν χρόνοι για την τωρινή διεργασία
											        	
		        				                    	//Θέτουμε στο hashmap servicesArrivals ως key το id του task και ως value προστίθεται στη λίστα η χρονική στιγμή που γίνεται η άφιξη της τωρινής διεργασίας στο task
		        					                    servicesArrivals.put(((BpmnTask) item).getTaskId(),retrievedListprevious);
								                    }
							                    }
							                    
							                    serviceDuration.clear();//Καθαρισμός του serviceDuration για την επόμενη επανάληψη
							                    
							                	previousTask = ((BpmnTask) item).getTaskId();//Θέτουμε τo τωρινό task ως previous για την επόμενη επανάληψη
							                    
							            	}
			                    		}
			                    		
			                    		
			                    		
			                    	}
			                    }
			                    
			                }
			                
			            
			                probabilities.clear();//Καθαρισμός πίνακα πιθανοτήτων για την επόμενη επανάληψη
			                paths.clear();//Καθαρισμός πίνακα μονοπατιών για την επόμενη επανάληψη
			                
			                //Μηδενισμός counts
			            	for (int i1 = 0; i1 < size; i1++) {
			            		 counts.set(i1, 0);
			                }
			                
			            }
			            else if(!isGateway) {//Αν δεν πρόκειται για δεδομένα εντός exclusive gateway
			            	
			            	sumsamples = sumsamples + task.getdurationSample();//Προσθήκη χρόνου task στο σύνολο
			            
			            	
			            	previous = serviceEnd.getOrDefault(task.getTaskId(), (double) 0); //Κρατείται η στιγμή που τελειώνει η προηγούμενη διεργασία από το συγκεκριμένο task
			            	
			            	
			            	//Αν ο χρόνος της διεργασίας που καταφθάνει είναι μικρότερος από το χρόνο που τελείωνει η προηγούμενη διεργασία από το συγκεκριμένο task
			            	if((loopT+arrival)< previous) {
			            		waitingTime = previous - (loopT+arrival); //ορίζεται ο χρόνος αναμονής της τωρινής διεργασίας μέχρι να τελειώσει η προηγούμενη διεργασία
			            	}
			            	else {
			            		waitingTime = 0; //αλλιώς δεν υπάρχει χρόνος αναμονής
			            	}
			            	
			            	loopT+= task.getdurationSample() + waitingTime; //Η διάρκεια της τωρινής διεργασίας στο συγκεκριμένο task είναι ο χρόνος εξυπηρετήσης της συν το χρόνο αναμονής της
			            	
			            	if(task.getdurationSample()!=0) {//Αν το task έχει χρόνο εξυπηρέτησης
			            		
			            		
			            		serviceEnd.put(task.getTaskId(), (loopT+arrival));//Προστίθεται στο serviceEnd το id του activity και η χρονική στιγμή που τελειώνει η εξυπηρέτηση της τωρινής διεργασίας
			            		serviceDuration.add(loopT+arrival); // Προστίθεται στο serviceDuration  η χρονική στιγμή που τελειώνει η εξυπηρέτηση της τωρινής διεργασίας
			            		
			            	   
			            		//Στις λίστες προστίθεται οι χρόνοι τέλους εξυπηρέτησης της τωρινής και της προηγούμενης διεργασίας
			                    ArrayList<Double> retrievedList = totalservicesEnd.get(task.getTaskId());
			                    ArrayList<Double> retrievedListprevious =  totalservicesEnd.get(previousTask);
			                    
			                    if (retrievedList == null) {//Αν δεν υπάρχουν χρόνοι για την τωρινή διεργασία
			                    	
			                    	//Θέτουμε στο hashmap totalservicesEnd ως key το id του task και ως value προστίθεται στη λίστα η χρονική στιγμή που τελειώνει η εξυπηρέτηση της τωρινής διεργασίας
			                    	totalservicesEnd.put(task.getTaskId(), new ArrayList<>(serviceDuration));
			                    	
			                    	
			                    }
			                    else {//Αν υπάρχουν χρόνοι για την τωρινή διεργασία

				                    //Προσθέτουμε στη λίστα την χρονική στιγμή που τελειώνει η εξυπηρέτηση της τωρινής διεργασίας 
				                    retrievedList.add(loopT + arrival);

				                  //Θέτουμε στο hashmap totalservicesEnd ως key το id του task και ως value προστίθεται στη λίστα η χρονική στιγμή που τελειώνει η εξυπηρέτηση της τωρινής διεργασίας
				                    totalservicesEnd.put(task.getTaskId(), retrievedList); 
				                    
			                    }

			                    if(counter > 2) {//Από το δεύτερο task και μετά
			                    	
				                    if (retrievedListprevious == null) {//Αν δεν υπάρχουν χρόνοι για την τωρινή διεργασία
				                    	//Θέτουμε στο hashmap servicesArrivals ως key το id του task και ως value προστίθεται στη λίστα η χρονική στιγμή που γίνεται η άφιξη της τωρινής διεργασίας στο task
						                    servicesArrivals.put(task.getTaskId(),retrievedListprevious);
						                   
				                    }else {//Αν υπάρχουν χρόνοι για την τωρινή διεργασία
	
				                    	//Θέτουμε στο hashmap servicesArrivals ως key το id του task και ως value προστίθεται στη λίστα η χρονική στιγμή που γίνεται η άφιξη της τωρινής διεργασίας στο task
					                    servicesArrivals.put(task.getTaskId(),retrievedListprevious);
					                    
				                    }
			                    }
			                    
			                    
			                    serviceDuration.clear();//Καθαρισμός του serviceDuration για την επόμενη επανάληψη
			                    
			                	previousTask = task.getTaskId();//Θέτουμε τo τωρινό task ως previous για την επόμενη επανάληψη
			            	}
			            	
			            	
			            }
			            
			            //Αν το τελειώνει το exclusive gateway ενημερώνεται το isGateway για την επόμενη επανάληψη
			            if(task.getTaskTag().equals("exclusiveGateway") && task.getIncomingTags().size()>1) {
			            	isGateway = false;
			            }
			            
			        }
			        
				}
				
				T = loopT + arrival; //Ανανέωση του ρολογιού της προσομοίωσης στο τέλος κάθες διεργασίας
				sums.add(loopT);//Προστίθεται ο συνολικός χρόνος κάθε διεργασίας
		        loopT=0; //Μηδενίζεται ο χρόνος για την επόμενη επανάληψη
				
		    }
		
			
		   int maxqueue=0, entries=0;
		   Double linewaiting=0.0;
		   //Τρέχουμε τα hashmap servicesArrivals & totalservicesEnd
		    for (Entry<String, ArrayList<Double>> entry : servicesArrivals.entrySet()) {
	            String key = entry.getKey();
	            //Θέτουμε στις λίστες τις τιμές για το ίδιο key
	            ArrayList<Double> arrival = entry.getValue();
	            ArrayList<Double> departure = totalservicesEnd.get(key);
	            
	            int queue=0;
	            
	            for(int ar=1; ar<arrival.size(); ar++) {
	            	
	            	queue=0;
	            	boolean sameArrival = true;  // Flag to track if still in the same arrival.get(ar)

	            	for(int dep=0; dep<departure.size(); dep++) {
	            		if(dep < ar) {
	            			if(arrival.get(ar)<departure.get(dep)) {//Αν η άφιξη είναι μικρότερη του τέλους εξυπηρέτησης
	            				if (arrival.get(ar) != previousarrival) {//Έλεγχος ότι η άφιξη είναι διαφορετική από την προηγούμενη
	                                sameArrival = false;
	                            } else {
	                                sameArrival = true;
	                            }
	                            previousarrival = arrival.get(ar); //Θέτουμε την τωρινή άφιξη ως previous για την επόμενη επανάληψη
		            			queue++;//Αύξηση της ουράς
		            			
		            			if(!sameArrival) {//Αν έχουμε διαφορετική άφιξη
		            				entries++;//Αύξηση των entries στην ουρά
		            			}
		            			if(ar-dep==1) {
		            				linewaiting += departure.get(dep) - arrival.get(ar); //Χρόνος αναμονής στην ουρά
		            			}
		            			
		            		}
	            		}
	            		else {
	            			break;
	            		}
	            	
	            		
	            		
	            	}
	            	 
	            	//Εύρεση της μεγαλύτερης ουράς που δημιουργείται στη διάρκεια της προσομοίωσης
	            	if(queue>maxqueue) {
	            		maxqueue = queue;
	            	}
	            }
	            
	            
	            queueData.put(key,maxqueue);//Προστίθεται για κάθε activity η μέγιστη ουρά του
	            entriesData.put(key, entries);//Προστίθεται για κάθε activity πόσοι εισήχθησαν σε ουρά
	            linewaitingData.put(key, linewaiting);//Προστίθεται για κάθε activity ο χρόνος αναμονής στην ουρά
	            entries=0;
	            linewaiting = 0.0;
	        }
		    
		    
		    //Βρίσκουμε τη max τιμή του sums
	        Double maxValue = Collections.max(sums);
	        //Βρίσκουμε τη min τιμή του sums
	        Double minValue = Collections.min(sums);
		    
	        //Βρίσκουμε τον αριθμό των διεργασιών
	        Double TotalTokens = (double) sums.size();
	     
	        for(Double element: sums) {
	        	total += element;
	        }
	        
	        //Βρίσκουμε το μέσο όρο χρόνου των διεργασιών
	        Double AverageValue = total/sums.size();
	        
	        //Δημιουργία των πίνακων με τα δεδομένα που παράχθηκαν 
	        tableTokens = TableData(minValue, maxValue, AverageValue, TotalTokens);
	        tableQueue = QueueData(queueData,entriesData,linewaitingData);
	        
	        //Δημιουργία των διαγραμμάτων με τα δεδομένα που παράχθηκαν 
		    chart = ChartExample("",sums);
		    piechart = createPieChartPanel(activitiesdurations);
    
		}	
    }catch (Exception e) {
    	e.printStackTrace(); 
    }
    
	
}
	





	private void CalculateExclusivePaths(Map<String, List<BPMNElement>> exclusivesPaths, HashMap<String, String> exlcusivePathsDurations) {
		
		double sumexc=0.0;
		
		// Τρέχω το hashmap exclusivesPaths που περιέχει τις διαδικασίες που υπάρχουν σε κάθε μονοπάτι του κάθε Exclusive gateway
		// που υπάρχει στο μοντέλο που προσομοιώνεται
		
	    for (String key : exclusivesPaths.keySet()) {
	        //System.out.println("Key: " + key);
	        List<BPMNElement> list = exclusivesPaths.get(key);
	        for (BPMNElement item : list) {
	            sumexc += ((BpmnTask)item).getdurationSample(); // κρατάω το σύνολο τον χρόνων κάθε μονοπατιού
	        }
	        exlcusivePathsDurations.put(key, String.valueOf(sumexc));// αποθηκεύω για κάθε μονοπάτι ενός exclusive gateway το σύνολο διάρκειας του
	        sumexc=0;
	    }
	
	    //Η παραπάνω διαδικασία γίνεται ώστε να χρησιμοποιηθεί αργότερα στα ποσοστά πιθανοτήτων όπου έχουν οριστεί για κάθε μονοπάτι σε κάθε επανάληψη
	
	 
		
	}

	//Συνάρτηση που δημιουργεί τον πίνακα με τα δεδομένα των ουρών της προσομοίωσης
	private JTable QueueData(HashMap<String,Integer> queueData, HashMap<String, Integer> entriesData, HashMap<String, Double> linewaitingData) {
	
		//Εισαγωγή επικεφαλίδων για τις στήλες του πίνακα
        String[] columnHeaders = {"Activity", "MaxQueue", "Entries", "TotalWaitingTime", "AverageWaitingTime"};

        // Αποθήκευση των δεδομένων που λήφθηκαν ως ορίσματα
        int size = queueData.size(); // All maps have the same size and keys
        Object[][] data = new Object[size][5];
        int i = 0;
        for (String key : queueData.keySet()) {
            data[i][0] = key;
            data[i][1] = queueData.get(key);
            data[i][2] = entriesData.get(key);
            data[i][3] = linewaitingData.get(key);
            data[i][4] = linewaitingData.get(key)/entriesData.get(key);
            i++;
        }

        //Δημιουργία πίνακα με τις επικεφαλίδες και τα αντίστοιχα στοιχεία
        JTable table = new JTable(data, columnHeaders);

        
		return table; //Επιστροφή πίνακα
	}


	//Συνάρτηση που δημιουργεί τον πίνακα με τα δεδομένα των διεργασιών της προσομοίωσης
	 private  JTable TableData(Double min, Double max, Double average, Double Total) {
		 
		 	//Αποθήκευση των δεδομένων που λήφθηκαν ως ορίσματα
	        Double[][] data = {
	            {max, min, average,Total},
	        };
	        
			//Εισαγωγή επικεφαλίδων για τις στήλες του πίνακα
	        String[] columnHeaders = {"MaxValue", "MinValue", "Average", "Total"};

	   
	        //Δημιουργία πίνακα με τις επικεφαλίδες και τα αντίστοιχα στοιχεία
	        JTable table = new JTable(data, columnHeaders);
	        
	        //Προσαρμογή του πλάτους των στηλών
	        table.getColumnModel().getColumn(0).setPreferredWidth(100);
	        table.getColumnModel().getColumn(1).setPreferredWidth(150);
	        table.getColumnModel().getColumn(2).setPreferredWidth(100);
	        table.getColumnModel().getColumn(3).setPreferredWidth(100);
	        
	        return table; //Επιστροφή πίνακα
	        
	 }
	        
	 //Συνάρτηση που δημιουργεί διάγραμμα τύπου πίτα για τα activities της διαδικασίας
	 private JPanel createPieChartPanel(HashMap<String, Double> data) {
	        //Αποθηκεύονται στους πίνακες τα id των activities και οι τιμές τους
	        String[] labels = data.keySet().toArray(new String[0]);
	        double[] values = data.values().stream().mapToDouble(Double::doubleValue).toArray();

	        //Δημιουργείται το διάγραμμα πίτα
	        PieChart chart = new PieChartBuilder().width(800).height(600).title("Pie Chart").build();

	        
	        chart.getStyler().setLegendVisible(true);
	        chart.getStyler().setDefaultSeriesRenderStyle(PieSeriesRenderStyle.Donut);

	        //Βάζουμε τα δεδομένα στο διάγραμμα
	        for (int i = 0; i < labels.length; i++) {
	            chart.addSeries(labels[i], values[i]);
	        }

	        return new XChartPanel<>(chart); //Επιστρέφεται το διάγραμμα
	 }
	  
	 //Συνάρτηση που δημιουργεί το panel διαγράμματος τύπου γραμμή
	 private ChartPanel ChartExample(String title, ArrayList<Double> sums) {
	        JFreeChart lineChart = createChart(sums);
	        ChartPanel chartPanel = new ChartPanel(lineChart);
	        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
	        return chartPanel;
	 }

	  //Συνάρτηση που δημιουργεί διάγραμμα τύπου γραμμή
	 private JFreeChart createChart(ArrayList<Double> sums) {
		 
	        XYSeries series = new XYSeries("Sum");
	        
	        //Προστίθενται τα δεδομένα της λίστα sums
	        for (int i = 0; i < sums.size(); i++) {
	            series.add(i + 1, sums.get(i));
	        }

	        XYSeriesCollection dataset = new XYSeriesCollection(series);

	        JFreeChart chart = ChartFactory.createXYLineChart(
	                "Διάρκεια διεργασίων",         // Τίτλος γραφήματος
	                "Αριθμός Διεργασίας",     // Τίτλος άξονα x
	                "Σύνολο διάρκειας",   // Τίτλος άξονα y
	                dataset,              
	                PlotOrientation.VERTICAL,
	                true,              
	                true,
	                false);

	        
	        XYPlot plot = chart.getXYPlot();
	        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

	        //Εμφάνιση των σημείων στη γραμμή
	        renderer.setSeriesShapesVisible(0, true);
	        renderer.setSeriesShapesFilled(0, true);
	        renderer.setSeriesLinesVisible(0, true);

	        plot.setRenderer(renderer);

	        //Χρωματισμός γραμμής
	        renderer.setSeriesPaint(0, Color.BLUE);

	        return chart;//Επιστροφή γραφήματος
	}


	    //Έλεγχος της τιμής του κελιού ώστε να επιστρέφεται πάντα σε μορφή String
    private  String parseCellValue(Cell cell) {
        String cellValue = "";

        if (cell != null) {
            switch (cell.getCellType()) {
                case STRING:
                    cellValue = cell.getStringCellValue();
                    break;
                case NUMERIC:
                    // You may need additional formatting based on your requirements
                    cellValue = String.valueOf(cell.getNumericCellValue());
                    break;
                case BOOLEAN:
                    cellValue = String.valueOf(cell.getBooleanCellValue());
                    break;
                // Handle other cell types as needed
                       	
                default:
                    // Handle other cell types or leave the cell value empty
                	
            }
        }

        return cellValue;
    }

 
  //Συνάρτηση που επιστρέφει το αποτέλεσμα της ομοιόμορφης κατανομής
    public static double inverseTransformSample(double u,double a,double b) {
    	Double x=0.0;
    	
        x = a + u * (b-a);
    	
    	return x;
    }

    //Συνάρτηση που επιστρέφει το αποτέλεσμα της εκθετικής κατανομής
    public static double inverseTransformSampleΕxponential(double u, double lambda) {
     
        return -Math.log(1 - u) / lambda;
    }
    
    
    public class LCG {
        
            private long seed;
            private long a;
            private long c;
            private long m;

            public LCG(long seed, long a, long c, long m) {
                this.seed = seed;
                this.a = a;
                this.c = c;
                this.m = m;
            }

            	
            //Δημιουργία του επόμενο τυχαίου δείγματος με το καινούργιο seed
            public double next() {
                seed = (a * seed + c) % m;
                return (double) seed / m;
            }
            
            public long getSeed() {
                return seed;
            }
        
    }
    
    //Συνάρτηση που ελέγχει ένα η δομή των δεδομένων που εισήγαγε ο χρήστης είναι τα σωστά
    private void DataValidationCheck(Workbook workbook, Sheet sheet) {

        int[] dataColumn = {1,2,5,8,9};
        int [] bpmnColumn = {0,6,9};
        int columnIndex = 0; // Assuming you want to check the third column (index starts from 0)
        int count = 0;
        boolean flag = true;
        int i=1,r=0,pos=0;
        String cellDuration,cellCost,cellDurationIE,cellCostIE;
        ArrayList<String> cellRate = new ArrayList<>();
        ArrayList<String> cellRateFlow = new ArrayList<>();
        boolean isBold=false,enter=true,last=true;
        ArrayList<Integer> emptycells = new ArrayList<>() ;
        double nextRandom;
        ArrayList<Cell> wrongDATA = new ArrayList<Cell>(); 
        ArrayList<Cell> maybeError = new ArrayList<Cell>();
        int p = 0;
        double sum=0;
        
        
        while(flag) {
        	
        	   //Γίνεται έλεγχος των γραμμών της στήλης 5 ώστε να βρεθεί ο αριθμός των γραμμών που έχουν δεδομένα
        	   Row row1 = sheet.getRow(i);
       		
               if (row1 != null) {
               	
	               	Cell cell1 = row1.getCell(5);
	               	
	               	if(parseCellValue(cell1) != "") {
	               		r++;
	               	}
	               	else {
	               		flag=false;
	               	}
               }
               else {
            	   flag=false;
               }
               i++;
        }
        
        //Δημιουργία θέσεων πίνακα με βάση τα exclusive gateways που υπάρχουν
        String[][] errorCells = new String[r][2];
       
        flag=true;
     
    for(int k=0;k<bpmnColumn.length;k++) {

    	i=1;
    	flag=true;
    	
        while(flag) {
    		
        	Row row = sheet.getRow(i);
        		
		        if (row != null) {
		        	
		        	Cell cell = row.getCell(bpmnColumn[k]);
		        	
		        
		            if (cell == null) {
		                cell = row.createCell(bpmnColumn[k]);
		            	
		            }
		            
		            //Γίνεται έλεγχος των στηλών με τα id ώστε για όποιες γραμμές υπάρχονυ δεδομένα να γίνεται έλεγχος
		        	if(parseCellValue(cell) != "") {
		        		
		        		switch(bpmnColumn[k]) {
				        	case 0:
	
				        		if(parseCellValue(cell) != "") {
				        		      
				        			String name = parseCellValue(cell);
				        			
				        			cellDuration = parseCellValue(row.getCell(2));//Θέτουμε το κελί της στήλης 2 που έχει τα δεδομένα χρόνου
				        			cellCost = parseCellValue(row.getCell(3));//Θέτουμε το κελί της στήλης 3 που έχει τα δεδομένα κόστους
				        			
				        		    
								        if(cellDuration != "") {
								        	//Αν στα δεδομένα υπάρχει το σύμβολο της παύλας
								        	if(cellDuration.contains("-")) {
								        		
									        	String[] partsDuration = cellDuration.split("-");
									        	
									        	//Χωρίζονται οι αριθμοί που υπάρχουν πριν και μετά την παύλα ως κατώτατο και ανώτερο όριο
									        	String floorNumberDuration = partsDuration[0];
							        		    String ceilNumberDuration = partsDuration[1];
							        		    
							        		    //Αν το κατώτατο όριο είναι μεγαλύτερο από το ανώτατο το κελί αποθηκεύεται στη λίστα με τα λάθος δεδομένα
							        		    if(Double.parseDouble(floorNumberDuration) > Double.parseDouble(ceilNumberDuration)) {
							        		    	if(!wrongDATA.contains(cell)) {
							        		    		wrongDATA.add(cell);
							        		    	}
							        		    	
							        		    	//Αν το κελί είναι λανθασμένο τότε μηδενίζονται οι χρόνοι του συγκεκριμένου activity
							        		    	cellDuration = "0-0";						        		    	
							        		    }
							        		        
									        	
								        	}
								        	else {//Αν στα δεδομένα δεν υπάρχει το σύμβολο της παύλας
								        		
								        		//Μηδενίζονται οι χρόνοι του συγκεκριμένου activity
								        		cellDuration = "0-0";
								        		
								        		 //το κελί αποθηκεύεται στη λίστα με τα λάθος δεδομένα
								        		if(!wrongDATA.contains(cell)) {
						        		    		wrongDATA.add(cell);
						        		    	}
								        	}
								        	
								        }
								        else {//Αν το κελί είναι κενό οι χρόνοι του activity θεωρούνται μηδενικοί
								        	
								        	cellDuration = "0-0";
								        }
								        
								        durations.put(name, cellDuration);//Στο hashmap durations αποθηκεύονται ως key το id του activity και οι ελεγμένοι χρόνοι
								      
								        
					        			//Ακολουθείται η ίδια ακριβώς διαδικασία και για τα κόστη
								        if(cellCost != "") {
								        	
								        	if(cellCost.contains("-")) {
							        			String[] partsCost = cellCost.split("-");
						        		       
						        		        String floorNumberCost = partsCost[0];
						        		        String ceilNumberCost = partsCost[1];
						        		        
						        		        if(Double.parseDouble(floorNumberCost) > Double.parseDouble(ceilNumberCost)) {
						        		        	if(!wrongDATA.contains(cell)) {
							        		    		wrongDATA.add(cell);
							        		    	}
						        		        	cellCost="0-0";
							        		    }
										        
									        }
									        else {
									        		cellCost="0-0";
									        		if(!wrongDATA.contains(cell)) {
							        		    		wrongDATA.add(cell);
							        		    	}
									        	}
									        }
									        else {
									        	cellCost="0-0";
									        }
								        
								        costs.put(name,cellCost);
								        
								}
				        	
				        		break;
				    
				        	case 6:

				        		if(enter) {
						               
				        			//Λίστα στην οποία αποθηκεύονται τα κελία που το κείμενο του είναι σε στυλ italic 
						                List<Cell> italicCellsBetweenBold = new ArrayList<>();
						                
						                
						                // Variables to track the previous bold cell
						                //Στη μεταβλητή previousBoldCell αποθηκεύεται το τελευταίο κελί που είχε bold κείμενο
						                Cell previousBoldCell = null;
		
						                // Γίνεται έλεγχος των κελιών της στήλης 6 όσο υπάρχουν δεδομένα
						                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
						                    Row row1 = sheet.getRow(rowIndex);
						                    if (row1 != null) {
						                    	
						                        Cell currentCell = row1.getCell(6);
						                      
							                        if(parseCellValue(currentCell) != "") {//Αν έχει δεδομένα το κελί
							                        	
							                        	//Βρίσκω το στυλ του current κελιού
							        	            	CellStyle cellStyle = currentCell.getCellStyle();
									        	        org.apache.poi.ss.usermodel.Font font = workbook.getFontAt(cellStyle.getFontIndex());
									        	        
									                        if (currentCell != null) {
									                            
									                            boolean currentCellItalic = font.getItalic();//Αν το current κελί είναι στλυ italic
					
									                            //Ελέγχεται αν το προηγούμενο κελί ήταν bold και το current είναι Italic
									                            if (previousBoldCell != null && currentCellItalic) {
									                            	
									                            	//Αν ισχύει η παραπάνω συνθήκη τότε σύμφωνα με το format του excel αρχείου
									                            	//στη συγκεκριμένη γραμμή ο χρήστης ορίζει ποσοστό
									                                italicCellsBetweenBold.add(currentCell);
									                                
									                                cellRate.add(parseCellValue(row1.getCell(7)));//Αποθηκεύεται το ποσοστό 
									                                cellRateFlow.add(parseCellValue(row1.getCell(5)));//Αποθηκεύεται το id του βέλους που αφορά το ποσοστό
									                            	
									                            }
					
									                            //Αν το current κελί είναι bold
									                            if (font.getBold()) {
									                            	
									                            	maybeError.add(currentCell);//Προστίθεται στο maybeError το current κελί που έχει το όνομα κάποιου exclusive gateway 
									                            	
									                                previousBoldCell = currentCell; //Ορίζεται ως τελευταίο bold κελί το current
									                             
										        	        		Random random = new Random();
										        	        		
										        	        		for(int q=0;q<cellRate.size();q++) {
										        	        			
										        	        			if(cellRate.get(q).equals("")) {//Αν δεν έχει δωθεί δεδομένο ποσοστού στο current κελί
																			emptycells.add(q); //Κρατείται η θέση του κελιού
																			count++;
									        	        				}
									        	        				else {//Αν έχει δωθεί δεδομένο ποσοστού στο current κελί
									        	        					sum += Double.parseDouble(cellRate.get(q)); //προστίθεται στο sum το ποσοστό του κελιού
									        	        				}

										        	        		}
										        	        		
									        	        			if(sum<1 && count==0 && cellRate.size()>0) {//Αν το σύνολο των ποσοστών που όρισε ο χρήστης για τα μονοπάτια του exclusive είναι μικρότερο του 100%
									        	        				
									        	        				if(maybeError.size()>0) {
									        	        					//Αποθηκεύεται στο πίνακα errorCells το κελί με το όνομα του gateway με το λάθος & το l που συμβολίζει το less
										        	        				errorCells[pos][0] = maybeError.get(maybeError.size()-2).toString();
										        	        				errorCells[pos][1] = "l";
										        	        				pos++;	
									        	        				}
									        	        				else if(maybeError.size() == 0){

										        	        				errorCells[pos][0] = maybeError.get(0).toString();
										        	        				errorCells[pos][1] = "l";
										        	        				pos++;	
									        	        				}
									        	        			}
									        	        			else if(sum>1 && count==0 && cellRate.size()>0) {//Αν το σύνολο των ποσοστών που όρισε ο χρήστης για τα μονοπάτια του exclusive είναι μεγαλύτερο του 100%
									        	        				if(maybeError.size()>0) {
									        	        					//Αποθηκεύεται στο πίνακα errorCells το κελί με το όνομα του gateway με το λάθος & το g που συμβολίζει το gr
										        	        				errorCells[pos][0] = maybeError.get(maybeError.size()-2).toString();
										        	        				errorCells[pos][1] = "g";
										        	        				pos++;	
									        	        				}
									        	        				else {

										        	        				errorCells[pos][0] = maybeError.get(0).toString();
										        	        				errorCells[pos][1] = "g";
										        	        				pos++;	
									        	        				}
									        	        			}
									        	     
										        	        		if(sum<1 && count!=0) {//Αν το σύνολο των ποσοστών είναι μικρότερο του 100% αλλά ο χρήστης δεν έχει συμπληρώσει ποσοστά για όλα τα μονοπάτια
										        	        			for(int m=0; m<count-1; m++) 
											        	        		{
										        	        				
											        	        			nextRandom = random.nextDouble(1-sum);//Δημιουργείται τυχαίος αριθμός στο διάστημα του περισσευάμενου ποσοστού
											        	        			sum+=nextRandom; //Προστίθεται στο sum που ελέγχει το σύνολο

											        	        			//Εισάγεται στο cellRate ο τυχαίος αριθμός που παράχθηκε
											        	        			cellRate.set(emptycells.get(m),String.valueOf(nextRandom));
											        	        		   
											        	        			
											        	        		}
										        	        			 
										        	        			//Εισαγωγή και για το τελευταίο μονοπάτι
										        	        			cellRate.set(emptycells.get(count-1), String.valueOf(1-sum));
										        	        		
													        	        sum += 1-sum;  //Προστίθεται στο sum που ελέγχει το σύνολο
										        	        	
										        	        		}
										        	        		else if(sum>=1 && count!=0) { //Αν το σύνολο των ποσοστών είναι μεγαλύτερο του 100% αλλά ο χρήστης δεν έχει συμπληρώσει ποσοστά για όλα τα μονοπάτια
										        	        			
										        	        			//Μηδενίζει τα ποσοστά όλων των κελιών του exclusive
										        	        			for(int m=0; m<count-1; m++) 
											        	        		{
											        	        			cellRate.set(emptycells.get(m),"0"); 
											        	        		}
										        	        			
										        	        			cellRate.set(emptycells.get(count-1), "0");
										        	        			
										        	        			if(maybeError.size()>0) {
										        	        				//Αποθηκεύεται στο πίνακα errorCells το κελί με το όνομα του gateway με το λάθος & το g που συμβολίζει το gr
										        	        				errorCells[pos][0] = maybeError.get(maybeError.size()-2).toString();
										        	        				errorCells[pos][1] = "g";
										        	        				pos++;	
									        	        				}
									        	        				else {

										        	        				errorCells[pos][0] = maybeError.get(0).toString();
										        	        				errorCells[pos][1] = "g";
										        	        				pos++;	
									        	        				}
										        	        		}
										        	        		
										        	        	
										        	        		double sumcheck = 0;
										        	        		String rateFlow;
										        	        		
										        	        		//Ελέγχεται το ποσοστό του τελικού συνόλου για κάθε exclusive
										        	        		for(String rate : cellRate) {
													                	sumcheck+= Double.parseDouble(rate);
													                }
										        	        		
										        	        		
										        	        		//Αν το σύνολο είναι 100%
										        	        		if(String.valueOf(sumcheck).equals("1.0")) {
										        	        			for(String rate : cellRate) {
										        	        				rateFlow= cellRateFlow.get(p);
										        	        				rates.put(rateFlow, rate);//Εισάγεται στο hashmap rates ως key το id του βέλους και το ποσοστό του
										        	        				p++;
														                }
										        	        			
										        	        		}
										        	        		else{//Αν δεν είναι 100%
											        	        		for (String rate : cellRate) {
										        	                        rateFlow = cellRateFlow.get(p);
										        	                        rates.put(rateFlow, "0");//Εισάγεται στο hashmap rates ως key το id του βέλους και ως ποσοστό το 0
										        	                        p++;
									        	                    	}
										        	        		}
										        	        	
//										        	        		
										        	        		//Καθαρισμός των στηλών και μηδενισμός των μεταβλητών για το επόμενο exclusive
										        	        		cellRate.clear();
										        	        		sum=0;
										        	        		count=0;
										        	        		emptycells.clear();
										        	        		enter=false;
									                             }
									                            
									                         }
							                        }
							                        //Για τη τελευταία γραμμή με δεδομένα γίνεται η ίδια διαδικασία ελέγχων απλά γίνεται ξεχωριστά γιατί δεν ακολουθεί κάποιο bold κείμενο
							                        else if(parseCellValue(currentCell) == "" && last){
							                        	
							                        	 previousBoldCell = currentCell;
								        	        		
								        	        		Random random = new Random();
								        	        		
								        	        		for(int q=0;q<cellRate.size();q++) {
								        	        			
								        	        			if(cellRate.get(q).equals("")) {
																	emptycells.add(q); 
																	count++;
																	
							        	        				}
							        	        				else {
							        	        					sum += Double.parseDouble(cellRate.get(q));
							        	        				}
								        	        		}
								        	        		
								        	        		if(sum<1 && count==0 && cellRate.size()>0) {
							        	        				if(maybeError.size()>0) {
							        	        					
								        	        				errorCells[pos][0] = maybeError.get(maybeError.size()-1).toString();
								        	        				errorCells[pos][1] = "l";
								        	        				pos++;	
							        	        				}
							        	        				else if(maybeError.size() == 0){

								        	        				errorCells[pos][0] = maybeError.get(0).toString();
								        	        				errorCells[pos][1] = "l";
								        	        				pos++;	
							        	        				}
							        	        			}
							        	        			else if(sum>1 && count==0 && cellRate.size()>0) {
							        	        				if(maybeError.size()>0) {

								        	        				errorCells[pos][0] = maybeError.get(maybeError.size()-1).toString();
								        	        				errorCells[pos][1] = "g";
								        	        				pos++;	
							        	        				}
							        	        				else {

								        	        				errorCells[pos][0] = maybeError.get(0).toString();
								        	        				errorCells[pos][1] = "g";
								        	        				pos++;	
							        	        				}
							        	        			}
							        	        		
								        	        	
								        	        		if(sum<1 && count!=0) {
								        	        			for(int m=0; m<count-1; m++) 
									        	        		{
								        	        				
									        	        			nextRandom = random.nextDouble(1-sum);
									        	        			sum+=nextRandom;
									        	        			
									        	        			cellRate.set(emptycells.get(m),String.valueOf(nextRandom));
									        	        			
									        	        		}
								        	        			 
								        	        			cellRate.set(emptycells.get(count-1), String.valueOf(1-sum));
											        	        sum += 1-sum;
								        	        			
								        	        		}
								        	        		else if(sum>=1 && count!=0) {
								        	        			
								        	        			for(int m=0; m<count-1; m++) 
									        	        		{
									        	        			cellRate.set(emptycells.get(m),"0");
									        	        		}
								        	        			
								        	        			cellRate.set(emptycells.get(count-1), "0");
								        	        			
								        	        			if(maybeError.size()>0) {

								        	        				errorCells[pos][0] = maybeError.get(maybeError.size()-1).toString();
								        	        				errorCells[pos][1] = "g";
								        	        				pos++;	
							        	        				}
							        	        				else {

								        	        				errorCells[pos][0] = maybeError.get(0).toString();
								        	        				errorCells[pos][1] = "g";
								        	        				pos++;	
							        	        				}
								        	        		}
//								        	        	
								        	        		
								        	         		double sumcheck = 0;
								        	        		String rateFlow;
								        	        		
								        	        		for(String rate : cellRate) {
											                	sumcheck+= Double.parseDouble(rate);
											                }
								        	        		
								        	        		
								        	        		if(String.valueOf(sumcheck).equals("1.0")) {
								        	        			for(String rate : cellRate) {
								        	        				rateFlow= cellRateFlow.get(p);
								        	        				rates.put(rateFlow, rate);
								        	        				p++;
												                }
								        	        			
								        	        		}
								        	        		else{
									        	        		for (String rate : cellRate) {
								        	                        rateFlow = cellRateFlow.get(p);
								        	                        rates.put(rateFlow, "0");
								        	                        p++;
							        	                    	}
								        	        		}
								        	        		
								        	        		
								        	        		cellRate.clear();
								        	        		sum=0;
								        	        		count=0;
								        	        		emptycells.clear();
								        	        		enter=false;
								        	        		last=false;
							                             }
							                    	
							                        	
							                        }
							            
						                }
				               
				                
				        		}
		        			        
				        		break;
				        		
				        	case 9:
				        		//Ακολουθείται η ίδια διαδικασία με τους χρόνους και τα κόστοι των activities
				        		if(parseCellValue(cell) != "") {
			        		       
				        			String name = parseCellValue(cell);
				        			
				        			cellDurationIE = parseCellValue(row.getCell(11));
				        			cellCostIE = parseCellValue(row.getCell(12));
				        			
				        		    
								        if(cellDurationIE != "") {
								        	if(cellDurationIE.contains("-")) {
								        		
									        	String[] partsDurationIE = cellDurationIE.split("-");
									        	
									        	String floorNumberDurationIE = partsDurationIE[0];
							        		    String ceilNumberDurationIE = partsDurationIE[1];
							        		    
							        		    if(Double.parseDouble(floorNumberDurationIE) > Double.parseDouble(ceilNumberDurationIE)) {
							        		    	if(!wrongDATA.contains(cell)) {
							        		    		wrongDATA.add(cell);
							        		    	}
							        		    	cellDurationIE = "0-0";
							        		    }
							        		   
								        	}
								        	else {
								        		cellDurationIE = "0-0";
								        		if(!wrongDATA.contains(cell)) {
						        		    		wrongDATA.add(cell);
						        		    	}
								        	}
								        }
								        else {
								        cellDurationIE = "0-0";
								        }
								        
								        durationsIE.put(name,cellDurationIE);
								        
								        if(cellCostIE != "") {
								        	if(cellCostIE.contains("-")) {
							        			String[] partsCostIE = cellCostIE.split("-");
						        		       
						        		        String floorNumberCostIE = partsCostIE[0];
						        		        String ceilNumberCostIE = partsCostIE[1];
						        		        
						        		        if(Double.parseDouble(floorNumberCostIE) > Double.parseDouble(ceilNumberCostIE)) {
						        		        	if(!wrongDATA.contains(cell)) {
							        		    		wrongDATA.add(cell);
							        		    	}
						        		        	cellCostIE = "0-0";
							        		    }
									        	
									        }
								        	else {
								        		cellCostIE = "0-0";
								        		if(!wrongDATA.contains(cell)) {
						        		    		wrongDATA.add(cell);
						        		    	}
								        	}
								        }
								        else {
								        	cellCostIE = "0-0";
								        }
								        
								        costsIE.put(name,cellCostIE);
				        		}   	
				        		
				        		break;
			        	}
			        	
			        	
		        	}
		        	else {
		        		flag=false;
		        	}
		        	
	        		
		        }
		        else {
		        	flag=false;
		        }
		   i++;
		
        }
    }
   	
		  
    	//Για την εμφάνιση των δεδομένων του arraylist με τα κελιά με τα λάθος δεδομένα σε μορφή λίστας στην οθόνη
	    StringBuilder wrongDATAStringBuilder = new StringBuilder();
	    
	    wrongDATAStringBuilder.append("\n");
	    for (Cell element : wrongDATA) {
	    	wrongDATAStringBuilder.append("• ").append(element).append("\n"); //Διαχωρισμός κάθε στοιχείου της λίστα με τελείες
	    }
	    String wrongDataString = wrongDATAStringBuilder.toString();
	    
	    //Αν υπάρχουν λάθος δεδομένα εμφανίζει μήνυμα στην οθόνη με μία λίστα με τα id των activities ή events που έχουν τα λάθη
	    if(wrongDATA.size()>0) {
	    	JOptionPane.showConfirmDialog(null, "ΠΡΟΣΟΧΗ! Υπάρχουν λάθος δεδομένα.\n Συγκεκριμένα στα κελιά:" + wrongDataString, "Λάθος τιμές δεδομένων", JOptionPane.ERROR_MESSAGE);
	    }
	    
		for(int ec=0;ec<errorCells.length;ec++) {
			if(errorCells[ec][1]!= null && errorCells[ec][1].equals("g")) {
				greater.add(errorCells[ec][0]); //Λίστα με τα κελία των exclusive με παραπάνω από 100% ποσοστό
				
			}
			else if(errorCells[ec][1]!= null && errorCells[ec][1].equals("l")) {
				less.add(errorCells[ec][0]); //Λίστα με τα κελία των exclusive με λιγότερο από 100% ποσοστό
			}
			  
		}
		
		//Εμφάνιση και αυτών σε μορφή λίστας με τελείες
	    StringBuilder greaterStringBuilder = new StringBuilder();
	    
	    greaterStringBuilder.append("\n");
	    for (String element : greater) {
	        greaterStringBuilder.append("• ").append(element).append("\n");
	    }
	    
	    String greaterString = greaterStringBuilder.toString();
		
	    StringBuilder lessStringBuilder = new StringBuilder();
	    lessStringBuilder.append("\n");
	    for (String element : less) {
	        lessStringBuilder.append("• ").append(element).append("\n");
	    }
	    
	    String lessString = lessStringBuilder.toString();
	    
	    //Αν υπάρχουν exclusive με ποσοστά μεγαλύτερα του 100% εμφανίζει μήνυμα στην οθόνη με μία λίστα με τα id των flows που έχουν τα λάθη
		if(greater.size()!=0) {
			JOptionPane.showConfirmDialog(null, "ΠΡΟΣΟΧΗ! Τα ποσοστά που επιλέξατε ξεπερνούν το 100%\n Συγκεκριμένα στα Gateway Events:" + greaterString, "Λάθος τιμές ποσοστών", JOptionPane.ERROR_MESSAGE);
		}
		
		//Αν υπάρχουν exclusive με ποσοστά μικρότερα του 100% εμφανίζει μήνυμα στην οθόνη με μία λίστα με τα id των flows που έχουν τα λάθη
		if(less.size()!=0) {
			JOptionPane.showConfirmDialog(null, "ΠΡΟΣΟΧΗ! Τα ποσοστά που επιλέξατε είναι μικρότερα από 100%\n Συγκεκριμένα στα Gateway Events:" + lessString, "Λάθος τιμές ποσοστών", JOptionPane.ERROR_MESSAGE);
		}
	
   }
    
}
