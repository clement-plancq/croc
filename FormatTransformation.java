import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathFactory;

/*
 *  Cette classe permet d'opérer différentes transformations de format de fichier :
 *  	1) Vers le format SemEval
 *  		La méthode SemEvalOutput attend 5 paramètres :
 *  			- le nom du dossier contenant le fichier arff à transformer
 *  			- le nom du fichier lui-même
 *  			- L'ensemble de chaînes de coréférence obtenues en sortie de classe ChainComparator
 *  			- un String "type" pouvant prendre soit la valeur "systeme", soit la valeur "reference",
 *  			  selon la nature des données du fichier à transformer
 *  			- Le nom du modèle de classification
 *  		A partir de l'ensemble de chaînes de coréférence de la forme {{1,2}, {3,5,7,8}, {4,9}, {6}},
 *  		La méthode crée un fichier .txt de la forme :
					7	(7)
					8	(7)
					9	(9)
					10	(9)
					11	(11)
					12	(9)
					13	(13)
					14	(14)
					15	(15)
					16	(15)
		  La première colonne correspond à l'identifiant numérique de l'unité, 
		  La seconde à l'identifiant de la chaîne dans laquelle elle s'inscrit.
 *  					
 *  
 *  	2) Vers le format TEI
 *  		La méthode Traitement_TEI attend 5 paramètres :
 *  			- le nom du dossier contenant le fichier xml à transformer
 *  			- le nom du fichier lui-même (fichier xml initiaux)
 *  			- L'ensemble de chaînes de coréférence obtenues en sortie de classe ChainComparator
 *  			- un String "type" pouvant prendre soit la valeur "systeme", soit la valeur "reference",
 *  			  selon la nature des données du fichier à transformer
 *  			- Le nom du modèle de classification
 *  		Elle commence par construire un format intermédiaire à partir du fichier xml, de la forme :
 *  			<chaine id="1">
 *  				<maillon id="4">
 *  				<maillon id="5">
 *  			...
 *  		Ces nouveau format est stocké dans un docment DOM, qui est ensuite fourni en paramètre de la classe
 *  		TEIMaker qui se charge de la transformation du format et de la création du fichir de sortié.
 * 
 */

public class FormatTransformation {
	private File arffFile;
	private File xmlFile;
	
	public void SemEvalOutput (String dir, File file, Set<Set<Integer>> systemReponse, String type, String model) throws IOException{
		this.arffFile = file;
		List<List<Integer>> chainsList = new ArrayList<List<Integer>>();
		for (Set<Integer> set : systemReponse){
			List<Integer> listMentions = new ArrayList<Integer>();
			for (int mention : set){
				listMentions.add(mention);
				Collections.sort(listMentions);
			}
			chainsList.add(listMentions);
		}
		for (List<Integer> list : chainsList){
			RecurseSort(list, chainsList);
		}
		String fileName = "";
		if (type.equals("system")){
			fileName = model+"_system_"+arffFile.getName().substring(0,arffFile.getName().length()-4)+"txt";
		}
		if (type.equals("true")){
			fileName = "reference_"+arffFile.getName().substring(0,arffFile.getName().length()-4)+"txt";
		}
		FileWriter fstream = new FileWriter(dir+"/"+fileName, true);
		BufferedWriter outFile  = new BufferedWriter(fstream);
		outFile.write("#begin document abc.txt\n");
		int element = 1;
		while (element < 175){
			for (List<Integer> list : chainsList){
				if (list.contains(element)){
					int entity = chainsList.get(chainsList.indexOf(list)).get(0);
					outFile.write(element+"\t("+entity+")\n");
				}
			}
			element++;
		}
		outFile.write("#end document");
		outFile.close();
	}
	
	public void Traitement_TEI(String dir, String file, Set<Set<Integer>> systemChains, String setType, String model){
			this.xmlFile = new File(file);
			String fileName = "";
			String dirName = "";
			if (file.contains("/")){
				int slashIndex = file.lastIndexOf("/");
				fileName = file.substring(slashIndex+1, (int)file.length());
				dirName =  file.substring(0, slashIndex+1);
			}
			SAXBuilder sxb = new SAXBuilder();
			try{
				Document document = sxb.build(new File(dirName+this.xmlFile.getName()));
				Element annotation = (Element)(XPathFactory.instance().compile("//annotations")).evaluateFirst(document);
				int compteur = 1;
				for (Set<Integer> entite : systemChains){
					Element entity = new Element("chaine");
					Attribute id = new Attribute ("id", Integer.toString(compteur));
					entity.setAttribute(id);
					for (int element : entite){
						Element maillon = new Element("maillon");
						Attribute id_maillon = new Attribute ("id", Integer.toString(element));
						maillon.setAttribute(id_maillon);
						entity.addContent(maillon);	
					}
					annotation.addContent(entity);
					compteur++;
				}
				new TEIMaker(document, fileName, dir, setType, model);
			}
			catch (Exception e){System.out.println(e);}	
	}
	
	public List<List<Integer>> RecurseSort (List<Integer> courant,  List<List<Integer>> listReponse){
		int position = listReponse.indexOf(courant);
		if (position > 0){
			int compteur = position-1;
			while (compteur >= 0){
				int previous = listReponse.get(compteur).get(0);
				if (previous > courant.get(0)){
					Collections.swap(listReponse, position, position-1);
					RecurseSort(courant, listReponse);
					compteur = -1;
				}
				else{
					compteur --;
				}
			}
		}
		return listReponse;
	}
	
}
