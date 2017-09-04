import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;


public class Interface_newModel {
	private String algo;
	private String featSet;

	public Interface_newModel() throws Exception{
		JFrame window1 = new JFrame();
	    window1.setTitle("CROC - Coreference Resolution for Oral Corpora");
	    window1.setSize(600, 200);
	    window1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    window1.setLocationRelativeTo(null);
	    window1.setVisible(true);
	    
	    this.LearningInstances(window1);
	}
	
	public void Interface_Algo() throws Exception{
		final JFrame window2 = new JFrame();
	    window2.setTitle("CROC - Coreference Resolution for Oral Corpora");
	    window2.setSize(600, 200);
	    window2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    window2.setLocationRelativeTo(null);
	    JPanel top = new JPanel();
	    top.setBorder(new LineBorder(Color.BLACK));
	    JLabel jlabel = new JLabel("Sélectionner un algorithme de calcul");
	    jlabel.setFont(new Font("Dialog", 1, 15));
	    top.setSize(50,50);
	    top.add(jlabel);
	    window2.add(top,BorderLayout.NORTH);
	    window2.setLayout(new GridLayout(2, 1));
	    JPanel test = new JPanel();
	    test.setLayout(new GridLayout(1, 3));
	    JButton choice1 = new JButton("SVM");
	    JButton choice2 = new JButton("J48");
	    JButton choice3 = new JButton("NaiveBayes");
	  
	    
	    choice1.addActionListener(new ActionListener(){
	        public void actionPerformed(ActionEvent event){
	        	SetAlgo("SVM");
	        	window2.dispose();
	        	Interface_featureSet();
	        }
	      });
	    
	    choice2.addActionListener(new ActionListener(){
	        public void actionPerformed(ActionEvent event){
	        	SetAlgo("J48");
	        	window2.dispose();
	        	Interface_featureSet();
	        }
	      });
	    
	    choice3.addActionListener(new ActionListener(){
	        public void actionPerformed(ActionEvent event){
	        	SetAlgo("NaiveBayes");
	        	window2.dispose();
	        	Interface_featureSet();
	        }
	      });
	    
	    test.add(choice1);
	    test.add(choice2);
	    test.add(choice3);
	    window2.add(test);
	    window2.setVisible(true);
	}
	
	public void Interface_featureSet(){
			final JFrame window3  = new JFrame();
		 	window3.setTitle("CROC - Coreference Resolution for Oral Corpora");
		    window3.setSize(600, 200);
		    window3.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    window3.setLocationRelativeTo(null);
		    window3.setVisible(true);
		    JPanel top = new JPanel();
		    top.setBorder(new LineBorder(Color.BLACK));
		    JLabel jlabel = new JLabel("Sélectionner un ensemble d'attributs");
		    jlabel.setFont(new Font("Dialog", 1, 15));
		    top.setSize(50,50);
		    top.add(jlabel);
		    window3.add(top,BorderLayout.NORTH);
		    window3.setLayout(new GridLayout(2, 1));
		    JPanel test = new JPanel();
		    test.setLayout(new GridLayout(2, 2));
		    JButton choice1 = new JButton("AllFeatures");
		    JButton choice2 = new JButton("NotOralFeatures");
		    JButton choice3 = new JButton("RelationalFeatures");
		    JButton choice4 = new JButton("Autre ensemble");
		    
		    choice1.addActionListener(new ActionListener(){
		        public void actionPerformed(ActionEvent event){
		        	SetFeatureSet("allFeatures");
		        	try {
						GenerateModel();
						window3.dispose();
					}
		        	catch (Exception e) {e.printStackTrace();}
		        }
		      });
		    
		    choice2.addActionListener(new ActionListener(){
		        public void actionPerformed(ActionEvent event){
		        	SetFeatureSet("notOralFeatures");
		        	try {
						GenerateModel();
						window3.dispose();
					}
		        	catch (Exception e) {e.printStackTrace();}
		        }
		      });
		    choice3.addActionListener(new ActionListener(){
		        public void actionPerformed(ActionEvent event){
		        	SetFeatureSet("relationalFeatures");
		        	try {
						GenerateModel();
						window3.dispose();
					}
		        	catch (Exception e) {e.printStackTrace();}
		        }
		      });
		    choice4.addActionListener(new ActionListener(){
		        public void actionPerformed(ActionEvent event){
		        	if (ChooseFile(window3)==1){
		        		window3.dispose();
		        		Interface_featureSet();
		        	}
		        	else{
			        	try {
							GenerateModel();
							window3.dispose();
						}
			        	catch (Exception e) {e.printStackTrace();}
		        	}	
		        }
		      });
		    
		    test.add(choice1);
		    test.add(choice2);
		    test.add(choice3);
		    test.add(choice4);
		    window3.add(test);
	}
	
