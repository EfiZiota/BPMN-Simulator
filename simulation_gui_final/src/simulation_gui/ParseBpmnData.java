package simulation_gui;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


//Δημιουργία ενός interface ώστε να χειρίζονται τα lanes, τα αντικείμενα τύπου activities αλλά και τα βέλη των BPMN μοντέλων
interface BPMNElement {

}

//Κλάση που αναπαριστά αντικείμενα τύπου lane
class Participant implements BPMNElement{
	
	private String participantName;
	private String participantId;
	private String participantProcessRef;
	
	
	public Participant(String participantName, String participantId, String participantProcessRef) {
		this.participantName = participantName;
		this.participantId = participantId;
		this.participantProcessRef = participantProcessRef;
		
		
	}


	public String getParticipantName() {
		return participantName;
	}


	public String getParticipantId() {
		return participantId;
	}


	public String getParticipantProcessRef() {
		return participantProcessRef;
	}
	
	  @Override
		public String toString() {
			return "Participant [participantId=" + participantId + ", participantName="+ participantName + ", participantProcessRef=" + participantProcessRef + "\n";
		}
}

//Κλάση που αναπαριστά αντικείμενα τύπου βελών
class SequenceFlow implements  BPMNElement{
	
    private String flowName;
    private String flowTag;
    private String flowId;
    private String sourceRef;
    private String targetRef;
   
    
    public SequenceFlow(String flowTag,String flowName,String flowId, String sourceRef, String targetRef) {
    	this.flowTag = flowTag;
    	this.flowName = flowName;
    	this.flowId = flowId;
        this.sourceRef = sourceRef;
        this.targetRef = targetRef;
    }
    
    public String getFlowTag() {
    	return flowTag;
    }
    
    public String getFlowId() {
    	return flowId;
    }

    public String getFlowName() {
        return flowName;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    @Override
	public String toString() {
		return "SequenceFlow [flowId=" + flowId + ", flowName="+ flowName + ", flowTag=" + flowTag + ", sourceRef=" + sourceRef
				+ ", targetRef=" + targetRef + "\n";
	}

	public String getTargetRef() {
        return targetRef;
    }
     
}

//Κλάση που αναπαριστά αντικείμενα τύπου task
class BpmnTask  implements BPMNElement{
    private String taskName;
    private String taskTag;
    private String taskId;
    private List<String> incomingTags;
    private List<String> outgoingTags;
    private List<String> linkEventDefinitionTags;
    private List<String> terminateEventDefinitionTags;
    private List<String> sequenceFlows;
    private double durationSample, costSample;
    private boolean hasMessageFlow;
    
    
    public BpmnTask(String taskTag,String taskName,String taskId) {
    	this.taskTag = taskTag;
    	this.taskName = taskName;
    	this.taskId = taskId;
        this.incomingTags = new ArrayList<>();
        this.outgoingTags = new ArrayList<>();
        this.linkEventDefinitionTags = new ArrayList<>();
        this.terminateEventDefinitionTags = new ArrayList<>();
        this.sequenceFlows = new ArrayList<>();
       // this.hasMessageFlow = false;
    }
    
    public String getTaskTag() {
    	return taskTag;
    }
    
    public String getTaskId() {
    	return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public List<String> getIncomingTags() {
        return incomingTags;
    }

    @Override
	public String toString() {
		return "BpmnTask [taskId=" + taskId + ", taskName="+ taskName + ", taskTag=" + taskTag + ", incomingTags=" + incomingTags
				+ ", outgoingTags=" + outgoingTags + ", terminateEventDefinitionTags=" + terminateEventDefinitionTags + ", linkEventDefinitionTags=" + linkEventDefinitionTags + ", outgoingName=" + sequenceFlows + ", durationSample="+ durationSample + ", costSample="+ costSample + ", hasMessageFlow="+ hasMessageFlow +  "\n";
	}

	public List<String> getOutgoingTags() {
        return outgoingTags;
    }
    
    public List<String> getlinkEventDefinitionTags() {
        return linkEventDefinitionTags;
    }
    
    public List<String> getterminateEventDefinitionTags() {
        return terminateEventDefinitionTags;
    }
    
    public List<String> getSequenceFlows() {
        return sequenceFlows;
    }

    public double getdurationSample() {
    	return durationSample;
    }
    

	public void setdurationSample(double durationSample) {
		this.durationSample = durationSample;
	}

	public double getCostSample() {
		return costSample;
	}

	public void setCostSample(double costSample) {
		this.costSample = costSample;
	}
	

	public void sethasMessageFlow(boolean hasMessageFlow) {
		this.hasMessageFlow = hasMessageFlow;
	}
     
	
	public boolean hasMessageFlow() {
		return hasMessageFlow;
	}
     
}

public class ParseBpmnData extends BPMNFileAdder {
	
