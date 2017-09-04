import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathFactory;


public class Interface_CoreferenceResolver {
	
	public Interface_CoreferenceResolver() throws Exception{
		final JFrame fenetre = new JFrame();
		fenetre.setTitle("CROC - Détection des chaînes de coréférence");
   		 fenetre.setSize(600, 200);
   		 fenetre.setLocationRelativeTo(null);      
   		 fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   		 fenetre.setVisible(true);
   		 
		final int total = new File("testData").listFiles().length;
		new Thread(new Runnable() {
			public void run() {
			   try {
				   int compteur = 1;
		           for (File j : new DirectoryReader().listFiles("testData")){
			           final int c = compteur;
			           final File f = j; 
			           fenetre.setTitle("CROC - Détection des chaînes de coréférence");
			           fenetre.setSize(600, 200);
			           fenetre.setLayout(new GridBagLayout());
			           fenetre.setLocationRelativeTo(null);      
			           fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			           JPanel jp = new JPanel();
			           Dimension dim = new Dimension(400, 80);
			           jp.setPreferredSize(dim);
			           jp.setBackground(Color.LIGHT_GRAY); 
			           jp.setLayout(new GridBagLayout());
			           jp.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
			           JLabel jl = new JLabel("Traitement du fichier "+c+"/"+total+" : "+f.getName());
			           jl.setForeground(Color.WHITE);
			           jp.add(jl);
			           fenetre.add(jp);fenetre.setVisible(true);
			           FileProcess(f, fenetre);
			           compteur++;
			           fenetre.remove(jp);
			           fenetre.repaint();
		           }
		           fenetre.dispose();
		           JFrame fin = new JFrame();
		           fin.setTitle("CROC - Détection des chaînes de coréférence");
		           fin.setSize(600, 200);
		           fin.setLocationRelativeTo(null);      
		           fin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		     		 
		           JLabel jl2 = new JLabel("Tous les fichiers ont été traités !");
		           jl2.setFont(new Font("Dialog", 1, 15));
		           JPanel jp2 = new JPanel();
		           jp2.add(jl2);
		           fin.add(jp2);
		           fin.setVisible(true);
		           new Interface_VerifWindow().ExitQuestion(fin);
		           
				} catch (Exception e) {e.printStackTrace();}
			}
		}
		).start();
	}
		
	public void FileProcess(File j, JFrame fenetre) throws Exception{
		int nbFile = 0;
		File f = new File("ResultFiles_"+j.getName().substring(0,j.getName().length()-4));
		String nameArff = j.getName().substring(0, j.getName().length()-3)+"arff";
		if (!new File(f.getName()+"/"+nameArff).exists()){
			f.mkdir();
			new PairSelection("test", "w20", "testData/"+j.getName());
		}
		
		DirectoryReader testParcours_2 = new DirectoryReader();
		String nameTxt = j.getName().substring(0, j.getName().length()-3)+"txt";
		File resu = new File(f.getName()+"/resultEval_"+nameTxt);
		if (resu.exists()){
			resu.delete();
		}
		int resuFile = 0;
		SAXBuilder sxb = new SAXBuilder();
		try{
			System.out.println(j.getName());
			Document document = sxb.build("testData/"+j.getName());
			if ((Element)XPathFactory.instance().compile("//relation").evaluateFirst(document) == null){
					JOptionPane.showMessageDialog(null, "Le fichier d'origine \""+j.getName()+"\" ne contient pas l'annotation de référence\n"
							+ "	Les métriques d'évaluation ne pourront être calculées !",
							"Attention", 2);
				resuFile = 1;
				nbFile = nbFile-1;
			}
		}
		catch(Exception e){System.out.println(e);}
		for (File m : testParcours_2.listFiles("models")){
			System.out.println(m.getName());
			if (m.getName().contains(".model")){
				nbFile++;
				ResolverSystem step3 = new ResolverSystem(f.getName(),f.getName()+"/"+nameArff, "models/"+m.getName());
				ChainComparator step4 = new ChainComparator(f.getName(),f.getName()+"/"+nameArff, m.getName());
				if (resuFile==0){
					step3.EvalOutput(f.getName()+"/reference_"+nameTxt, f.getName()+"/"+m.getName()+"_system_"+nameTxt, m.getName());
				}
				FormatTransformation step5 = new FormatTransformation();
				if (! new File(f.getName()+"/TEI_"+m.getName()+"_system"+"_"+j.getName()).exists()){
					step5.Traitement_TEI(f.getName(), "testData/"+j.getName(), step4.GetSystemReponse(), "system", m.getName());	
				}
				if (!new File(f.getName()+"/TEI_reference_"+j.getName()).exists()){
					step5.Traitement_TEI(f.getName(), "testData/"+j.getName(), step4.GetTrueReponse(), "reference", m.getName());
				}
			}
		}
		while (new File(f.getName()).listFiles().length!= (nbFile+3)){
			this.CleanDir(f.getName());
		}
	}
	
	public void CleanDir (String dirName){
		for (File i : new DirectoryReader().listFiles(dirName)){
				if (i.getName().contains("Model") && (i.getName().contains("arff") || i.getName().contains("txt"))){ 
				new File(dirName+"/"+i.getName()).delete();
			}
			if (i.getName().substring(0,9).equals("reference")){
				new File(dirName+"/"+i.getName()).delete();
			}
		}
	}
}