	public void LearningInstances(JFrame window) throws Exception{
		File f = new File("learningData/learningData.arff");
		final File fBis = f;
		final JFrame windowBis = window;
		if (f.exists()){
			//-------------------- 2-a) Si oui, on demande à l'uilisateur s'il souhaite l'écraser ou le conserver -------------------
			String keepInstancesFile = this.Verif(window);
			if (!keepInstancesFile.equals("stop")){
				//-------------------- 2-a-i) S'il l'écrase, on lance la création du nouveau fichier -------------------
				if(keepInstancesFile.equals("non")){
					String confirm = this.VerifDelete();
					if (confirm.equals("continuer")){
						new Thread(new Runnable() {
							public void run() {
							   try {
						    fBis.delete();
							JPanel jpan = new JPanel();
							jpan.setBorder(new LineBorder(Color.BLACK));
							JLabel text = new JLabel("Génération du fichier d'exemples annotés...");
							jpan.add(text);
							windowBis.add(jpan, BorderLayout.CENTER);
							new PairSelection("learning", "big", "learningData");
							windowBis.dispose();
							Interface_Algo();
							   } catch (Exception e) {e.printStackTrace();}
							}
						}
						).start();
					}
				}
			}
			else{
				window.dispose();
				Interface_Algo();
			}
		}
		else{
			//-------------------- 2-b) Si non, on lance la création du fichier -------------------
			new Thread(new Runnable() {
				public void run() {
				   try {
						JPanel jpan = new JPanel();
						jpan.setBorder(new LineBorder(Color.BLACK));
						JLabel text = new JLabel("Génération du fichier d'exemples annotés...");
						jpan.add(text);
						windowBis.add(jpan, BorderLayout.CENTER);
						new PairSelection("learning", "big", "learningData");
						windowBis.dispose();
						Interface_Algo();
				   } catch (Exception e) {e.printStackTrace();}
				}
			}).start();
		}
	}
	
	public String Verif(JFrame window){
		String retour = "";        
		String[] choice = {"Oui", "Non"};
		int option = JOptionPane.showOptionDialog(window, "Le corpus d'apprentissage \"learningData.arff\" existe déjà\n            Souhaitez-vous continuer avec celui-ci ?",
				"CROC - Instances d'aprentissage", JOptionPane.OK_CANCEL_OPTION, 2, null, choice, choice[0]);
		if(option == 0){
			retour = "oui";
		}
		if(option == 1){
			retour = "non";
		}
		if (option == 0){
			retour = "stop";
		}
		return retour;
	}
	
	public String VerifDelete(){
		String retour = "";
		String[] choice = {"Continuer", "Annuler"};
		int option = JOptionPane.showOptionDialog(null, "Attention, cette action supprimera définitivement l'ancien fichier !",
				"Attention", JOptionPane.OK_CANCEL_OPTION, 2, null, choice, choice[1]);
		if(option == 0){
			retour = "continuer";
		}
		if(option == 1){
			retour = "annuler";
		}
		return retour;
	}
	