	private  BpmnTask first,firstmf;
	private  List<BpmnTask> firsts = new ArrayList<BpmnTask>();
	private  HashMap<String, BpmnTask> participantStart  = new HashMap<String, BpmnTask>();
	private  HashMap<String, BpmnTask> tasks  = new HashMap<String, BpmnTask>();
	private  List<BpmnTask> alltask = new ArrayList<BpmnTask>();
	private  List<SequenceFlow> flows = new ArrayList<SequenceFlow>();
	private  List<String> pathNames = new ArrayList<String>();
	private  List<SequenceFlow> messageflows = new ArrayList<SequenceFlow>();
	private  List<Participant> participants = new ArrayList<Participant>();
	private  List<BPMNElement> TasksInOrder = new ArrayList<BPMNElement>();
	private  List<BPMNElement> TasksInOrderMFV = new ArrayList<BPMNElement>();
	private  HashSet<BPMNElement> TasksVisited = new HashSet<BPMNElement>();
	private  List<BPMNElement> gatewaylist = new ArrayList<BPMNElement>();
	private Map<String, List<BPMNElement>> pathsDurations = new HashMap<>();
	private int time=1;
	private boolean conditionMet=false;
	private String selectedParticipant;
	private boolean exists =false;
	
	
	
		
	 public ParseBpmnData(String afilePath, String aselectedParticipant) {
	        super(afilePath);
	        this.selectedParticipant = aselectedParticipant;
	        initialize();
	    }
    public List<BPMNElement> getTasksInOrder() {
		return TasksInOrder;
	}

    
    
    public List<BPMNElement> getTasksInOrderMFV() {
		return TasksInOrderMFV;
	}

	public Map<String, List<BPMNElement>> getPathsDurations() {
		return pathsDurations;
	}



	private void initialize() {
      
	
	
        try {
        	
            File bpmnFile = new File(filePath);
            SAXReader reader = new SAXReader();
            Document document = reader.read(bpmnFile);

            parseBpmnNew(document);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
      
      	
    }         
             
    private  void parseBpmnNew(Document document) {

        Element definitions = document.getRootElement();

        //Κρατούνται στις αντίστοιχες λίστες όλα τα στοιχεία των process & collaboration tags
          List<Element> processElements = definitions.elements("process");
          List<Element> collaborationElements = definitions.elements("collaboration");
          
          
          for (Element collaborationElement : collaborationElements) {
        	//Δίνονται όλα τα στοιχεία που βρίσκονται στο collaboration tag για να κατανεμηθούν
               parseTasksNew(collaborationElement);
           }
          
          for (Element processElement : processElements) {
              
        	  for(Participant participant : participants) {
        		  if(processElement.attributeValue("id").equals(participant.getParticipantProcessRef())) {
        			  TasksInOrder.add(participant);//Εισαγωγή στην αρχή του συμμετέχοντα για τον οποίο ξεκινάει το μονοπάτι
        			  
        		  }
        	  }
        	  
        	  //Δίνονται όλα τα στοιχεία που βρίσκονται στο process tag για να κατανεμηθούν
        	  parseTasksNew(processElement);
        	  
        	  //Ξεκινάει η δημιουργία μίας λίστας με τα στοιχεία του μοντέλου στη σειρά με αρχή το πρώτο Start Event με τη συνάρτηση bpmnpath
        	  //Διότι στο xml αρχείο δεν ακολουθείται κάποια σειρά
              bpmnpath(first);
            
          }
          
          TasksVisited.clear(); //Καθαρισμός της λίστας TasksVisited για χρήση μετέπειτα
          
        
          List<String> keysToModify = new ArrayList<>();
          Map<String, String> newKeys = new HashMap<>();

          //Έλεγχος στο collaboration tag για να βρεθούν για κάθε id των processes τα name τους
          for (Element collaborationElement : collaborationElements) {
              for (Element taskElement : collaborationElement.elements("participant")) {
                  String processRef = taskElement.attributeValue("processRef");
                  String name = taskElement.attributeValue("name");

                  for (Map.Entry<String, BpmnTask> entry : participantStart.entrySet()) {
                      if (entry.getKey().equals(processRef)) {
                          keysToModify.add(processRef);//Κρατάμε στη λίστα keysToModify τα id των process 
                          newKeys.put(processRef, name);//Κρατάμε στo hashmap newKeys το id του process και το όνομα του
                      }
                  }
              }
          }

         
          //Αλλαγή στο Hashmap participantStart των keys από id στα names των participants
          for (String oldKey : keysToModify) {
              BpmnTask value = participantStart.remove(oldKey); //Διαγραφή του παλιού key
              String newKey = newKeys.get(oldKey);
              participantStart.put(newKey, value); //Προσθήκη νέου key στο hashmap
          }
        	
          //Βρίσκουμε το key του hashmap participantStart που αντιστοιχεί στο επιλεγμένο participant από τον χρήστη
          for (Map.Entry<String, BpmnTask> entry : participantStart.entrySet()) {
              if (entry.getKey().equals(selectedParticipant)) {
            	  exists =true;
            	  firstmf = entry.getValue(); //αν υπάρχει τίθεται ως firstmf το value του hashmap participantStart που είναι το startEvent του συγκεκριμένου συμμετέχοντα
              }
          }
          
          if(exists) {
        	  //Αν έχει βρεθεί χρησιμοποιείται σαν όρισμα στις συναρτήσεις findEnd & participantPath για να βρεθεί το μονοπάτι του συμμετέχοντα
              BpmnTask end = findEnd(firstmf, new HashSet<>());  //Συνάρτηση που βρίσκει και κρατάει το endEvent του επιλεγμένου συμμετέχοντα 
              participantPath(firstmf, end); //Συνάρτηση που βρίσκει το μονοπάτι του επιλεγμένου συμμετέχοντα  
          }
          else { //αν δεν υπάρχουν συμμετέχοντες χρησιμοποιείται σαν όρισμα στην συνάρτηση participantPath το startEvent όλου του μοντέλου για να βρεθεί το μονοπάτι του συμμετέχοντα
        	  BpmnTask end = findEnd(first, new HashSet<>());//Συνάρτηση που βρίσκει και κρατάει το endEvent του μοντέλου
              participantPath(first, end); //Συνάρτηση που βρίσκει το μονοπάτι του μοντέλου 
          }
        
       
    }
    
    //Συνάρτηση που βρίσκει το μονοπάτι ενός συμμετέχοντα μέσα στο μοντέλο με χρήση της αναδρομής
    private void participantPath(BpmnTask current, BpmnTask end) {
    	
    	Boolean flag=true;
    	
    	//Αποθήκευση του current task στη λίστα TasksInOrderMFV & ενημέρωση του hashset TasksVisited για να μην ξαναβάλει στη λίστα το ίδιο task
    	if(!(current.getTaskTag().equals("exclusiveGateway") && current.getOutgoingTags().size()>1)) {//Χρησιμοποιείται αυτή η συνθήκη γιατί τα exclusive προστίθονται σε άλλο σημείο
	       	TasksInOrderMFV.add(current);
	       	TasksVisited.add(current);
    	}
    	
    	//Εάν το μονοπάτι φτάσει σε endEvent τότε σταματάει η συνάρτηση
    	if(current.getTaskTag().equals("endEvent") && current.getterminateEventDefinitionTags().isEmpty()) {
    		conditionMet = true;
    		return;
    	}
    	
    	
    	//Για όλα τα βέλη που εξέρχονται από ένα task
    	for(String out: ((BpmnTask) current).getOutgoingTags()) {
    		
    		//Το βέλος που εξέρχεται από το current task είναι το εισερχόμενο του επόμενου task στη σειρά
    		//με τη χρήση του hashmap tasks που έχει αποθηκευμένο για κάθε εισερχόμενο βέλος το task του βρίσκουμε το επόμενο task
    		BpmnTask next = tasks.get(out);
    		
    		//Έλεγχος όταν βρίσκεται σε exclusive gateway ώστε να προσθέτει όλα του τα μονοπάτια πριν συνεχίσει εκτός αυτού
    		if(!next.getIncomingTags().get(next.getIncomingTags().size()-1).equals(out)) continue; 
    		
        	//Εάν το current task έχει κάποιο messageflow
        	if(current.hasMessageFlow()) {
        		for(SequenceFlow flow: messageflows) {
        			if(flow.getSourceRef().equals(current.getTaskId())) {
        				for(BpmnTask task: alltask) {
        					if(task.getTaskId().equals(flow.getTargetRef())) {
        						//ελέγχεται αν το task στο οποίο καταλήγει το messageflow και τα μετέπειτα αυτού 
        						//καταλήγουν στο endEvent του επιλεγμένου συμμετέχοντα
        						if(canReach(task, end, new HashSet<>())) {
	        						next = task; //αν καταλήγει στο σωστό endEvent τότε τίθεται ως next task αυτό στο οποίο καταλήγει το messageflow
	            					break;
        						}
        					} 
        				}	
        			}	
        		}
        	}
        	
        	//Έαν το next task είναι exclusiveGateway
    		if(next.getTaskTag().equals("exclusiveGateway")) {
    			
    			//Γίνεται έλεγχος της λίστας flows
    			for(SequenceFlow flow : flows) {
    				//Έαν τα βέλη που εξέρχονται από το split exclusiveGateway έχουν κάποια ονομασία 
    				if(next.getOutgoingTags().contains(flow.getFlowId()) && flow.getFlowName()!=(null) && next.getOutgoingTags().size()>1){
    					if(flag==true && !TasksVisited.contains(next)) {
    						TasksInOrderMFV.add(next);//αποθηκεύεται το exclusiveGateway στη λίστα TasksInOrderMFV
    						TasksVisited.add(next);
    						flag=false;
    					}
    					if(!TasksVisited.contains(flow)) {
	    					TasksInOrderMFV.add(flow); //αποθηκεύεται το βέλος στη λίστα TasksInOrderMFV
	    					TasksVisited.add(flow);
    					}
    				}
    			}
    					
    		}
    		
    		//Εάν το next task είναι linkEventDefinition
    		if(!next.getlinkEventDefinitionTags().isEmpty()) {
    			for(BpmnTask task: alltask) {
    				//Βρίσκεται το linkEventDefinition που συνδέεται με αυτό για να συνεχιστεί από εκεί η διαδικασία
    				if(!task.getlinkEventDefinitionTags().isEmpty() && !task.equals(next) && task.getTaskName().equals(next.getTaskName())) {
    					participantPath(next, end);//Αναδρομή της συνάρτησης για το επόμενο task
    					next = task; 
    					break;
    				}
    			}
    		}

    		//if(!conditionMet) {
    			participantPath(next, end);//Αναδρομή της συνάρτησης για το επόμενο task
    		//
        }
    }
    
    //Συνάρτηση που βρίσκει το endEvent ενός συμμετέχοντα
    private BpmnTask findEnd(BpmnTask current, HashSet<BpmnTask> visited) {
    	
    	//αν βρεθεί το endEvent επιστρέφεται
    	if(current.getTaskTag().equals("endEvent")) {
    		return current;
    	}
    	
		List<BpmnTask> allEnds = new ArrayList<BpmnTask>();
		
		//Για όλα τα βέλη που εξέρχονται από ένα task
    	for(String out: ((BpmnTask) current).getOutgoingTags()) {
    		
    		//Βρίσκεται το επόμενο task στη σειρά μέσω του εξερχόμενου βέλους του current task
    		BpmnTask next = tasks.get(out);
    		
    		//Έλεγχος όταν βρίσκεται σε exclusive gateway ώστε να προσθέτει όλα του τα μονοπάτια πριν συνεχίσει εκτός αυτού
    		if(!next.getIncomingTags().get(next.getIncomingTags().size()-1).equals(out))continue;

    		//Εάν το next task είναι linkEventDefinition
    		if(!next.getlinkEventDefinitionTags().isEmpty()) {
    			for(BpmnTask task: alltask) {
    				//Βρίσκεται το linkEventDefinition που συνδέεται με αυτό για να συνεχιστεί από εκεί η διαδικασία
    				if(!task.getlinkEventDefinitionTags().isEmpty() && !task.equals(next) && task.getTaskName().equals(next.getTaskName())) {
    					next = task;
    					break;
    				}
    			}
    		}
    		
    		
    		if(visited.contains(next))
    			continue;
    		
    		visited.add(next);//Προστίθεται το next στα task που έχουν ήδη επισκεφθεί 
    		allEnds.add(findEnd(next, visited));//Προστίθεται στη λίστα allEnds το endEvent που επιστρέφει η συνάρτηση findEnd
    		
        }
    	
    	for(BpmnTask task: allEnds) {
			if(task!=null && task.getterminateEventDefinitionTags().isEmpty()) {
    			return task; //Βρίσκουμε στο allEnds το endEvent και το επιστρέφουμε
			}
		}
    	
    	return null;
    }

    //Συνάρτηση που επιστρέφει true ή false ανάλογα αν το επιλεγμένο μονοπάτι καταλήγει στο σωστό endEvent
    private boolean canReach(BpmnTask current, BpmnTask end, HashSet<BpmnTask> visited) {
    	if(current.getTaskTag().equals("endEvent")) {
    		return current.equals(end);
    	}
    	
    	//Για όλα τα βέλη που εξέρχονται από ένα task
    	for(String out: ((BpmnTask) current).getOutgoingTags()) {
    		
    		//Βρίσκεται το επόμενο task στη σειρά μέσω του εξερχόμενου βέλους του current task
    		BpmnTask next = tasks.get(out);
    		
    		//Έλεγχος όταν βρίσκεται σε exclusive gateway ώστε να προσθέτει όλα του τα μονοπάτια πριν συνεχίσει εκτός αυτού
    		if(!next.getIncomingTags().get(next.getIncomingTags().size()-1).equals(out))continue;

    		//Εάν το next task είναι linkEventDefinition
    		if(!next.getlinkEventDefinitionTags().isEmpty()) {
    			for(BpmnTask task: alltask) {
    				//Βρίσκεται το linkEventDefinition που συνδέεται με αυτό για να συνεχιστεί από εκεί η διαδικασία
    				if(!task.getlinkEventDefinitionTags().isEmpty() && !task.equals(next) && task.getTaskName().equals(next.getTaskName())) {
    					next = task;
    					break;
    				}
    			}
    		}
    		
    		
    		if(visited.contains(next))
    			continue;
    		
    		visited.add(next);//Προστίθεται το next στα task που έχουν ήδη επισκεφθεί 
    		
    		if(canReach(next, end, visited)) //Γίνεται αναδρομή της συνάρτησης
    			return true;
    		
    		//Αν το current task έχει message flow
        	if(current.hasMessageFlow()) {
        		for(SequenceFlow flow: messageflows) {
        			if(flow.getSourceRef().equals(current.getTaskId())) {
        				for(BpmnTask task: alltask) {
        					if(task.getTaskId().equals(flow.getTargetRef())) {
        						if(canReach(task, end, visited)) //Γίνεται αναδρομή της συνάρτησης για να ελεγχθεί αν το μονοπάτι του messageflow καταλήγει στο σωστό endEvent
        			    			return true;
            					break;
        					}
        				}	
        			}	
        		}
        	}

        }
    	return false;
    }

   
    
   
    //Συνάρτηση που αποθηκεύει σε Hashmap κάθε μονοπάτι ενός exclusive gateway με τη χρήση της αναδρομής
    private void exclusivepath(BPMNElement current) {
	
    	//Έλεγχος ώστε αν προκύπτει κάποιο parallelGateway μέσα στο μονοπάτι να προστίθεται όλα του τα μονοπάτια στο βασικό μονοπάτι
    	if(((BpmnTask) current).getTaskTag().equals("parallelGateway") && ((BpmnTask) current).getOutgoingTags().size()>1) {
    		
    		for(String out: ((BpmnTask) current).getOutgoingTags()) {
    			
    			//Βρίσκεται το επόμενο task στη σειρά μέσω του εξερχόμενου βέλους του current task
    			BpmnTask next = tasks.get(out);
    			
    			//Έλεγχος όταν βρίσκεται σε exclusive gateway ώστε να προσθέτει όλα του τα μονοπάτια πριν συνεχίσει εκτός αυτού
    			if(!next.getIncomingTags().get(next.getIncomingTags().size()-1).equals(out)) continue;
    			
    			
    			if(!gatewaylist.contains(next)) {
    				gatewaylist.add(next);
    				
    			}
    			exclusivepath(next);
    		}
    	}
    	
    	//Για όλα τα βέλη που εξέρχονται από το current
    	for(String out: ((BpmnTask) current).getOutgoingTags()) {
	    
	    		//krataei onomata tvn belvn poy jekinaei καθε path toy exclusive
    			
	    		if(((BpmnTask) current).getTaskTag().equals("exclusiveGateway") && ((BpmnTask) current).getOutgoingTags().size()>1){
	    			if(!pathNames.contains(out)) {
	    				pathNames.add(out);//Εισάγονται στο pathNames τα βέλη που αντιστοιχούν στα μονοπάτια ενός split exclusive gateway
	    			}
	    		}
	    		  
	    		BpmnTask next = tasks.get(out);
	    		
	    		if(!next.getIncomingTags().get(next.getIncomingTags().size()-1).equals(out)) continue;
	    		
	    		
	    		if(next.getTaskTag().equals("exclusiveGateway") &&  next.getIncomingTags().size()>1) {
	    			break;
	    		}
	    		else {
	    			if(!gatewaylist.contains(next)) {
	    				gatewaylist.add(next);//Προστίθενται στη λίστα gatewaylist τα task που υπάρχουν στο κάθε μονοπάτι 
	    				
	    			}
	    			exclusivepath(next);//Αναδρομή της συνάρτησης με το επόμενο task 
	    			
	    		}
	    	
	    		//Συνάρτηση με την οποία δημιουργείται hashmap για κάθε μονοπάτι ενός exclusive gateway
	    		createHashMap(pathNames,gatewaylist);
	    		gatewaylist.clear();//Καθαρισμός λίστα για το επόμενο μονοπάτι
    	}
    	
    }
    
    
    
    //Συνάρτηση που βρίσκει το συνολικό μονοπάτι του μοντέλου bpmn με χρήση της αναδρομής
    private  void bpmnpath(BPMNElement current) {
    	
    	Boolean flag=true;
    	
    	if(!TasksVisited.contains(current)) {
	       	TasksInOrder.add(current);
	       	TasksVisited.add(current);
    	}
    	
    	//Εάν το current task είναι exclusiveGateway καλείται η συνάρτηση exclusivepath για να βρεθούν τα μονοπάτια του
    	if(((BpmnTask) current).getTaskTag().equals("exclusiveGateway") && ((BpmnTask) current).getOutgoingTags().size()>1){
    		exclusivepath(current);
    	}
    	

    
    	//Για όλα τα βέλη που εξέρχονται από ένα task
    	for(String out: ((BpmnTask) current).getOutgoingTags()) {
    		
    		//Βρίσκεται το επόμενο task στη σειρά μέσω του εξερχόμενου βέλους του current task
    		BpmnTask next = tasks.get(out);
    		
    		//Έλεγχος όταν βρίσκεται σε exclusive gateway ώστε να προσθέτει όλα του τα μονοπάτια πριν συνεχίσει εκτός αυτού
    		if(!next.getIncomingTags().get(next.getIncomingTags().size()-1).equals(out)) continue;
    		
    		
    		//Έαν το next task είναι exclusiveGateway
    		if(next.getTaskTag().equals("exclusiveGateway")) {
    			//Γίνεται έλεγχος της λίστας flows
    			for(SequenceFlow flow : flows) {

    				//Έαν τα βέλη που εξέρχονται από το split exclusiveGateway έχουν κάποια ονομασία 
    				if(next.getOutgoingTags().contains(flow.getFlowId()) && flow.getFlowName()!=(null) && next.getOutgoingTags().size()>1){
    				
    					if(flag==true && !TasksVisited.contains(next)) {
    						TasksInOrder.add(next);//αποθηκεύεται το exclusiveGateway στη λίστα TasksInOrder
    						TasksVisited.add(next);
    						flag=false;
    					}
    					if(!TasksVisited.contains(flow)) {
	    					TasksInOrder.add(flow); //αποθηκεύεται το βέλος στη λίστα TasksInOrder
	    					TasksVisited.add(flow);
    					}
    				}
    			}
    					
    		}
    		
    		
    		//Εάν το next task είναι linkEventDefinition
    		if(!next.getlinkEventDefinitionTags().isEmpty()) {
    			for(BpmnTask task: alltask) {
    				//Βρίσκεται το linkEventDefinition που συνδέεται με αυτό για να συνεχιστεί από εκεί η διαδικασία
    				if(!task.getlinkEventDefinitionTags().isEmpty() && !task.equals(next) && task.getTaskName().equals(next.getTaskName())) {
    					bpmnpath(next);
    					next = task;
    					break;
    				}
    			}
    		}

    		
    		bpmnpath(next);//Αναδρομή της συνάρτησης για το επόμενο task
    	}
    	
    	
    	
    }
    
    
    private void createHashMap(List<String> pathNames, List<BPMNElement> gatewaylist) {
   	
		for(String name : pathNames) {
			for(BPMNElement gateway : gatewaylist) {
				for(int i=0;i<((BpmnTask)gateway).getIncomingTags().size();i++) {
					if(name.equals(((BpmnTask)gateway).getIncomingTags().get(i))) {
						
	                    List<BPMNElement> currentList = new ArrayList<>(gatewaylist);
	                    
	                    //Εισαγωγή στο hashmap pathsDurations ως key του id κάθε βέλους που εξέρχεται από ένα split exclusive gateway
						//και ως value την λίστα με το μονοπάτι που ακολουθείται από αυτό το βέλος
						pathsDurations.put(name,currentList);
						
						break;
							
					}
				}
				
			}
			
			
		}
		
		
	}

    
	private  void parseTasksNew(Element processElement) {
		
		//Αποθήκευση των ονομάτων κάθε συμβόλου της bpmn για να γίνεται η αναγνώριση κάθε στοιχείου processElement που δίνεται στη συνάρτηση
		String Bpmntasks[] = { "bpmn:task", "bpmn:receiveTask", "bpmn:manualTask", "bpmn:userTask", "bpmn:serviceTask",
				"bpmn:sendTask", "bpmn:businessRuleTask", "bpmn:scriptTask","bpmn:callActivity","bpmn:subProcess",
				"bpmn:exclusiveGateway","bpmn:parallelGateway", "bpmn:inclusiveGateway","bpmn:complexGateway","bpmn:eventBasedGateway",
				"startEvent", "task", "receiveTask", "manualTask", "userTask", "serviceTask", "sendTask", "businessRuleTask", "scriptTask", "endEvent",
				"intermediateThrowEvent", "intermediateCatchEvent","callActivity","subProcess",
				"exclusiveGateway","parallelGateway","inclusiveGateway","complexGateway","eventBasedGateway","sequenceFlow","messageFlow","bpmn:messageFlow","bpmn:sequenceFlow","participant"};

		
		int number = 1;
		
		for (int i = 0; i < Bpmntasks.length; i++) {
			
			for (Element taskElement : processElement.elements(Bpmntasks[i])) {
				//System.out.println(taskElement);
				
				//Αν το εισαγώμενο στοιχείο είναι τύπου βέλους
				if(Bpmntasks[i].equals("sequenceFlow") || Bpmntasks[i].equals("bpmn:sequenceFlow")) {
					  
					//Θέτονται όλα τα χαρακτηριστικά της αντίστοιχης κλάσης SequenceFlow
					String flowTag = taskElement.getName();
					String flowName = taskElement.attributeValue("name");
					String flowId = taskElement.attributeValue("id");
					String sourceRef = taskElement.attributeValue("sourceRef");
					String targetRef = taskElement.attributeValue("targetRef");

					//Στην περίπτωση που το όνομα του βέλους δνε έχει οριστεί από τον χρήστη στα gateway events
					//τότε του δίνεται η ονομασία "Επιλογή"
					if(flowName == null && sourceRef.startsWith("Gateway")) {
						flowName = "Eπιλογή ";
					}
					
					//Δημιουργείται αντικείμενο τύπου SequenceFlow, με τα χαρακτηριστικά του στοιχείου της XML που αποθηκεύτηκαν παραπάνω
					SequenceFlow flow = new SequenceFlow(flowTag, flowName,flowId, sourceRef ,targetRef);

				
					//Αποθήκευση των αντικειμένων που δημιουργούνται στη λίστα flows
					flows.add(flow);
					
				}
				else if(Bpmntasks[i].equals("messageFlow") || Bpmntasks[i].equals("bpmn:messageFlow")) {//Αν το εισαγώμενο στοιχείο είναι τύπου βέλους message
					  
					//Ακολουθείται η ίδια διαδικασία με τα απλά βέλη απλά τα αντικείμενα που δημιουργούνται αποθηκεύονται στη λίστα messageflows
					
					String flowTag = taskElement.getName();
					String flowName = taskElement.attributeValue("name");
					String flowId = taskElement.attributeValue("id");
					String sourceRef = taskElement.attributeValue("sourceRef");
					String targetRef = taskElement.attributeValue("targetRef");

					SequenceFlow flow = new SequenceFlow(flowTag, flowName,flowId, sourceRef ,targetRef);

					
					messageflows.add(flow);
					
				}
				else if(Bpmntasks[i].equals("participant") ){//Αν το εισαγώμενο στοιχείο είναι τύπου συμμετέχοντα
					
					//Θέτονται όλα τα χαρακτηριστικά της αντίστοιχης κλάσης Participant
					String participantId = taskElement.attributeValue("id");
					String participantName = taskElement.attributeValue("name");
					String participantProcessRef = taskElement.attributeValue("processRef");
					
					//Δημιουργείται αντικείμενο τύπου Participant, με τα χαρακτηριστικά του στοιχείου της XML που αποθηκεύτηκαν παραπάνω
					Participant participant = new Participant(participantName,participantId,participantProcessRef);
					
					//Αποθήκευση των αντικειμένων που δημιουργούνται στη λίστα participants
					participants.add(participant);
					
				}
				else { //Αν το εισαγώμενο στοιχείο είναι κάποιου τύπου task
					
					
					//Θέτονται τα βασικά χαρακτηριστικά της αντίστοιχης κλάσης BpmnTask που αντιστοιχούν σε όλους τους τύπους task
					String taskTag = taskElement.getName();
					String taskName = taskElement.attributeValue("name");
					String taskId = taskElement.attributeValue("id");
	
					BpmnTask task = new BpmnTask(taskTag, taskName,taskId);
	
					
					
					// Parse linkEventDefinition tags
					//Για τα στοιχεία τα οποία διαθέτουν linkEventDefinition tag στο XML 
					for (Element incomingElement : taskElement.elements("linkEventDefinition")) {
						task.getlinkEventDefinitionTags().add(incomingElement.attributeValue("id"));//Θέτουμε στο αντίστοιχο χαρακτηριστικό του BpmnTask το id του linkEventDefinition
					}
					
					// Parse terminateEventDefinition tags
					//Για τα στοιχεία τα οποία διαθέτουν terminateEventDefinition tag στο XML 
					for (Element incomingElement : taskElement.elements("terminateEventDefinition")) {
						task.getterminateEventDefinitionTags().add(incomingElement.attributeValue("id"));//Θέτουμε στο αντίστοιχο χαρακτηριστικό του BpmnTask το id του terminateEventDefinition
					}
	
					// Parse incoming tags
					//Για τα στοιχεία τα οποία διαθέτουν incoming tag στο XML 
					for (Element incomingElement : taskElement.elements("incoming")) {
						task.getIncomingTags().add(incomingElement.getText()); //Θέτουμε στο αντίστοιχο χαρακτηριστικό του BpmnTask το κείμενο του incoming tag 
						tasks.put(incomingElement.getText(), task);//Εισάγουμε στο Hashmap tasks ως Key το κείμενο του incoming tag και ως value το task στο οποίο αντιστοιχεί το tag
					}
	
					// Parse outgoing tags
					//Για τα στοιχεία τα οποία διαθέτουν outgoing tag στο XML 
					for (Element outgoingElement : taskElement.elements("outgoing")) {
						task.getOutgoingTags().add(outgoingElement.getText());	//Θέτουμε στο αντίστοιχο χαρακτηριστικό του BpmnTask το κείμενο του outgoing tag 
					}
					
					//Για τα στοιχεία τα οποία διαθέτουν startEvent tag στο XML 
					if (task.getTaskTag().equals("startEvent")) {
						first = task; //θέτουμε το συγκεκριμένο task στην first
						firsts.add(task); //θέτουμε όλα τα startΕvent της διαδικασίας στη λίστα firsts
						if(processElement.attributeValue("id").startsWith("Process")) {
							//Εισάγουμε στο Hashmap tasks ως Key το id του συμμετέχοντα και ως value το startΕvent task το οποίο εμπεριέχεται στο εξής tag
							participantStart.put(processElement.attributeValue("id"), task);
						}
					
					}
						
					//Κρατάει μόνο το πρώτο startEvent στη μεταβλητή firstmf
					if (task.getTaskTag().equals("startEvent") && time==1) {
						firstmf = task;
						time++;
					}
						
	
					alltask.add(task);//Όλα τα task που δημιουργούνται αποθηκεύονται στη λίστα alltask
				}
			}
		}
		
		// Έλεγχος για το ποια tasks διαθέτουν message flows
        for(BpmnTask task: alltask) {
 	    	for(SequenceFlow flow : messageflows) {
 	    		if(flow.getSourceRef().equals(task.getTaskId())) {
 	    			task.sethasMessageFlow(true); //Θέτουμε το αντίστοιχο χαρακτηριστικό στη κλάση BpmnTask ως true
 	    		}
 	    	}
        }

	}


}