	public String VeriFile(String algo, String ft){
		String retour = "";
		System.out.println("models/Model_"+ft+"_"+algo+".model");
		if (new File ("models/Model_"+ft+"_"+algo+".model").exists()){         
			String[] choice = {"Oui", "Non"};
			int option = JOptionPane.showOptionDialog(null, "Le modèle \"Model_"+ft+"_"+algo+".model\" existe déjà.\n                    Souhaitez-vous le conserver ?",
					"CROC - Modèle de classification", JOptionPane.OK_CANCEL_OPTION, 2, null, choice, choice[0]);
			if(option == 0){
				retour = "oui";
			}
			if(option == 1){
				retour = "non";
			}
		}
		else{
			retour = "stop";
		}
		return retour;
	}
	
	public int ChooseFile(JFrame parent){
		JFileChooser choix = new JFileChooser();
		int retour=choix.showOpenDialog(parent);
		if(retour==JFileChooser.APPROVE_OPTION){ 
		   choix.getSelectedFile().getName();
		   this.featSet = choix.getSelectedFile().getName().substring(0,choix.getSelectedFile().getName().length()-4);
		}
		System.out.println(retour);
		return retour;
	}
	
	public void SetAlgo(String algo){
		this.algo = algo;
		System.out.println(algo);
	}
	
	public void SetFeatureSet(String ftSet){
		this.featSet = ftSet;
		System.out.println(ftSet);
	}

	public void GenerateModel() throws Exception{
		final JFrame window4 = new JFrame();
	    window4.setTitle("CROC - Coreference Resolution for Oral Corpora");
	    window4.setSize(600, 200);
	    window4.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    window4.setLocationRelativeTo(null);
	    window4.setVisible(true);
	    
	    JPanel top = new JPanel();
	    top.setBorder(new LineBorder(Color.BLACK));
	    JLabel jlabel = new JLabel("Calcul du modèle ...");
	    top.add(jlabel);
	    window4.add(top,BorderLayout.CENTER);

		String keepModel = this.VeriFile(this.algo, this.featSet);
		if (!keepModel.equals("stop")){
			//-------------------- 4) On vérifie si le modèle demandé existe déjà -------------------
			if(keepModel.equals("non")){
				//-------------------- 4-a) Si oui, on demande à l'utilisateur s'il souhaite l'écraser ou le conserver -------------------
				String confirm = this.VerifDelete();
				if (confirm.equals("continuer")){
					//-------------------- 4-a-i) S'il l'écrase, on lance la création du nouveau modèle --------------------
				    System.out.println("calcul du modèle");
					new SetModel("learningData.arff", this.algo, this.featSet);
					top.remove(jlabel);
					top.revalidate();
					top.repaint();
					JLabel jl1 = new JLabel("Le modèle a été correctement crée");
					jl1.setFont(new Font("Dialog", 1, 15));
					top.add(jl1, JLabel.CENTER);
					new Interface_VerifWindow().ExitQuestion(window4);
				}
			}
			else{
				//-------------------- 4-a-ii) Sinon, le processus se termine --------------------
				top.remove(jlabel);
				top.revalidate();
				top.repaint();
				JLabel jlabel2 = new JLabel("L'ancien modèle a été conservé");
				jlabel2.setFont(new Font("Dialog", 1, 15));
				top.add(jlabel2, JLabel.CENTER);
				new Interface_VerifWindow().ExitQuestion(window4);
			}
		}
		else{
			//-------------------- 4-b) S'il n'existe pas, on lance sa création -------------------
			JFrame fenetre = new JFrame();
			fenetre.setTitle("Calcul du modèle ...");
			fenetre.setSize(400, 100);
			fenetre.setLocationRelativeTo(null);       
			fenetre.setVisible(true);
			new SetModel("learningData.arff", this.algo, this.featSet);
			fenetre.dispose();
			top.remove(jlabel);
			top.revalidate();
			top.repaint();
			JLabel jlb3 = new JLabel("Le modèle a été correctement crée");
			jlb3.setFont(new Font("Dialog", 1, 15));
			top.add(jlb3, JLabel.CENTER);
			new Interface_VerifWindow().ExitQuestion(window4);
		}
	}
}
